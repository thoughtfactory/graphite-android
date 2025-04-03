package com.syncodec.graphite.utils

import kotlin.random.Random


sealed class DataLoader<out T> {

    abstract val rand: Int

    data class Init(override val rand: Int = Random.nextInt()) : DataLoader<Nothing>()

    data class Loading(override val rand: Int = Random.nextInt()) : DataLoader<Nothing>()

    data class NoData(override val rand: Int = Random.nextInt()) : DataLoader<Nothing>()

    data class Error(val message: String?, val exception: Exception?, override val rand: Int = Random.nextInt()): DataLoader<Nothing>()

    data class Loaded<T>(val data: T, val hash: Int? = null, override val rand: Int = Random.nextInt()) : DataLoader<T>()
}
