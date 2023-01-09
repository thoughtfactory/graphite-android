package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnAddLink
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.SearchResultStatusView
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.text.LargeTextField


@Preview
@Composable
fun AddLinkBottomSheet() {
	val onAddLink = LocalCompositionOnAddLink.current
	var urlText by remember { mutableStateOf("") }

	var isTextFocused by remember { mutableStateOf(false) }

	val closeSheet = LocalCompositionCloseBottomSheet.current

	GenericBottomSheet(
		title = "Add Link",
		icon = R.drawable.ic_link,
	) {

		LargeTextField(
			modifier = Modifier,
			value = urlText,
			placeholder = "http:// or https://",
			isFocused = isTextFocused,
			onFocusChanged = { isTextFocused = it },
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				autoCorrect = true,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Go
			),
			keyboardActions = KeyboardActions(
				onGo = {
					onAddLink(urlText)
					urlText = ""
					closeSheet()
				}
			),
			trailingIcon = R.drawable.ic_search,
			onClickTrailingIcon = { onAddLink(urlText) },
		) { urlText = it }

		Spacer(modifier = Modifier.height(8.dp))

		SearchResultStatusView(
			imageId = R.drawable.il_bucket_link_search,
			text = "Spotify, YouTube, Netflix anything you want to save!",
			contentDescription = "Add Link",
		)
	}
}
