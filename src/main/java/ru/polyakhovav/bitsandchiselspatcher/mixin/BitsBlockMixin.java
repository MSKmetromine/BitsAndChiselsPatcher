package ru.polyakhovav.bitsandchiselspatcher.mixin;

import io.github.coolmineman.bitsandchisels.BitsBlock;
import io.github.coolmineman.bitsandchisels.BitsBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BitsBlock.class)
public class BitsBlockMixin {
    /**
     * @author A-Polyakhov
     * @reason compatibility is not preservable
     */
    @Overwrite(remap = false)
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BitsBlockEntity((BlockState[][][]) null, pos, state, false);
    }
}
