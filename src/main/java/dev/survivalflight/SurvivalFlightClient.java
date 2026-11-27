package dev.survivalflight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.lwjgl.glfw.GLFW;

/** Client-only permission toggle. Vanilla LocalPlayer implements all flight movement. */
public final class SurvivalFlightClient implements ClientModInitializer {
    private static SurvivalFlightClient instance;
    private final FlightSession session = new FlightSession();
    private final FlightSafety safety = new FlightSafety();
    private KeyMapping toggle;
    private LocalPlayer owner;
    private Object level;
    private GameType mode;
    private boolean wasFlying;

    @Override
    public void onInitializeClient() {
        instance = this;
        toggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.survivalflight.toggle", GLFW.GLFW_KEY_F7, KeyMapping.Category.MISC));
        ClientTickEvents.START_CLIENT_TICK.register(this::tick);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
    }

    private void reset() {
        session.disable();
        safety.reset();
        owner = null;
        level = null;
        mode = null;
        wasFlying = false;
    }

    /** Called after vanilla applies the authoritative packet, on the client thread. */
    public static void serverAbilitiesReceived() {
        if (instance == null) return;
        boolean active = instance.session.enabled();
        instance.session.forget();
        instance.safety.reset();
        instance.wasFlying = false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (active && player != null) {
            message(player, airborne(player) && !player.getAbilities().flying
                    ? "message.survivalflight.danger" : "message.survivalflight.off");
        }
    }

    private static boolean airborne(LocalPlayer player) {
        // Warn conservatively even just above the ground; no fall-safety claim is made.
        return !player.onGround() && !player.isSpectator();
    }

    private boolean eligible(LocalPlayer player) {
        Minecraft client = Minecraft.getInstance();
        return player != null && player == owner && player == client.player
                && client.level == level && client.gameMode != null
                && client.gameMode.getPlayerMode() == GameType.SURVIVAL
                && mode == GameType.SURVIVAL && player.isAlive()
                && !player.isPassenger() && !player.isFallFlying()
                && !player.isInWater() && !player.isInLava();
    }

    public static boolean miningAssistActive(Player player) {
        if (instance == null || player != instance.owner) return false;
        LocalPlayer local = instance.owner;
        return instance.session.enabled() && instance.eligible(local)
                && local.getAbilities().flying && local.getY() >= local.level().getMinY();
    }

    public static boolean reportGrounded() {
        // Shared packet classes may also be decoded by an integrated server/Netty thread.
        // Never change those packets (including packets belonging to another player).
        if (instance == null || !Minecraft.getInstance().isSameThread()
                || !instance.eligible(instance.owner)) return false;
        LocalPlayer player = instance.owner;
        return instance.safety.protects(instance.session.enabled(), player.getAbilities().flying,
                true, player.getY(), player.level().getMinY());
    }

    private static void message(LocalPlayer player, String key) {
        player.sendOverlayMessage(Component.translatable(key));
    }

    private void tick(Minecraft client) {
        LocalPlayer player = client.player;
        GameType currentMode = client.gameMode == null ? null : client.gameMode.getPlayerMode();
        boolean nativeFlight = currentMode == GameType.CREATIVE || currentMode == GameType.SPECTATOR;
        if (player != owner || client.level != level || currentMode != mode) {
            boolean active = session.enabled();
            // Vanilla already assigned the native mode's flags: never overwrite them.
            if (player == owner && nativeFlight) session.forget();
            else session.disable();
            safety.reset();
            owner = player;
            level = client.level;
            mode = currentMode;
            wasFlying = false;
            while (toggle.consumeClick()) { }
            if (active && player != null && !nativeFlight && airborne(player)) {
                message(player, "message.survivalflight.danger");
            }
        }
        if (player == null || client.level == null || !player.isAlive() || currentMode != GameType.SURVIVAL) {
            session.disable();
            safety.reset();
            wasFlying = false;
            while (toggle.consumeClick()) { }
            return;
        }
        if (eligible(player)) safety.observe(session.enabled(), player.getAbilities().flying, player.onGround());
        else safety.reset();
        while (toggle.consumeClick()) {
            if (client.gui.screen() != null) continue;
            if (session.enabled()) {
                boolean previouslyFlying = player.getAbilities().flying;
                session.disable();
                // Notify vanilla's server of the restored flying flag, if it changed.
                if (previouslyFlying != player.getAbilities().flying) player.onUpdateAbilities();
                message(player, airborne(player) && !player.getAbilities().flying
                        ? "message.survivalflight.danger" : "message.survivalflight.off");
            } else {
                Abilities abilities = player.getAbilities();
                session.enable(new FlightSession.FlightFlags() {
                    public boolean mayfly() { return abilities.mayfly; }
                    public boolean flying() { return abilities.flying; }
                    public void mayfly(boolean value) { abilities.mayfly = value; }
                    public void flying(boolean value) { abilities.flying = value; }
                });
                message(player, "message.survivalflight.on");
            }
        }
        if (session.enabled()) {
            session.maintainPermission();
            if (wasFlying && !player.getAbilities().flying && airborne(player)) {
                message(player, "message.survivalflight.danger");
            }
        }
        wasFlying = session.enabled() && player.getAbilities().flying;
    }
}
