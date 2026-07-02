package ru.polyakhovav.bitsandchiselspatcher.client

import io.github.coolmineman.bitsandchisels.BitMeshes
import io.github.coolmineman.bitsandchisels.ColorPolice
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import ru.polyakhovav.bitsandchiselspatcher.BitStorageUtil
import ru.polyakhovav.bitsandchiselspatcher.ModExecutors
import java.lang.ref.WeakReference
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Consumer

object MeshBuildQueue {
    private val CACHE: ConcurrentMap<Int, CompletableFuture<Mesh>> = ConcurrentHashMap()

    fun queue(level: Level, pos: BlockPos, bits: Array<BlockState>) =
        CompletableFuture.supplyAsync(bits::contentHashCode, ModExecutors.EXECUTOR)
            .thenComposeAsync({ hash ->
                CACHE.computeIfAbsent(hash) {
                    CompletableFuture.supplyAsync({
                        val bits3D = BitStorageUtil.to3D(bits)
                        BitMeshes.createMesh(bits3D, level, pos)
                    }, ModExecutors.EXECUTOR)
                }
            }, ModExecutors.EXECUTOR)
}