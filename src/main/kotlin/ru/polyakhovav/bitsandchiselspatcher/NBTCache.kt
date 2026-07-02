package ru.polyakhovav.bitsandchiselspatcher

import io.github.coolmineman.bitsandchisels.BitNbtUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState

object NBTCache {
    private val CACHE = mutableMapOf<Int, CompoundTag>()

    fun get(bits: Array<BlockState>) = CACHE.computeIfAbsent(bits.contentHashCode()) {
        val states = BitStorageUtil.to3D(bits)

        val tag = CompoundTag()
        BitNbtUtil.write3DBitArray(tag, states)

        tag
    }
}