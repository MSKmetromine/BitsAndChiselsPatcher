package ru.polyakhovav.bitsandchiselspatcher

import io.github.coolmineman.bitsandchisels.BitMeshes
import io.github.coolmineman.bitsandchisels.mixin.SimpleVoxelShapeFactory
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object VoxelShapeBuildQueue {
    private val CACHE: ConcurrentMap<Int, CompletableFuture<VoxelShape>> = ConcurrentHashMap()

    fun queue(bits: Array<BlockState>) =
        CompletableFuture.supplyAsync({ SharedBitsCache.getIndex(bits) }, ModExecutors.EXECUTOR)
            .thenComposeAsync({ hash ->
                CACHE.computeIfAbsent(hash) {
                    CompletableFuture.supplyAsync({
                        val voxelShape = BitSetDiscreteVoxelShape(16, 16, 16)

                        for ((i, bit) in bits.withIndex()) {
                            if (bit.isAir) {
                                continue
                            }

                            voxelShape.fill(
                                BitStorageUtil.getX(i),
                                BitStorageUtil.getY(i),
                                BitStorageUtil.getZ(i),
                            )
                        }

                            SimpleVoxelShapeFactory.getSimpleVoxelShape(voxelShape)
                    }, ModExecutors.EXECUTOR)
                }
            }, ModExecutors.EXECUTOR)
}