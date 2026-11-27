package dev.survivalflight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public final class SurvivalFlightClient implements ClientModInitializer {
    private KeyMapping toggle;
    private LocalPlayer owner;
    private Object level;
    private boolean enabled;
    private boolean landingProtection;
    private boolean previousMayfly;
    private int airborneTicks;

    @Override public void onInitializeClient() {
        toggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.survivalflight.toggle", GLFW.GLFW_KEY_F7, KeyMapping.Category.MISC));
        ClientTickEvents.START_CLIENT_TICK.register(this::tick);
    }

    private void reset() {
        if (enabled && owner != null && !owner.getAbilities().instabuild) {
            owner.getAbilities().mayfly = previousMayfly;
            if (!previousMayfly) owner.getAbilities().flying = false;
        }
        enabled = false;
        landingProtection = false;
        airborneTicks = 0;
    }

    private void tick(Minecraft mc) {
        LocalPlayer p = mc.player;
        if (p != owner || mc.level != level) {
            reset();
            owner = p;
            level = mc.level;
            while (toggle.consumeClick()) { }
        }
        if (p == null || mc.level == null || !p.isAlive() || mc.gameMode == null) {
            reset();
            return;
        }
        if (!mc.gameMode.getPlayerMode().isSurvival()) {
            enabled = false;
            landingProtection = false;
            airborneTicks = 0;
            while (toggle.consumeClick()) { }
            return;
        }
        while (toggle.consumeClick()) {
            if (mc.screen != null) continue;
            if (!enabled) {
                previousMayfly = p.getAbilities().mayfly;
                enabled = true;
                landingProtection = false;
                airborneTicks = 0;
                p.displayClientMessage(Component.literal("SurvivalFlight: ON"), true);
            } else {
                boolean inAir = !p.onGround();
                reset();
                landingProtection = inAir;
                p.displayClientMessage(Component.literal(inAir ?
                    "Flight OFF - falling! Protection is experimental" : "SurvivalFlight: OFF"), true);
            }
        }
        if (enabled) {
            p.getAbilities().mayfly = true;
            if (p.getAbilities().flying && !p.onGround() && !p.isPassenger()) {
                if (++airborneTicks >= 30) {
                    airborneTicks = 0;
                    if (!mc.options.keyJump.isDown() && !mc.options.keyShift.isDown()) {
                        Vec3 v = p.getDeltaMovement();
                        p.setDeltaMovement(v.x, -0.12, v.z);
                    }
                }
            } else airborneTicks = 0;
        }
        if ((enabled || landingProtection) && !p.onGround() && !p.isFallFlying()
                && !p.isPassenger() && p.getDeltaMovement().y < -0.03) {
            p.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true, p.horizontalCollision));
            p.fallDistance = 0;
        }
        if (landingProtection && p.onGround()) landingProtection = false;
    }
}
