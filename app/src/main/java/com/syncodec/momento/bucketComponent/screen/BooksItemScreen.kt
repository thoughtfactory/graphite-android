package com.syncodec.momento.bucketComponent.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.syncodec.momento.bucketComponent.BucketItemViewModel
import com.syncodec.momento.bucketComponent.modalBottomSheet.BookData


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BooksItemScreen(
) {
	val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

	val viewModel: BucketItemViewModel = viewModel()
//	val bookData: BookData = objectMapper.readValue(viewModel.bucketItemDataJson)

	val scrollState = rememberScrollState()

	Column {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
		) {
//			AndroidView(
//				factory = { editorView },
//				update = { editor ->
//				},
//				modifier = Modifier
//					.fillMaxWidth()
//					.fillMaxHeight()
//			)
		}
//		EditorToolbar(editorView, {})
	}

}
