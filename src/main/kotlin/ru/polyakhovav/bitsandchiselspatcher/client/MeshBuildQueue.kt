package ru.polyakhovav.bitsandchiselspatcher.client

import io.github.coolmineman.bitsandchisels.BitMeshes
import io.github.coolmineman.bitsandchisels.ColorPolice
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import ru.polyakhovav.bitsandchiselspatcher.BitStorageUtil
import ru.polyakhovav.bitsandchiselspatcher.BitsAndChiselsPatcher
import ru.polyakhovav.bitsandchiselspatcher.ModExecutors
import ru.polyakhovav.bitsandchiselspatcher.SharedBitsCache
import java.lang.ref.WeakReference
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.logging.Logger

object MeshBuildQueue {
    private val CACHE: ConcurrentMap<Int, CompletableFuture<Mesh>> = ConcurrentHashMap()

    fun queue(level: Level, pos: BlockPos, bits: Array<BlockState>) =
        CompletableFuture.supplyAsync({ SharedBitsCache.getIndex(bits) }, ModExecutors.EXECUTOR)
            .thenComposeAsync({ hash ->
                CACHE.computeIfAbsent(hash) {
                    CompletableFuture.supplyAsync<Mesh>({
                        val bits3D = BitStorageUtil.to3D(bits)
                        BitMeshes.createMesh(bits3D, level, pos)
                    }, ModExecutors.EXECUTOR)
                        .orTimeout(5L, TimeUnit.SECONDS)
                        .exceptionally {
                            Logger.getLogger("bitsandchiselspatcher")
                                .log(java.util.logging.Level.SEVERE, "Failed to build bit mesh.", it)

                            CACHE.remove(hash)

                            null
                        }
                }
            }, ModExecutors.EXECUTOR)
}