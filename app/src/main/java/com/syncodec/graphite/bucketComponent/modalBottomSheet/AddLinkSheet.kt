package com.syncodec.graphite.bucketComponent.modalBottomSheet

import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.bucketComponent.BucketActivity
import com.syncodec.graphite.custom.LargeTextField
import com.syncodec.graphite.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.custom.button.LargeButton

@Composable
fun AddLinkSheet(
	onAction: (BucketActivity.Action, String) -> Unit
) {
	val context = LocalContext.current

	var linkText by remember { mutableStateOf("") }
	var isLinkTextFocused by remember { mutableStateOf(false) }

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface,
		modifier = Modifier.heightIn(120.dp),
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
		) {

			BottomSheetStrip()

			BottomSheetHeader(
				title = "Add a link",
				icon = R.drawable.ic_link
			)

			Spacer(modifier = Modifier.height(8.dp))

			LargeTextField(
				modifier = Modifier.padding(24.dp, 0.dp),
				text = linkText,
				placeholder = "https://",
				isFocused = isLinkTextFocused,
				onFocusChanged = { isLinkTextFocused = it },
				keyboardOptions = KeyboardOptions.Default.copy(
					capitalization = KeyboardCapitalization.None,
					autoCorrect = true,
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Search
				),
				keyboardActions = KeyboardActions(
					onDone = {
						if (linkText.isNotBlank() && URLUtil.isValidUrl(linkText)) {
							onAction(BucketActivity.Action.ADD_LINK, linkText)
						}
					}
				)
			) { linkText = it }

			Spacer(modifier = Modifier.height(16.dp))


			LargeButton(
				text = "Save link",
				enabled = linkText.isNotBlank(),
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp)
			) {
				if (linkText.isNotBlank() ) {
					if (URLUtil.isValidUrl(linkText)) {
						onAction(BucketActivity.Action.ADD_LINK, linkText)
					} else {
						Toast.makeText(context, "Invalid URL!", Toast.LENGTH_SHORT).show()
					}
				}
			}

			Spacer(modifier = Modifier.height(16.dp))
		}
	}
}
