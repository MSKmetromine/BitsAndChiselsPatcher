package ru.polyakhovav.bitsandchiselspatcher

import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.lang.ref.WeakReference
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object SharedBitsCache {
    private val CACHE = Collections.synchronizedList(
        mutableListOf<WeakReference<Array<BlockState>>>()
    )

    private val EMPTY = BitStorageUtil.full(Blocks.AIR.defaultBlockState())

    fun empty() = EMPTY

    fun getIndex(bits: Array<BlockState>): Int {
        synchronized(CACHE) {
            return CACHE.indexOfFirst { it.get()?.contentEquals(bits) == true }
        }
    }

    fun get(bits: Array<BlockState>): Array<BlockState> {
        synchronized(CACHE) {
            val cachedRef = CACHE.firstOrNull { it.get()?.contentEquals(bits) == true }
            val cached = cachedRef?.get()

            if (cached != null) {
                return cached
            }

            CACHE += WeakReference(bits)

            return bits
        }
    }
}