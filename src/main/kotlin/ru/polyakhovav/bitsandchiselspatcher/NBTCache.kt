package ru.polyakhovav.bitsandchiselspatcher

import io.github.coolmineman.bitsandchisels.BitNbtUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import java.util.Collections
import java.util.WeakHashMap

object NBTCache {
    private val CACHE: MutableMap<BitShape, CompoundTag> = Collections.synchronizedMap(
        WeakHashMap()
    )

    fun get(shape: BitShape) = CACHE.computeIfAbsent(shape) {
        val states = BitStorageUtil.to3D(shape.bits)

        val tag = CompoundTag()
        BitNbtUtil.write3DBitArray(tag, states)

        tag
    }
}