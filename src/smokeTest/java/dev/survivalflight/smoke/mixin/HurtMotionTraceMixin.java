package dev.survivalflight.smoke.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import dev.survivalflight.smoke.SprintDescentSmokeTest;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Read-only test trace, excluded from the installable JAR. */
@Mixin(ClientPacketListener.class)
abstract class HurtMotionTraceMixin {
    @Inject(method="handleSetEntityMotion",at=@At("RETURN"))
    private void survivalflight$trace(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        var player=Minecraft.getInstance().player;
        if(Boolean.getBoolean("survivalflight.hurtSmokeTest")&&player!=null&&packet.id()==player.getId()) {
            System.out.println("[SF-HURT] WIRE raw="+packet.movement()+", applied="+player.getDeltaMovement()+", hurtTime="+player.hurtTime+", flying="+player.getAbilities().flying);
        }
    }
    @Inject(method="handleMovePlayer",at=@At("RETURN"))
    private void survivalflight$correction(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if(Boolean.getBoolean("survivalflight.sprintSmokeTest")) SprintDescentSmokeTest.correction(packet.id(),packet.change().position());
    }
}
