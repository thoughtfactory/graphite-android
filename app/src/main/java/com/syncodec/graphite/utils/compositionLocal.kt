package com.syncodec.graphite.utils

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.syncodec.graphite.presentation.common.richText.RichTextEditor


//val LocalVaultState =
//	compositionLocalOf<Graphite.Companion.VaultState> { error("Vault state unavailable...") }
//val LocalFilterState = compositionLocalOf<Filter> { error("Filter state unavailable...") }
val LocalCompositionPremium = compositionLocalOf<Boolean> { error("No premium state provided") }
val LocalSelectedState = compositionLocalOf<Boolean> { error("No selected state provided") }
val LocalRichTextEditor = compositionLocalOf<RichTextEditor> { error("No richtext editor provided") }
@OptIn(ExperimentalMaterialApi::class)
val LocalModalBottomSheetState = compositionLocalOf<ModalBottomSheetState> { error("No modal bottom sheet state provided") }
val LocalModalBottomSheetType = compositionLocalOf<Int> { error("No modal bottom sheet type provided") }
val LocalSetModalBottomSheetType = compositionLocalOf<(Int) -> Unit> { error("No modal bottom sheet type setter provided") }
val LocalSaveNote = compositionLocalOf<() -> Unit> { error("No save note provided") }

//fun <T> stateSaver() = Saver<MutableState<T>, Any>(
//	save = { state -> state.value ?: "null" },
//	restore = { value ->
//		@Suppress("UNCHECKED_CAST")
//		mutableStateOf((if (value == "null") null else value) as T)
//	}
//)


val LocalBackgroundColor = compositionLocalOf { Color.White }
