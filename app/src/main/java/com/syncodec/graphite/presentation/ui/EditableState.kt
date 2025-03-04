package com.syncodec.graphite.presentation.ui

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow


data class EditableState<T>(
    val isEditableFlow: MutableStateFlow<Boolean> = MutableStateFlow(value = false),
    val onSave: (T) -> Unit = {}
) {
    fun onEdit() = this.isEditableFlow.tryEmit(value = true)

    fun onSaveEdit(data: T) {
        this.onSave(data)
        this.isEditableFlow.tryEmit(value = false)
    }

    fun onDiscard() = this.isEditableFlow.tryEmit(value = false)
}

fun <T> LocalEditableState() = staticCompositionLocalOf<EditableState<T>> { error("") }
