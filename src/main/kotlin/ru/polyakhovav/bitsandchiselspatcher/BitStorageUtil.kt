package ru.polyakhovav.bitsandchiselspatcher

import net.minecraft.core.Vec3i
import net.minecraft.world.level.block.state.BlockState

object BitStorageUtil {
    fun toIndex(x: Int, y: Int, z: Int) = (x shl 8) or (y shl 4) or z

    fun getX(index: Int) = (index shr 8) and 0xF
    fun getY(index: Int) = (index shr 4) and 0xF
    fun getZ(index: Int) = index and 0xF

    fun to3D(bits: Array<BlockState>) =
        Array(16) { x ->
            Array(16) { y ->
                Array(16) { z ->
                    bits[toIndex(x, y, z)]
                }
            }
        }

    fun from3D(array: Array<Array<Array<BlockState>>>) =
        Array(16 * 16 * 16) { index ->
            array[getX(index)][getY(index)][getZ(index)]
        }

    fun full(state: BlockState) = SharedBitsCache.get(
        Array(16 * 16 * 16) { state }
    )
}