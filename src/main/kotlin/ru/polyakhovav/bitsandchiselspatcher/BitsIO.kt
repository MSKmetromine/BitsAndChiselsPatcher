package ru.polyakhovav.bitsandchiselspatcher

import io.github.coolmineman.bitsandchisels.BitNbtUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.level.block.state.BlockState

object BitsIO {
    fun loadFromNBT(tag: CompoundTag, bits: Array<BlockState>) {
        if (tag.contains("bits") || tag.contains("bits_v2")) {
            val states = Array(16) {
                Array(16) {
                    Array<BlockState?>(16) {
                        null
                    }
                }
            }

            BitNbtUtil.read3DBitArray(tag, states)

            val newBits = BitStorageUtil.from3D(states as Array<Array<Array<BlockState>>>)
            newBits.copyInto(newBits)

            return
        }

        val paletteList = tag.getList("palette", Tag.TAG_COMPOUND.toInt())
        val palette = paletteList.map { BitNbtUtil.toBlockState(it as CompoundTag) }

        val compressed = tag.getByteArray("bits_v2_compressed")
        val data = CompressionUtil.decompress(compressed)

        for (i in bits.indices) {
            val paletteIndex = data[i].toInt()
            bits[i] = palette[paletteIndex]
        }
    }

    fun saveToNBT(tag: CompoundTag, bits: Array<BlockState>) {
        val palette = mutableListOf<BlockState>()

        val data = ByteArray(8192) { index ->
            val state = bits[index]

            var paletteIndex = palette.indexOf(state)

            if (paletteIndex == -1) {
                paletteIndex = palette.size
                palette += state
            }

            paletteIndex.toByte()
        }

        val compressed = CompressionUtil.compress(data)
        tag.putByteArray("bits_v2_compressed", compressed)

        val paletteList = ListTag()

        for (state in palette) {
            paletteList.add(
                BitNbtUtil.fromBlockState(state)
            )
        }

        tag.put("palette", paletteList)
    }
}