package ru.polyakhovav.bitsandchiselspatcher

import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object SharedBitsCache {
    private val CACHE: ConcurrentMap<Int, WeakReference<Array<BlockState>>> = ConcurrentHashMap()

    private val EMPTY = BitStorageUtil.full(Blocks.AIR.defaultBlockState())

    fun empty() = EMPTY

    fun get(bits: Array<BlockState>): Array<BlockState> {
        val hash = bits.contentHashCode()

        synchronized(CACHE) {
            val cached = CACHE[hash]?.get()

            if (cached != null) {
                return cached
            }

            CACHE[hash] = WeakReference(bits)

            return bits
        }
    }

    fun getByHash(hash: Int) = CACHE[hash]
}