package com.syncodec.graphite.utils


sealed class DataLoader<out T> {

    data object Init : DataLoader<Nothing>()

    data object Loading : DataLoader<Nothing>()

    data object NoData : DataLoader<Nothing>()

    data class Error(val message: String?, val exception: Exception?): DataLoader<Nothing>()

    data class Loaded<T>(val data: T, val hash: Int? = null) : DataLoader<T>()
}
