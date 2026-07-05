package ru.polyakhovav.bitsandchiselspatcher

import java.util.concurrent.Executors
import kotlin.math.max

object ModExecutors {
    val EXECUTOR = Executors.newWorkStealingPool(
        max(1, Runtime.getRuntime().availableProcessors() / 2)
    )
}