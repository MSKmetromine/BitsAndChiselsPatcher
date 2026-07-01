package ru.polyakhovav.bitsandchiselspatcher

import java.util.concurrent.Executors

object ModExecutors {
    val POOL = Executors.newSingleThreadExecutor()

    fun submit(runnable: Runnable) {
        POOL.submit(runnable)
    }
}