package ru.polyakhovav.bitsandchiselspatcher

import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.lang.ref.WeakReference
import java.util.Collections
import java.util.UUID
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicInteger

object SharedBitsCache {
    private val LAST_ID = AtomicInteger()

    private val CACHE_IDS: MutableMap<BitShape, Int> = Collections.synchronizedMap(WeakHashMap())
    private val CACHE_OBJECTS: MutableMap<Int, WeakReference<BitShape>> = ConcurrentHashMap()

    private val EMPTY = BitStorageUtil.full(Blocks.AIR.defaultBlockState())

    fun empty() = EMPTY

    fun get(bits: Array<BlockState>): BitShape {
        val shape = BitShape(bits)

        synchronized(CACHE_IDS) {
            var id = CACHE_IDS[shape]

            if (id != null) {
                val obj = CACHE_OBJECTS[id]?.get()

                if (obj != null) {
                    return obj
                }
            }

            id = LAST_ID.getAndIncrement()

            CACHE_IDS[shape] = id
            CACHE_OBJECTS[id] = WeakReference(shape)

            return shape
        }
    }
}