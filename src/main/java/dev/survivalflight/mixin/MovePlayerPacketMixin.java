package dev.survivalflight.mixin;

import dev.survivalflight.SurvivalFlightClient;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundMovePlayerPacket.class)
abstract class MovePlayerPacketMixin {
    @Shadow @Final @Mutable protected boolean onGround;

    // Preserve positions, rotations and collision flags; never touch server-side decoding.
    @Inject(method = "<init>", at = @At("RETURN"))
    private void survivalflight$flightGroundReport(CallbackInfo ci) {
        if (SurvivalFlightClient.reportGrounded()) onGround = true;
    }
}
