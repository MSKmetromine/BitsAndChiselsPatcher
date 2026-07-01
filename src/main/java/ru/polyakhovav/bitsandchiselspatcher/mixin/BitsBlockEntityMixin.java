package ru.polyakhovav.bitsandchiselspatcher.mixin;

import io.github.coolmineman.bitsandchisels.*;
import io.github.coolmineman.bitsandchisels.mixin.SimpleVoxelShapeFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.polyakhovav.bitsandchiselspatcher.ModExecutors;
import ru.polyakhovav.bitsandchiselspatcher.shape.ShapeCache;
import ru.polyakhovav.bitsandchiselspatcher.shape.ShapeUtil;
import ru.polyakhovav.bitsandchiselspatcher.shape.StoredBitShape;

@Mixin(BitsBlockEntity.class)
public abstract class BitsBlockEntityMixin extends BlockEntity {
    @Shadow(remap = false)
    protected VoxelShape shape;

    @Shadow
    protected Mesh mesh;

    private BitsBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Unique
    private StoredBitShape bitShape;

    @Inject(method = "<init>([[[Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V", at = @At("TAIL"))
    public void init(BlockState[][][] shape, BlockPos par2, BlockState par3, boolean par4, CallbackInfo ci) {
        this.bitShape = ShapeCache.INSTANCE.submit(
                ShapeUtil.INSTANCE.fullBlock(Blocks.AIR.defaultBlockState())
        );
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V", at = @At("RETURN"))
    public void init(BlockState state, BlockPos par2, BlockState par3, boolean par4, CallbackInfo ci) {
        this.bitShape = ShapeCache.INSTANCE.submit(
                ShapeUtil.INSTANCE.fullBlock(state)
        );
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putUUID("ShapeID", this.bitShape.getId());
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite
    public void load(CompoundTag tag) {
        super.load(tag);
//        BitNbtUtil.read3DBitArray(var1, this.states);
//        this.rebuildShape();
//        if (this.getLevel() != null && this.getLevel().isClientSide) {
//            this.postFromClientTag();
//        }
//
//        this.rebuildNbtCache();
//        this.alive = true;

        if (tag.contains("bits_v2") || tag.contains("bits")) {
            var states = new BlockState[16][16][16];
            BitNbtUtil.read3DBitArray(tag, states);
            this.setStates(states);
        } else {
            if (tag.hasUUID("ShapeID")) {
                var id = tag.getUUID("ShapeID");
                this.bitShape = ShapeCache.INSTANCE.get(id);
            }
        }

        this.rebuildShape();

        if (this.level != null && this.level.isClientSide()) {
            this.postFromClientTag();
        }
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    private void rebuildNbtCache() {}

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    public void setState(int x, int y, int z, BlockState state) {
        var newBits = this.bitShape.getBits().clone();

        newBits[ShapeUtil.INSTANCE.getIndex(x, y, z)] = state;

        this.bitShape = ShapeCache.INSTANCE.submit(newBits);
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    public void setStates(BlockState[][][] states) {
        var newBits = new BlockState[16 * 16 * 16];

        for (var x = 0; x < 16; x++) {
            for (var y = 0; y < 16; y++) {
                for (var z = 0; z < 16; z++) {
                    var i = ShapeUtil.INSTANCE.getIndex(x, y, z);
                    newBits[i] = states[x][y][z];
                }
            }
        }

        this.bitShape = ShapeCache.INSTANCE.submit(newBits);
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    public void rebuildServer() {
        this.rebuildShape();
//        this.rebuildNbtCache();
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    public BlockState getState(int x, int y, int z) {
        return this.bitShape.getBits()[ShapeUtil.INSTANCE.getIndex(x, y, z)];
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    public BlockState[][][] getStates() {
        var states = new BlockState[16][16][16];

        for (var x = 0; x < 16; x++) {
            for (var y = 0; y < 16; y++) {
                for (var z = 0; z < 16; z++) {
                    var i = ShapeUtil.INSTANCE.getIndex(x, y, z);
                    states[x][y][z] = this.bitShape.getBits()[i];
                }
            }
        }

        return states;
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    protected void rebuildShape() {
            ModExecutors.INSTANCE.submit(() -> {
            var isFullBlock = true;

            var lightSum = 0;

            var shape = new BitSetDiscreteVoxelShape(16, 16, 16);

            var firstBit = this.bitShape.getBits()[0];

            for (var i = 0; i < this.bitShape.getBits().length; i++) {
                var bit = this.bitShape.getBits()[i];

                if (firstBit != bit) {
                    isFullBlock = false;
                }

                lightSum += bit.getLightEmission();

                // Calculate coordinates
                var rem = i;

                var y = rem / (16 * 16);
                rem %= (16 * 16);

                var z = rem / 16;
                rem %= 16;

                var x = rem;

                // Set bit
                if (!bit.isAir()) {
                    shape.fill(x, y, z);
                }
            }

            if (isFullBlock) {
                if (this.getLevel().isClientSide()) {
                    Minecraft.getInstance().execute(() -> {
                        this.level.setBlockAndUpdate(this.worldPosition, firstBit);
                    });
                } else {
                    this.level.getServer().execute(() -> {
                        this.level.setBlockAndUpdate(this.worldPosition, firstBit);
                    });
                }

                return;
            }

            var newLight = Mth.clamp((int) (Mth.sqrt(lightSum) * 0.0625), 0, 16);

            Runnable runnable = () -> {
                this.shape = SimpleVoxelShapeFactory.getSimpleVoxelShape(shape);

                var state = this.getBlockState();
                if (state.getValue(BitsBlock.LIGHT_LEVEL) != newLight) {
                    this.level.setBlock(this.worldPosition, state.setValue(BitsBlock.LIGHT_LEVEL, newLight), 0);
                }
            };

            if (this.getLevel().isClientSide()) {
                Minecraft.getInstance().execute(runnable);
            } else {
                this.level.getServer().execute(runnable);
            }
        });
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    @Environment(EnvType.CLIENT)
    protected void rebuildMesh() {
        ModExecutors.INSTANCE.submit(() -> {
            var newMesh = BitMeshes.createMesh(this.getStates(), this.level, this.worldPosition);

            Minecraft.getInstance().execute(() -> {
                this.mesh = newMesh;
                Minecraft.getInstance().levelRenderer.setBlocksDirty(this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ());
            });
        });
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    public void postFromClientTag() {
        this.rebuildMesh();
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    public void sync() {
        this.setChanged();
        ((ServerLevel) this.level).getChunkSource().blockChanged(this.getBlockPos());
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    public CompoundTag getUpdateTag() {
        var tag = new CompoundTag();
        tag.putUUID("ShapeID", this.bitShape.getId());
        return tag;
    }

    /**
     * @author A-Polyakhov
     * @reason reworked
     */
    @Overwrite(remap = false)
    @Environment(EnvType.CLIENT)
    public @Nullable Object getRenderAttachmentData() {
        return this.mesh;
    }
}
