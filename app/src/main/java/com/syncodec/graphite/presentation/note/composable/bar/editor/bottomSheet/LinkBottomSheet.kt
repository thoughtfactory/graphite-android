package com.syncodec.graphite.presentation.note.composable.bar.editor.bottomSheet

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ICON_BUTTON_SIZE
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.note.kitKat.KitKatAction


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LinkBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	link: String? = null,
	onKitKatAction: (KitKatAction) -> Unit = {}
) {
	val context = LocalContext.current
	val clipboardManager = LocalClipboardManager.current

	var newLink by remember { mutableStateOf("") }
	LaunchedEffect(key1 = link) {
		newLink = link ?: ""
	}

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = "Link",
		) {
			OutlinedTextField(
				value = newLink,
				shape = MaterialTheme.shapes.medium,
				onValueChange = { newLink = it },
				placeholder = { Text(text = "Add a link") },
				trailingIcon = {
					IconButton(
						onClick = { clipboardManager.getText()?.text?.let { newLink = it } ?: Toast.makeText(context, "Nothing to paste", Toast.LENGTH_SHORT).show() },
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_paste),
							contentDescription = "Paste",
							modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
						)
					}
				},
				maxLines = 1,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(8.dp))

			OutlinedButton(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.fillMaxWidth(),
				onClick = { onKitKatAction(KitKatAction.Link.ExtendSelection) }
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_extend_selection),
					contentDescription = "Extend selection",
					modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
				)

				Spacer(modifier = Modifier.width(8.dp))

				Text(text = "Extend selection")
			}

			Spacer(modifier = Modifier.height(4.dp))

			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				OutlinedButton(
					shape = MaterialTheme.shapes.medium,
					modifier = Modifier.weight(1f),
					onClick = { onKitKatAction(KitKatAction.Link.Unset) },
				) {
					Text(text = "Unset")
				}

				Spacer(modifier = Modifier.width(4.dp))

				Button(
					shape = MaterialTheme.shapes.medium,
					modifier = Modifier.weight(1f),
					onClick = {
						if (newLink.isEmpty()) Toast.makeText(context, "Link is empty", Toast.LENGTH_SHORT).show() else {
							onKitKatAction(KitKatAction.Link.Set(newLink))
							onDismissRequest()
						}
					},
				) {
					Text(text = "Set")
				}
			}
		}
	}
}
