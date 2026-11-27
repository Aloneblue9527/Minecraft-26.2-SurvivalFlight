package dev.survivalflight.mixin;

import dev.survivalflight.SurvivalFlightClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
    @Inject(method = "handlePlayerAbilities", at = @At("RETURN"))
    private void survivalflight$serverAbilities(ClientboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        SurvivalFlightClient.serverAbilitiesReceived();
    }

    @Redirect(method = "handleSetEntityMotion", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;lerpMotion(Lnet/minecraft/world/phys/Vec3;)V"))
    private void survivalflight$hurtMotion(Entity entity, Vec3 incoming) {
        entity.lerpMotion(SurvivalFlightClient.stabilizeHurtMotion(entity, incoming));
    }
}
