package ru.polyakhovav.bitsandchiselspatcher.shape

import net.minecraft.world.level.block.state.BlockState

object ShapeUtil {
    fun fullBlock(state: BlockState) = Array(16 * 16 * 16) { state }

    fun getIndex(x: Int, y: Int, z: Int) = (y shl 8) or (z shl 4) or x
}