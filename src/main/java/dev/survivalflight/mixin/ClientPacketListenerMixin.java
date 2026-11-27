package dev.survivalflight.mixin;

import dev.survivalflight.SurvivalFlightClient;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
    @Inject(method = "handlePlayerAbilities", at = @At("RETURN"))
    private void survivalflight$serverAbilities(ClientboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
        SurvivalFlightClient.serverAbilitiesReceived();
    }
}
