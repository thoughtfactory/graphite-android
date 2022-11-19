package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnAddLink
import com.syncodec.graphite.presentation.bucket.composable.buildingBlock.SearchResultStatusView
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.text.LargeTextField


@Preview
@Composable
fun AddLinkBottomSheet() {
	val onAddLink = LocalCompositionOnAddLink.current
	var urlText by remember { mutableStateOf("") }

	var isTextFocused by remember { mutableStateOf(false) }
	val focusRequester = remember { FocusRequester() }

	val closeSheet = LocalCompositionCloseBottomSheet.current

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Add Link",
			icon = R.drawable.ic_link,
		)

		Spacer(modifier = Modifier.height(8.dp))

		LargeTextField(
			modifier = Modifier.padding(24.dp, 0.dp),
			text = urlText,
			placeholder = "http:// or https://",
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				autoCorrect = true,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Go
			),
			isFocused = isTextFocused,
			focusRequester = focusRequester,
			onFocusChanged = { isTextFocused = it },
			onValueChanged = { urlText = it },
			keyboardActions = KeyboardActions(
				onGo = {
					onAddLink(urlText)
					urlText = ""
					closeSheet()
				}
			)
		)

		Spacer(modifier = Modifier.height(8.dp))

		SearchResultStatusView(
			imageId = R.drawable.il_bucket_link_search,
			text = "Spotify, YouTube, Netflix anything you want!",
			contentDescription = "Add Link",
		)

		Spacer(modifier = Modifier.height(32.dp))
	}
}
