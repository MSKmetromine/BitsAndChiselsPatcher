package ru.polyakhovav.bitsandchiselspatcher

import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

object CompressionUtil {
    private val CACHE_COMPRESSED = mutableMapOf<Int, WeakReference<ByteArray>>()
    private val CACHE_UNCOMPRESSED = mutableMapOf<Int, WeakReference<ByteArray>>()

    fun compress(data: ByteArray) = CACHE_COMPRESSED.computeIfAbsent(data.contentHashCode()) {
        val output = ByteArrayOutputStream()

        val stream = DeflaterOutputStream(output)
        stream.write(data)
        stream.close()

        WeakReference(output.toByteArray())
    }.get()!!

    fun decompress(compressed: ByteArray) = CACHE_UNCOMPRESSED.computeIfAbsent(compressed.contentHashCode()) {
        val output = ByteArrayOutputStream()

        val stream = DeflaterOutputStream(output)
        stream.write(compressed)
        stream.close()

        WeakReference(output.toByteArray())
    }.get()!!
}