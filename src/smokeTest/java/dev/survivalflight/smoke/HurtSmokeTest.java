package dev.survivalflight.smoke;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Function;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/** Opt-in destructive test of real Blaze-owned projectiles in a DISPOSABLE world. */
public final class HurtSmokeTest implements ClientModInitializer {
    private int stage, time, total, blazeId;
    private double startY, startX, startZ, minY, minVy;
    private boolean sawDamage, sawFire;

    @Override public void onInitializeClient() {
        if (Boolean.getBoolean("survivalflight.hurtSmokeTest")) ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }
    private void next() { stage++; time = 0; }
    private void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
        System.out.println("[SF-HURT] PASS " + message);
    }
    private <T> T server(Minecraft mc, Function<ServerPlayer,T> action) {
        var integrated=mc.getSingleplayerServer();
        if (integrated==null) throw new AssertionError("Disposable integrated server only");
        var uuid=mc.player.getUUID();
        return integrated.submit(()->action.apply(integrated.getPlayerList().getPlayer(uuid))).join();
    }
    private void command(Minecraft mc,String... commands) {
        server(mc,p->{for(String cmd:commands)mc.getSingleplayerServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(),cmd);return true;});
    }
    private void motion(Minecraft mc,int id,Vec3 movement) {
        mc.getConnection().handleSetEntityMotion(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(id,movement));
    }
    private void controls(Minecraft mc) {
        // Synthetic handler controls supplement the real projectile; fixture state only.
        server(mc,p->{p.clearFire();return true;});
        var p=mc.player;
        p.hurtTime=10;p.setDeltaMovement(0,0,0);
        motion(mc,p.getId(),new Vec3(0.2,-0.4,0.1));
        check(p.getDeltaMovement().equals(new Vec3(0.2,0,0.1)),"only downward Y changes; horizontal packet components are exact");
        p.setDeltaMovement(0,0.15,0);
        motion(mc,p.getId(),new Vec3(0.2,-0.4,0.1));
        check(p.getDeltaMovement().y==0.15,"existing ascent is retained");
        motion(mc,p.getId(),new Vec3(0.2,0.4,0.1));
        check(p.getDeltaMovement().y==0.4,"upward damage motion is retained");
        mc.options.keyShift.setDown(true);
        motion(mc,p.getId(),new Vec3(0.2,-0.4,0.1));
        check(p.getDeltaMovement().y==-0.4,"Shift descent is retained during hurt");
        mc.options.keyShift.setDown(false);p.hurtTime=0;
        motion(mc,p.getId(),new Vec3(0.2,-0.4,0.1));
        check(p.getDeltaMovement().y==-0.4,"healthy flight does not filter server motion");
        p.hurtTime=10;
        var other=mc.level.getEntity(blazeId);
        check(other!=null,"other tracked entity exists for scope control");
        motion(mc,blazeId,new Vec3(0.1,-0.3,0.1));
        check(other.getDeltaMovement().y==-0.3,"other entities retain their motion");
        p.setDeltaMovement(0,0,0);
    }
    private void tick(Minecraft mc) {
        if(mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        try {
            if(++total>1600)throw new AssertionError("Hurt fixture timeout stage="+stage);
            time++;
            switch(stage) {
                case 0 -> {
                    if(!mc.player.isAlive()){if(time%40==0)mc.player.respawn();return;}
                    mc.gui.setScreen(null);
                    command(mc,"difficulty normal","gamemode survival","execute in minecraft:overworld run tp @s 0.5 61 0.5",
                            "time set day","kill @e[type=!player]","clear @s","fill -6 61 -6 6 95 6 air",
                            "fill -6 60 -6 6 60 6 stone","spawnpoint @s 0 61 0");
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
                    check(mc.player.getAbilities().flying,"native survival flight active");
                    command(mc,"tp @s 0.5 80 0.5");next();
                }
                case 3 -> {
                    if(time<40)return;
                    startY=minY=mc.player.getY();startX=mc.player.getX();startZ=mc.player.getZ();minVy=0;
                    check(Math.abs(startY-80)<0.05,"hover settled before actual Blaze-owned fireball");
                    blazeId=server(mc,p->{
                        var level=p.level();
                        var blaze=new Blaze(EntityTypes.BLAZE,level);blaze.setNoAi(true);
                        blaze.setPos(p.getX()-4,p.getY(),p.getZ());level.addFreshEntity(blaze);
                        var shot=new SmallFireball(level,blaze,new Vec3(1,0,0));
                        shot.setPos(p.getX()-3,p.getY()+0.8,p.getZ());level.addFreshEntity(shot);
                        System.out.println("[SF-HURT] actual SmallFireball owner=Blaze, server velocity before hit="+p.getDeltaMovement());
                        return blaze.getId();
                    });next();
                }
                case 4 -> {
                    minY=Math.min(minY,mc.player.getY());minVy=Math.min(minVy,mc.player.getDeltaMovement().y);
                    float hp=server(mc,ServerPlayer::getHealth);
                    if(hp<20)sawDamage=true;
                    if(server(mc,p->p.getRemainingFireTicks()>0))sawFire=true;
                    if(time<50)return;
                    double horizontal=Math.hypot(mc.player.getX()-startX,mc.player.getZ()-startZ);
                    System.out.println("[SF-HURT] RESULT drop="+(startY-minY)+", minVy="+minVy+", horizontal="+horizontal+", serverHP="+hp+", flying="+mc.player.getAbilities().flying);
                    check(sawDamage&&hp<20,"actual fireball/fire damage reaches server health");
                    check(sawFire,"normal ignition is retained");
                    if(Boolean.getBoolean("survivalflight.expectHurtDrop")) {
                        check(startY-minY>0.03,"old version's damage-induced altitude loss reproduced");
                        System.out.println("[SF-HURT] BASELINE REPRODUCED");
                    } else {
                        check(mc.player.getAbilities().flying,"damage does not clear local flight permission");
                        check(startY-minY<0.03,"no damage-induced hover descent");
                        check(horizontal>0.01,"horizontal knockback retained");
                        controls(mc);
                        KeyMapping.click(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F7));
                        next(); return;
                    }
                    mc.stop();stage=99;
                }
                case 5 -> {
                    if(time<4)return;
                    check(!mc.player.getAbilities().flying&&!mc.player.getAbilities().mayfly,"F7 OFF restores original flight flags");
                    mc.player.hurtTime=10;
                    motion(mc,mc.player.getId(),new Vec3(0.2,-0.4,0.1));
                    check(mc.player.getDeltaMovement().y==-0.4,"F7 OFF does not stabilize damage motion");
                    System.out.println("[SF-HURT] ALL PASS");mc.stop();stage=99;
                }
                default -> { }
            }
        }catch(Throwable failure){System.out.println("[SF-HURT] FAIL stage="+stage+" tick="+time);failure.printStackTrace();mc.stop();stage=99;}
    }
}
