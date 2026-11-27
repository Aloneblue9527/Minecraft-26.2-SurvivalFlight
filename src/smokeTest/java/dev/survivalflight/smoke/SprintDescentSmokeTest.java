package dev.survivalflight.smoke;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Function;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/** Explicit opt-in input/correction checks, only for a disposable integrated world. */
public final class SprintDescentSmokeTest implements ClientModInitializer {
    private static SprintDescentSmokeTest instance;
    private int stage,time,total,corrections,checks;
    private boolean counting,sawSprint;
    private Vec3 start;

    @Override public void onInitializeClient() {
        if(Boolean.getBoolean("survivalflight.sprintSmokeTest")) {
            instance=this;ClientTickEvents.END_CLIENT_TICK.register(this::tick);
        }
    }
    public static void correction(int id,Vec3 position) {
        if(instance!=null&&instance.counting) {
            instance.corrections++;
            System.out.println("[SF-SPRINT] CORRECTION id="+id+", position="+position);
        }
    }
    private void check(boolean ok,String message) {
        if(!ok)throw new AssertionError(message);
        checks++;System.out.println("[SF-SPRINT] PASS "+message);
    }
    private void next(){stage++;time=0;}
    private <T>T server(Minecraft mc,Function<ServerPlayer,T> action) {
        var srv=mc.getSingleplayerServer();
        if(srv==null)throw new AssertionError("Disposable integrated world only");
        var uuid=mc.player.getUUID();return srv.submit(()->action.apply(srv.getPlayerList().getPlayer(uuid))).join();
    }
    private void command(Minecraft mc,String...commands) {
        server(mc,p->{for(String cmd:commands)mc.getSingleplayerServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),cmd);return true;});
    }
    private void inputs(Minecraft mc,boolean forward,boolean sprint,boolean shift,boolean jump) {
        mc.options.keyUp.setDown(forward);mc.options.keySprint.setDown(sprint);
        mc.options.keyShift.setDown(shift);mc.options.keyJump.setDown(jump);
    }
    private void measure(Minecraft mc){start=mc.player.position();corrections=0;sawSprint=false;counting=true;}
    private void finish(Minecraft mc,String label,boolean expectDescend) {
        counting=false;
        Vec3 delta=mc.player.position().subtract(start);
        System.out.println("[SF-SPRINT] RESULT "+label+": corrections="+corrections+", delta="+delta+", sawSprint="+sawSprint+", flying="+mc.player.getAbilities().flying);
        check(mc.player.getAbilities().flying,label+" keeps actual Mod flight");
        if(Boolean.getBoolean("survivalflight.expectSprintRebound")&&label.equals("sprint+Shift+forward")) {
            check(corrections>1,"published 1.1.1 repeated position correction reproduced");
        } else {
            check(corrections==0,label+" has zero server corrections");
            if(expectDescend)check(delta.y < -2,label+" descends normally");
            else check(Math.abs(delta.y)<0.05,label+" preserves horizontal hover");
            check(delta.horizontalDistance()>4,label+" advances normally");
        }
    }
    private void tick(Minecraft mc) {
        if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        try {
            if(++total>3000)throw new AssertionError("Sprint fixture timeout stage="+stage);
            time++;
            switch(stage) {
                case 0 -> {
                    if(!mc.player.isAlive()){if(time%40==0)mc.player.respawn();return;}
                    mc.gui.setScreen(null);inputs(mc,false,false,false,false);
                    command(mc,"difficulty peaceful","gamemode survival","execute in minecraft:overworld run tp @s 0.5 61 0.5 0 0",
                            "time set day","clear @s","fill -6 61 -6 6 95 6 air","fill -6 60 -6 6 60 6 stone","spawnpoint @s 0 61 0");
                    server(mc,p->{p.clearFire();p.setHealth(20);return true;});next();
                }
                case 1 -> {
                    if(time<60||!mc.player.onGround())return;
                    KeyMapping.click(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F7));next();
                }
                case 2 -> {
                    if(time==5||time==9)mc.options.keyJump.setDown(true);
                    if(time==7||time==11)mc.options.keyJump.setDown(false);
                    if(time<35)return;
                    check(mc.player.getAbilities().flying,"F7/native double-jump flight active");
                    command(mc,"tp @s 0.5 200 0.5 0 0");next();
                }
                case 3 -> {
                    if(time<40)return;
                    check(Math.abs(mc.player.getY()-200)<0.05,"airborne hover settled away from terrain");
                    inputs(mc,true,true,false,false);measure(mc);next();
                }
                case 4 -> {
                    sawSprint|=mc.player.isSprinting();
                    if(time<40)return;
                    finish(mc,"sprint+forward",false);check(sawSprint,"vanilla sprint really engaged before Shift");
                    inputs(mc,true,true,true,false);measure(mc);next();
                }
                case 5 -> {
                    sawSprint|=mc.player.isSprinting();
                    if(time<100)return;
                    finish(mc,"sprint+Shift+forward",true);
                    inputs(mc,false,false,false,false);
                    if(Boolean.getBoolean("survivalflight.expectSprintRebound")) {
                        System.out.println("[SF-SPRINT] BASELINE REPRODUCED");mc.stop();stage=99;
                    } else {next();}
                }
                case 6 -> {
                    if(time<40)return;
                    command(mc,"tp @s 0.5 200 0.5 0 0");next();
                }
                case 7 -> {
                    if(time<40)return;
                    inputs(mc,true,false,true,false);measure(mc);next();
                }
                case 8 -> {
                    if(time<60)return;
                    finish(mc,"Shift+forward without sprint",true);
                    inputs(mc,true,true,false,false);measure(mc);next();
                }
                case 9 -> {
                    sawSprint|=mc.player.isSprinting();
                    // Count corrections through release; measure steady hover after vanilla inertia.
                    if(time==12)start=mc.player.position();
                    if(time<62)return;
                    finish(mc,"release Shift then sprint+forward",false);
                    inputs(mc,false,false,false,false);next();
                }
                case 10 -> {
                    if(time<40)return;
                    command(mc,"tp @s 0.5 200 0.5 0 0");next();
                }
                case 11 -> {
                    if(time<40)return;
                    inputs(mc,true,true,false,false);measure(mc);next();
                }
                case 12 -> {
                    sawSprint|=mc.player.isSprinting();
                    // Exercise repeated press/release transitions, with no test teleports.
                    mc.options.keyShift.setDown(time%16<8);
                    if(time<128)return;
                    counting=false;
                    check(corrections==0,"repeated Shift press/release has zero corrections");
                    check(mc.player.position().subtract(start).horizontalDistance()>10,"alternating Shift continues forward");
                    inputs(mc,false,false,false,false);next();
                }
                case 13 -> {
                    if(time<40)return;
                    command(mc,"tp @s 0.5 200 0.5 0 0");next();
                }
                case 14 -> {
                    if(time<40)return;
                    inputs(mc,true,true,false,true);measure(mc);next();
                }
                case 15 -> {
                    sawSprint|=mc.player.isSprinting();
                    if(time<40)return;
                    counting=false;Vec3 delta=mc.player.position().subtract(start);
                    check(corrections==0&&delta.y>2&&delta.horizontalDistance()>4,"sprint+Space+forward ascends without correction");
                    inputs(mc,false,false,false,false);next();
                }
                case 16 -> {
                    if(time<40)return;
                    server(mc,p->{for(int x=-2;x<=1;x++)for(int z=-2;z<=1;z++)p.level().getChunk(x,z);return true;});
                    command(mc,"fill -24 60 -24 24 60 24 stone","fill -4 61 -24 4 95 24 air","tp @s 0.5 90 0.5 0 0");
                    server(mc,p->{p.setHealth(20);return true;});next();
                }
                case 17 -> {
                    if(time<30)return;
                    inputs(mc,true,true,true,false);measure(mc);
                    KeyMapping.click(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F7));next();
                }
                case 18 -> {
                    if(time<40||!mc.player.onGround())return;
                    counting=false;inputs(mc,false,false,false,false);
                    check(corrections==0,"Shift+forward pending landing after F7 OFF has zero corrections");
                    check(server(mc,ServerPlayer::getHealth)==20,"pending landing keeps experimental fall protection");
                    check(!mc.player.getAbilities().mayfly&&!mc.player.getAbilities().flying,"F7 OFF restores survival flight flags");
                    System.out.println("[SF-SPRINT] ALL PASS: "+checks+" checks");mc.stop();stage=99;
                }
                default -> { }
            }
        } catch(Throwable failure) {
            counting=false;inputs(mc,false,false,false,false);
            System.out.println("[SF-SPRINT] FAIL stage="+stage+" tick="+time);failure.printStackTrace();mc.stop();stage=99;
        }
    }
}
