package ru.polyakhovav.bitsandchiselspatcher.mixin;

import io.github.coolmineman.bitsandchisels.BitNbtUtil;
import io.github.coolmineman.bitsandchisels.BitsAndChisels;
import io.github.coolmineman.bitsandchisels.BitsBlock;
import io.github.coolmineman.bitsandchisels.BitsBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.polyakhovav.bitsandchiselspatcher.*;
import ru.polyakhovav.bitsandchiselspatcher.client.MeshBuildQueue;

import java.util.concurrent.CompletableFuture;

@Mixin(BitsBlockEntity.class)
public class BitsBlockEntityMixin extends BlockEntity {
    @Shadow(remap = false)
    private BlockState[][][] states;

    @Shadow(remap = false)
    protected Mesh mesh;

    @Shadow(remap = false)
    protected VoxelShape shape;

    @Shadow
    protected CompoundTag nbtCache;

    @Shadow
    private boolean alive;
    @Unique
    private BlockState[] bits;

    private BitsBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "<init>([[[Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V", at = @At("TAIL"))
    private void init(BlockState[][][] states, BlockPos pos, BlockState state, boolean alive, CallbackInfo ci) {
        if (states == null) {
            this.bits = SharedBitsCache.INSTANCE.empty();
        } else {
            this.bits = SharedBitsCache.INSTANCE.get(
                    BitStorageUtil.INSTANCE.from3D(states)
            );
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V", at = @At("TAIL"))
    private void init(BlockState fillWithState, BlockPos pos, BlockState state, boolean alive, CallbackInfo ci) {
        if (fillWithState == Blocks.AIR.defaultBlockState()) {
            this.bits = SharedBitsCache.INSTANCE.empty();
        } else {
            this.bits = SharedBitsCache.INSTANCE.get(
                    BitStorageUtil.INSTANCE.full(fillWithState)
            );
        }

        this.states = null;
    }

    /**
     * @author A-Polyakhov
     * @reason async handling
     */
    @Overwrite(remap = false)
    @Environment(EnvType.CLIENT)
    public void postFromClientTag() {
        MeshBuildQueue.INSTANCE.queue(this.level, this.worldPosition, bits)
                .thenAcceptAsync(mesh -> {
                    this.mesh = mesh;
                    Minecraft.getInstance().levelRenderer.setBlocksDirty(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ());
                }, Minecraft.getInstance());
    }

    /**
     * @author A-Polyakhov
     * @reason async handling
     */
    @Overwrite(remap = false)
    protected void rebuildShape() {
        VoxelShapeBuildQueue.INSTANCE.queue(this.bits)
                .thenAcceptAsync(shape -> this.shape = shape, Minecraft.getInstance());
    }

    /**
     * @author A-Polyakhov
     * @reason async handling
     */
    @Overwrite
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        var cached = NBTCache.INSTANCE.get(this.bits);
        tag.merge(cached);
    }

    /**
     * @author A-Polyakhov
     * @reason async handling
     */
    @Overwrite
    public void load(CompoundTag tag) {
        super.load(tag);

        if (this.level != null && this.getLevel().isClientSide()) {
            CompletableFuture
                    .supplyAsync(() -> {
                        var states = new BlockState[16][16][16];
                        BitNbtUtil.read3DBitArray(tag, states);
                        return SharedBitsCache.INSTANCE.get(
                                BitStorageUtil.INSTANCE.from3D(states)
                        );
                    }, ModExecutors.INSTANCE.getEXECUTOR())
                    .thenAcceptAsync(bits -> {
                        this.bits = bits;
                        this.alive = true;

                        this.rebuildShape();
                        this.postFromClientTag();
                    }, Minecraft.getInstance());
        } else {
            var states = new BlockState[16][16][16];
            BitNbtUtil.read3DBitArray(tag, states);

            this.bits = SharedBitsCache.INSTANCE.get(
                    BitStorageUtil.INSTANCE.from3D(states)
            );

            this.alive = true;

            this.rebuildShape();
        }
    }

    /**
     * @author A-Polyakhov
     * @reason async handling
     */
    @Overwrite(remap = false)
    private void rebuildNbtCache() {}

    /**
     * @author A-Polyakhov
     * @reason 1d array
     */
    @Overwrite(remap = false)
    public void setState(int x, int y, int z, BlockState state) {
        var bitsCopy = this.bits.clone();
        bitsCopy[BitStorageUtil.INSTANCE.toIndex(x, y, z)] = state;
        this.bits = SharedBitsCache.INSTANCE.get(bitsCopy);
        this.alive = true;

        this.updateBlockState();
    }

    @Unique
    private void updateBlockState() {
        var firstBit = this.bits[0];

        var fullBlock = true;
        var totalLight = 0;

        for (var bit : this.bits) {
            if (bit != firstBit) {
                fullBlock = false;
            }

            totalLight += bit.getLightEmission();
        }

        if (fullBlock) {
            this.level.setBlockAndUpdate(this.worldPosition, firstBit);
            return;
        }

        var newLight = Mth.clamp((int) (Mth.sqrt(totalLight) * (double) 0.0625F), 0, 16);

        if (this.getBlockState().getValue(BitsBlock.LIGHT_LEVEL) != totalLight) {
            this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(BitsBlock.LIGHT_LEVEL, newLight));
        }
    }

    /**
     * @author A-Polyakhov
     * @reason 1d array
     */
    @Overwrite(remap = false)
    public void setStates(BlockState[][][] states) {
        var bits = BitStorageUtil.INSTANCE.from3D(states);
        this.bits = SharedBitsCache.INSTANCE.get(bits);
        this.alive = true;

        this.updateBlockState();
    }

    /**
     * @author A-Polyakhov
     * @reason 1d array
     */
    @Overwrite(remap = false)
    public BlockState getState(int x, int y, int z) {
        return this.bits[BitStorageUtil.INSTANCE.toIndex(x, y, z)];
    }

    /**
     * @author A-Polyakhov
     * @reason 1d array
     */
    @Overwrite(remap = false)
    public BlockState[][][] getStates() {
        return BitStorageUtil.INSTANCE.to3D(this.bits);
    }
}
