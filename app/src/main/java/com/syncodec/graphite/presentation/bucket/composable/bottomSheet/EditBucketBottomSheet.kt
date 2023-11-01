package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.BucketObject
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.button.CancelButton


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditBucketBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	bucketObject: BucketObject? = null,
	onClickUpdate : (String, String) -> Unit = {_, _ ->}
) {

	var bucketTitleText by remember { mutableStateOf("") }
	var bucketDescriptionText by remember { mutableStateOf("") }

	LaunchedEffect(key1 = bucketObject) {
		bucketTitleText = bucketObject?.title ?: ""
		bucketDescriptionText = bucketObject?.description ?: ""
	}

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.menu),
		) {
			OutlinedTextField(
				value = bucketTitleText,
				shape = MaterialTheme.shapes.medium,
				onValueChange = { bucketTitleText = it },
				label = { Text(text = stringResource(id = R.string.title)) },
				placeholder = { Text(text = stringResource(id = R.string.bucket_title_placeholder)) },
				trailingIcon = { CancelButton { bucketTitleText = "" } },
				maxLines = 1,
				singleLine = true,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(4.dp))

			OutlinedTextField(
				value = bucketDescriptionText,
				shape = MaterialTheme.shapes.medium,
				onValueChange = { bucketDescriptionText = it },
				label = { Text(text = stringResource(id = R.string.description)) },
				placeholder = { Text(text = stringResource(id = R.string.bucket_description_placeholder)) },
				trailingIcon = { CancelButton { bucketDescriptionText = "" } },
				maxLines = 1,
				singleLine = true,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(8.dp))

			Button(
				shape = MaterialTheme.shapes.medium,
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary,
					disabledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f),
					disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
				),
				modifier = Modifier.fillMaxWidth(),
				onClick = {
					onClickUpdate(bucketTitleText, bucketDescriptionText)
					onDismissRequest()
				},
			) {
				Text(text = stringResource(id = R.string.update))
			}
		}
	}
}
