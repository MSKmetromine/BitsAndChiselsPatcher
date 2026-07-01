package ru.polyakhovav.bitsandchiselspatcher.shape

import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object ShapeCache {
    private val CACHE = mutableMapOf<UUID, StoredBitShape>()

    fun find(states: Array<BlockState>): StoredBitShape? {
        for ((key, value) in CACHE) {
            if (value.bits.contentEquals(states)) {
                return value
            }
        }

        return null
    }

    fun submit(states: Array<BlockState>): StoredBitShape {
        val existing = this.find(states)

        if (existing != null) {
            return existing
        }

        val newShape = StoredBitShape(
            id = UUID.randomUUID(),
            bits = states,
        )

        CACHE[newShape.id] = newShape

        return newShape
    }

    fun get(id: UUID): StoredBitShape {
        return CACHE.getOrElse(id) {
            this.submit(
                ShapeUtil.fullBlock(Blocks.AIR.defaultBlockState())
            )
        }
    }
}