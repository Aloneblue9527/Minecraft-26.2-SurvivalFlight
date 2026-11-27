package dev.survivalflight.mixin;

import dev.survivalflight.SurvivalFlightClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
abstract class MiningPacketMixin {
    @Inject(method = "send", at = @At("HEAD"))
    private void survivalflight$miningGroundReport(Packet<?> packet, CallbackInfo ci) {
        if (!(packet instanceof ServerboundPlayerActionPacket action)
                || !SurvivalFlightClient.reportGrounded()) return;
        var player = Minecraft.getInstance().player;
        if (!SurvivalFlightClient.miningAssistActive(player)) return;
        if (action.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK
                || action.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            // Keep the server's start/finish mining calculation consistent with flight.
            // A StatusOnly packet cannot recurse through the PlayerAction condition above.
            player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true, player.horizontalCollision));
        }
    }
}
