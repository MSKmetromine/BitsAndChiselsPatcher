package ru.polyakhovav.bitsandchiselspatcher.mixin;

import io.github.coolmineman.bitsandchisels.BitsBlock;
import io.github.coolmineman.bitsandchisels.BitsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BitsBlock.class)
public class BitsBlockMixin {
    @Redirect(method = "newBlockEntity", at = @At(value = "NEW", target = "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lio/github/coolmineman/bitsandchisels/BitsBlockEntity;"))
    public BitsBlockEntity newBlockEntityNew(BlockPos blockPos, BlockState blockState) {
        return new BitsBlockEntity((BlockState[][][]) null, blockPos, blockState, false);
    }
}
