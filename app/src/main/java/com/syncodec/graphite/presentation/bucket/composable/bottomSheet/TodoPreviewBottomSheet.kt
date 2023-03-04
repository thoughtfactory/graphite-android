package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTextField
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTextFieldDefaults
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.ui.IconButtonSize
import io.realm.kotlin.types.RealmUUID
import org.koin.androidx.compose.koinViewModel


@Preview
@Composable
fun TodoPreviewBottomSheet(
	previewBucketItemObjectId : RealmUUID? = null,
	closeSheet : () -> Unit = {}
) {
	val context = LocalContext.current

	val viewModel : BucketBottomSheetViewModel = koinViewModel()

	val previewBucketItemObject by viewModel.previewBucketItemObject.collectAsState()
	var todoText by remember { mutableStateOf("") }

	var currentState by remember { mutableStateOf(0) }
	val stateList = listOf(
		StateData(
			title = "Todo",
			icon = R.drawable.ic_todo,
			stateTint = MaterialTheme.colorScheme.primary
		),
		StateData(
			title = "Doing",
			icon = R.drawable.ic_clock,
			stateTint = MaterialTheme.colorScheme.primary
		),
		StateData(
			title = "Done",
			icon = R.drawable.ic_done,
			stateTint = MaterialTheme.colorScheme.primary
		),
	)

	LaunchedEffect(key1 = previewBucketItemObjectId) {
		previewBucketItemObjectId?.let { viewModel.setPreviewBucketItemObject(it) }
	}

	LaunchedEffect(key1 = previewBucketItemObject) {
		todoText = previewBucketItemObject?.title ?: ""
		currentState = try {
			BucketItemState.values().indexOfFirst { it.name == previewBucketItemObject?.state }.let { if (it == - 1) 0 else it }
		} catch (e : Exception) {
			0
		}
	}

	fun onAddTodo(realmUUID : RealmUUID?) {
		viewModel.putTodo(realmUUID = realmUUID, todo = todoText, state = BucketItemState.values().getOrElse(currentState) { BucketItemState.ALPHA })
		todoText = ""
		closeSheet()
	}

	GenericBottomSheet(
		title = "Todo",
		icon = R.drawable.ic_todo,
		enableScroll = true,
	) {

		BottomSheetTextField(
			value = todoText,
			placeholder = "Todo",
			actionButtons = {
				MenuButton(
					icon = R.drawable.ic_add,
					colors = MenuButtonDefaults.menuButtonColorsOnSurface()
				) { }
			},
			keyboardOptions = KeyboardOptions.Default.copy(
				capitalization = KeyboardCapitalization.None,
				autoCorrect = true,
				keyboardType = KeyboardType.Text,
				imeAction = ImeAction.Go
			),
			keyboardActions = KeyboardActions(
				onGo = { onAddTodo(realmUUID = previewBucketItemObjectId) },
				onDone = { onAddTodo(realmUUID = previewBucketItemObjectId) }
			),
			colors = BottomSheetTextFieldDefaults.textFieldColors(),
			onValueChange = { todoText = it },
		)

		Spacer(modifier = Modifier.height(4.dp))

		StateButton(
			stateList = stateList,
			currentState = currentState,
			modifier = Modifier
				.fillMaxWidth()
				.height(36.dp),
		) {
			currentState = it
			onAddTodo(realmUUID = previewBucketItemObjectId)
		}

		Spacer(modifier = Modifier.height(4.dp))

		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			val lockContainerColor by animateColorAsState(
				targetValue = if (previewBucketItemObject?.isLocked == true) MaterialTheme.colorScheme.primary
				else MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f)
			)
			val favouriteContainerColor by animateColorAsState(
				targetValue = if (previewBucketItemObject?.isFavourite == true) MaterialTheme.colorScheme.primary
				else MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.31f)
			)
			val lockContentColor by animateColorAsState(targetValue = if (previewBucketItemObject?.isLocked == true) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
			val favouriteContentColor by animateColorAsState(targetValue = if (previewBucketItemObject?.isFavourite == true) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
			Button(
				onClick = { viewModel.toggleLock(bucketItemObject = previewBucketItemObject) },
				colors = ButtonDefaults.buttonColors(containerColor = lockContainerColor, contentColor = lockContentColor),
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_lock_close),
					contentDescription = "Lock",
					modifier = Modifier.requiredSize(IconButtonSize)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(text = "Lock")
			}

			Spacer(modifier = Modifier.width(8.dp))

			Button(
				onClick = { viewModel.toggleFavourite(bucketItemObject = previewBucketItemObject) },
				colors = ButtonDefaults.buttonColors(containerColor = favouriteContainerColor, contentColor = favouriteContentColor),
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.weight(1f)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_favourite),
					contentDescription = "Favorite",
					modifier = Modifier.requiredSize(IconButtonSize)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(text = "Favourite")
			}
		}

		Button(
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.errorContainer,
				contentColor = MaterialTheme.colorScheme.onErrorContainer,
			),
			shape = MaterialTheme.shapes.medium,
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				if (previewBucketItemObject != null) viewModel.deleteBucketItem(realmUUIDList = previewBucketItemObject?.id?.let { listOf(it) } ?: listOf())
				else Toast.makeText(context, "Error deleting the item", Toast.LENGTH_SHORT).show()
				closeSheet()
			}
		) {
			Text(text = "Delete")
		}
	}
}

@Composable
private fun ColumnScope.Thumbnail(
	thumbnail : Bitmap?
) {
	val context = LocalContext.current

	this.apply {
		thumbnail?.let {
			AsyncImage(
				model = ImageRequest.Builder(context)
					.data(it)
					.build(),
				placeholder = null,
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(0.dp, 128.dp)
					.clip(MaterialTheme.shapes.medium)
			)
			Spacer(modifier = Modifier.height(6.dp))
		}
	}
}
