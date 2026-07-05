package ru.polyakhovav.bitsandchiselspatcher

import net.minecraft.world.level.block.state.BlockState

data class BitShape(
    val bits: Array<BlockState>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BitShape

        if (!bits.contentEquals(other.bits)) return false

        return true
    }

    override fun hashCode(): Int {
        return bits.contentHashCode()
    }
}
