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
import ru.polyakhovav.bitsandchiselspatcher.client.MeshBuildQueue
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

object VoxelShapeBuildQueue {
    private val CACHE: MutableMap<BitShape, CompletableFuture<VoxelShape>> = Collections.synchronizedMap(
        WeakHashMap()
    )

    fun queue(shape: BitShape) =
        CompletableFuture.supplyAsync( {
            CACHE.computeIfAbsent(shape) {
                CompletableFuture.supplyAsync({
                    val voxelShape = BitSetDiscreteVoxelShape(16, 16, 16)

                    for ((i, bit) in shape.bits.withIndex()) {
                        if (bit.isAir) {
                            continue
                        }

                        voxelShape.fill(
                            BitStorageUtil.getX(i),
                            BitStorageUtil.getY(i),
                            BitStorageUtil.getZ(i),
                        )
                    }

                    SimpleVoxelShapeFactory.getSimpleVoxelShape(voxelShape) as VoxelShape
                }, ModExecutors.EXECUTOR)
                    .orTimeout(5L, TimeUnit.SECONDS)
                    .exceptionally {
                        Logger.getLogger("bitsandchiselspatcher")
                            .log(java.util.logging.Level.SEVERE, "Failed to build bit voxel shape.", it)

                        CACHE -= shape

                        null
                    }
            }.get()
        }, ModExecutors.EXECUTOR)
}