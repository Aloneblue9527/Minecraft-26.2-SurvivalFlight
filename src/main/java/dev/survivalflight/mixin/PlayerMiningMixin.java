package dev.survivalflight.mixin;

import dev.survivalflight.SurvivalFlightClient;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
abstract class PlayerMiningMixin {
    // Only bypass the airborne /5. Tool, effects, underwater and fatigue remain vanilla.
    @Redirect(method = "getDestroySpeed", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"))
    private boolean survivalflight$miningGround(Player player) {
        return player.onGround() || SurvivalFlightClient.miningAssistActive(player);
    }
}
