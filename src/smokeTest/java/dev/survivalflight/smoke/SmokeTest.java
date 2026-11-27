package dev.survivalflight.smoke;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Function;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;

/** OPT-IN destructive fixtures for a DISPOSABLE local world. Never shipped in the mod. */
public final class SmokeTest implements ClientModInitializer {
    private int stage, time, total, checks, groundTicks, airTicks, miningTicks, dimension;
    private float groundSpeed;
    private BlockPos target;
    private boolean sawVoidDamage, warmed;
    private final String[] dimensions = {"overworld", "the_nether", "the_end"};

    @Override public void onInitializeClient() {
        if (Boolean.getBoolean("survivalflight.smokeTest")) ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }
    private void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        checks++;
        System.out.println("[SF-SMOKE] PASS " + message);
    }
    private void next() { stage++; time = 0; }
    private void toggle() { KeyMapping.click(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F7)); }
    private <T> T server(Minecraft mc, Function<ServerPlayer, T> action) {
        var integrated = mc.getSingleplayerServer();
        if (integrated == null) throw new AssertionError("Only a disposable integrated server is permitted");
        var uuid = mc.player.getUUID();
        return integrated.submit(() -> action.apply(integrated.getPlayerList().getPlayer(uuid))).join();
    }
    private void command(Minecraft mc, String... commands) {
        server(mc, player -> {
            for (String cmd : commands) mc.getSingleplayerServer().getCommands()
                    .performPrefixedCommand(player.createCommandSourceStack(), cmd);
            return true;
        });
    }
    private float health(Minecraft mc) { return server(mc, ServerPlayer::getHealth); }
    private void heal(Minecraft mc) { server(mc, p -> { p.setHealth(20); return true; }); }
    private void jump(Minecraft mc) {
        if (time == 5 || time == 9) mc.options.keyJump.setDown(true);
        if (time == 7 || time == 11) mc.options.keyJump.setDown(false);
    }
    private void placeTarget(Minecraft mc) {
        target = BlockPos.containing(mc.player.getX(), mc.player.getY() + 1, mc.player.getZ() + 2);
        server(mc, p -> { p.level().setBlockAndUpdate(target, Blocks.STONE.defaultBlockState()); return true; });
        miningTicks = 0;
    }
    private boolean mine(Minecraft mc) {
        if (miningTicks++ == 0) mc.gameMode.startDestroyBlock(target, Direction.NORTH);
        else mc.gameMode.continueDestroyBlock(target, Direction.NORTH);
        if (mc.level.getBlockState(target).isAir()) {
            mc.gameMode.stopDestroyBlock();
            return server(mc, p -> p.level().getBlockState(target).isAir());
        }
        if (miningTicks > 80) throw new AssertionError("Server did not accept mined block");
        return false;
    }
    private void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) return;
        try {
            if (++total > 3600) throw new AssertionError("Integration timeout: " + stage);
            time++;
            switch (stage) {
                case 0 -> {
                    if (!mc.player.isAlive()) { if (time % 40 == 0) mc.player.respawn(); return; }
                    mc.gui.setScreen(null);
                    command(mc, "difficulty peaceful", "gamemode survival", "execute in minecraft:overworld run tp @s 0.5 61 0.5", "clear @s",
                            "fill -5 61 -5 5 95 5 air", "fill -5 60 -5 5 60 5 stone",
                            "spawnpoint @s 0 61 0", "tp @s 0.5 61 0.5 0 0", "give @s netherite_pickaxe");
                    next();
                }
                case 1 -> {
                    if (time < 60 || !mc.player.onGround()) return;
                    check(!mc.player.getAbilities().mayfly, "starts OFF in survival");
                    groundSpeed = mc.player.getDestroySpeed(Blocks.STONE.defaultBlockState());
                    float authoritative = server(mc, p -> p.getDestroySpeed(Blocks.STONE.defaultBlockState()));
                    check(groundSpeed == authoritative && groundSpeed > 1, "ground tool baseline client/server = " + groundSpeed);
                    placeTarget(mc); next();
                }
                case 2 -> {
                    if (time > 15 && mine(mc)) {
                        // Both measured blocks must include vanilla's between-block delay.
                        if (!warmed) { warmed = true; placeTarget(mc); time = 0; }
                        else { groundTicks = miningTicks; next(); toggle(); }
                    }
                }
                case 3 -> {
                    jump(mc);
                    if (time < 35) return;
                    check(mc.player.getAbilities().flying && !mc.player.onGround(), "F7 and native double jump fly without changing physical onGround");
                    float flyingSpeed = mc.player.getDestroySpeed(Blocks.STONE.defaultBlockState());
                    float authoritative = server(mc, p -> p.getDestroySpeed(Blocks.STONE.defaultBlockState()));
                    check(flyingSpeed == groundSpeed, "client flying mining equals ground tool speed");
                    System.out.println("[SF-SMOKE] server sample speed=" + authoritative + ", grounded=" + server(mc, ServerPlayer::onGround));
                    check(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly(false, false).isOnGround(), "outbound flight packet flag is corrected");
                    check(!server(mc, p -> new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly(false, false).isOnGround()),
                            "server-thread packet construction is unaffected");

                    placeTarget(mc); next();
                }
                case 4 -> {
                    if (time > 15 && mine(mc)) {
                        airTicks = miningTicks;
                        check(airTicks <= groundTicks + 2, "real stone break ticks ground=" + groundTicks + ", flying=" + airTicks);
                        command(mc, "tp @s 0.5 90 0.5"); heal(mc); next();
                    }
                }
                case 5 -> {
                    if (time < 30) return;
                    check(mc.player.getAbilities().flying && mc.player.getY() > 85, "flying above a 29-block landing");
                    toggle(); next();
                }
                case 6 -> {
                    if (time < 40 || !mc.player.onGround()) return;
                    check(mc.player.isAlive() && health(mc) == 20, "F7 OFF 29-block landing retains server health 20");
                    check(!mc.player.getAbilities().mayfly && !mc.player.getAbilities().flying, "F7 restores survival abilities");
                    command(mc, "tp @s 0.5 68 0.5"); next();
                }
                case 7 -> {
                    if (time < 30 || !mc.player.onGround()) return;
                    float hp = health(mc);
                    check(hp < 20 && hp > 0, "later ordinary fall is unprotected, server health=" + hp);
                    next();
                }
                case 8 -> {
                    String dim = "minecraft:" + dimensions[dimension];
                    // Keep the End fixture away from the exit portal/dragon's central structures.
                    int x = dimension == 2 ? 300 : 0;
                    server(mc, p -> {
                        var key = dimension == 0 ? net.minecraft.world.level.Level.OVERWORLD
                                : dimension == 1 ? net.minecraft.world.level.Level.NETHER : net.minecraft.world.level.Level.END;
                        var level = mc.getSingleplayerServer().getLevel(key);
                        for (int cx = (x-4) >> 4; cx <= (x+4) >> 4; cx++)
                            for (int cz = (x-4) >> 4; cz <= (x+4) >> 4; cz++) level.getChunk(cx, cz);
                        return true;
                    });
                    command(mc, "execute in " + dim + " run fill " + (x-4) + " 61 " + (x-4) + " " + (x+4) + " 95 " + (x+4) + " air",
                            "execute in " + dim + " run fill " + (x-4) + " 60 " + (x-4) + " " + (x+4) + " 60 " + (x+4) + " stone",
                            "execute in " + dim + " run tp @s " + (x+0.5) + " 61 " + (x+0.5));
                    heal(mc); next();
                }
                case 9 -> {
                    if (time < 60 || mc.gui.screen() != null || !mc.player.onGround()) return;
                    check(!mc.player.getAbilities().mayfly, "dimension/rejoin OFF: " + dimensions[dimension]);
                    toggle(); next();
                }
                case 10 -> {
                    jump(mc);
                    if (time < 35) return;
                    check(mc.player.getAbilities().flying, "flight armed before void test: " + dimensions[dimension] + ", mayfly=" + mc.player.getAbilities().mayfly + ", ground=" + mc.player.onGround());
                    if (dimension > 0) {
                        double x = dimension == 2 ? 300.5 : 0.5;
                        command(mc, "tp @s " + x + " 90 " + x); heal(mc);
                        stage = 13; time = 0;
                    } else {
                        int voidY = mc.level.getMinY() - 70;
                        command(mc, "tp @s 0.5 " + voidY + " 0.5");
                        sawVoidDamage = false; next();
                    }
                }
                case 11 -> {
                    float hp = health(mc);
                    if (hp < 20) sawVoidDamage = true;
                    if (mc.player.isAlive()) {
                        if (time > 240) throw new AssertionError("Void did not kill: " + dimensions[dimension] + ", health=" + hp);
                        return;
                    }
                    check(sawVoidDamage && hp == 0, "normal void damage and death: " + dimensions[dimension]);
                    next();
                }
                case 12 -> {
                    if (!mc.player.isAlive()) { if (time % 20 == 0) mc.player.respawn(); return; }
                    if (time < 60 || mc.gui.screen() != null) return;
                    dimension++;
                    if (dimension < dimensions.length) { stage = 8; time = 0; }
                    else { System.out.println("[SF-SMOKE] ALL PASS: " + checks + " checks"); mc.stop(); stage = 99; }
                }
                case 13 -> { if (time >= 30) { toggle(); next(); } }
                case 14 -> {
                    if (time < 40 || !mc.player.onGround()) return;
                    check(mc.player.isAlive() && health(mc) == 20,
                            "F7 OFF 29-block landing retains server health 20: " + dimensions[dimension]);
                    toggle(); next();
                }
                case 15 -> {
                    jump(mc);
                    if (time < 35) return;
                    check(mc.player.getAbilities().flying, "rearmed flight before void: " + dimensions[dimension]);
                    command(mc, "tp @s 0.5 " + (mc.level.getMinY() - 70) + " 0.5");
                    sawVoidDamage = false; stage = 11; time = 0;
                }
                default -> { }
            }
        } catch (Throwable failure) {
            System.out.println("[SF-SMOKE] FAIL stage=" + stage + " tick=" + time);
            failure.printStackTrace(); mc.stop(); stage = 99;
        }
    }
}
