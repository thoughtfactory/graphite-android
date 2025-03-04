package com.syncodec.graphite.di.network


sealed class NetworkResponse<out T> {
    data object Init : NetworkResponse<Nothing>()
    data object Loading : NetworkResponse<Nothing>()
    data class Success<T>(val data: T) : NetworkResponse<T>()
    data class Error(val exception: Exception? = null, val message: String? = null) : NetworkResponse<Nothing>()
}
