package ru.polyakhovav.bitsandchiselspatcher.client

import io.github.coolmineman.bitsandchisels.BitMeshes
import io.github.coolmineman.bitsandchisels.ColorPolice
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import ru.polyakhovav.bitsandchiselspatcher.BitShape
import ru.polyakhovav.bitsandchiselspatcher.BitStorageUtil
import ru.polyakhovav.bitsandchiselspatcher.BitsAndChiselsPatcher
import ru.polyakhovav.bitsandchiselspatcher.ModExecutors
import ru.polyakhovav.bitsandchiselspatcher.SharedBitsCache
import java.lang.ref.WeakReference
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.logging.Logger

object MeshBuildQueue {
    private val CACHE: MutableMap<BitShape, CompletableFuture<Mesh>> = Collections.synchronizedMap(
        WeakHashMap()
    )

    fun queue(level: Level, pos: BlockPos, shape: BitShape) =
        CompletableFuture.supplyAsync({
            CACHE.computeIfAbsent(shape) {
                CompletableFuture.supplyAsync({
                    val bits3D = BitStorageUtil.to3D(shape.bits)
                    BitMeshes.createMesh(bits3D, level, pos)
                }, ModExecutors.EXECUTOR)
                    .orTimeout(5L, TimeUnit.SECONDS)
                    .exceptionally {
                        Logger.getLogger("bitsandchiselspatcher")
                            .log(java.util.logging.Level.SEVERE, "Failed to build bit mesh.", it)

                        CACHE -= shape

                        null
                    }
            }.get()
        }, ModExecutors.EXECUTOR)
}