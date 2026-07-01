package ru.polyakhovav.bitsandchiselspatcher.shape

import net.minecraft.world.level.block.state.BlockState
import java.util.UUID

data class StoredBitShape(
    val bits: Array<BlockState>,
    val id: UUID,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoredBitShape

        if (!bits.contentEquals(other.bits)) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bits.contentHashCode()
        result = 31 * result + id.hashCode()
        return result
    }

}