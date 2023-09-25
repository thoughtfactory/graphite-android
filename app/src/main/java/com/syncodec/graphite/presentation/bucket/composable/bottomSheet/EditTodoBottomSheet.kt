package com.syncodec.graphite.presentation.bucket.composable.bottomSheet

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.button.CheckButton
import com.syncodec.graphite.presentation.common.tab.GenericTabRow
import com.syncodec.graphite.presentation.common.tab.TabItem
import com.syncodec.graphite.presentation.base.FavouriteContainer
import com.syncodec.graphite.presentation.base.ICON_BUTTON_SIZE
import com.syncodec.graphite.presentation.base.LockClosedContainer
import com.syncodec.graphite.presentation.base.secureComposable.LocalIsRepoUnlocked
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun EditTodoBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	bucketItemObject: BucketItemObject? = null,
	onUpdateTitle: (BucketItemObject, String) -> Unit = { _, _ -> },
	onUpdateState: (BucketItemObject, Int) -> Unit = { _, _ -> },
	onToggleFavourite: (BucketItemObject) -> Unit = {},
	onToggleLock: (BucketItemObject) -> Unit = {},
) {
	val context = LocalContext.current

	val isAuthenticated = LocalIsRepoUnlocked.current

	var todoText by remember { mutableStateOf("") }
	var currentState by remember { mutableIntStateOf(0) }

	val isFavourite by remember(bucketItemObject) { derivedStateOf { bucketItemObject?.isFavourite == true } }
	val isLocked by remember(bucketItemObject) { derivedStateOf { bucketItemObject?.isLocked == true } }

	LaunchedEffect(key1 = bucketItemObject) {
//		https://issuetracker.google.com/issues/296211805
//		Temporary delay bug is not fixed
		delay(310)
		todoText = bucketItemObject?.title ?: ""
		currentState = bucketItemObject?.getState() ?: 0
	}

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.add_todo),
		) {
			OutlinedTextField(
				value = todoText,
				shape = MaterialTheme.shapes.medium,
				onValueChange = { todoText = it },
				label = { Text(text = stringResource(id = R.string.title)) },
				placeholder = { Text(text = stringResource(id = R.string.todo)) },
				trailingIcon = if (todoText != bucketItemObject?.title) {
					{
						CheckButton { bucketItemObject?.let { onUpdateTitle(it, todoText) } }
					}
				} else null,
				maxLines = 1,
				singleLine = true,
				keyboardOptions = KeyboardOptions(
					capitalization = KeyboardCapitalization.None,
					autoCorrect = true,
					keyboardType = KeyboardType.Text,
					imeAction = ImeAction.Done,
				),
				keyboardActions = KeyboardActions { bucketItemObject?.let { onUpdateTitle(it, todoText) } },
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(8.dp))

			GenericTabRow(
				tabItemList = listOf(
					TabItem(text = stringResource(id = R.string.todo), icon = R.drawable.ic_fa_bucket_todo) { currentState = 0; bucketItemObject?.let { onUpdateState(it, 0) } },
					TabItem(text = stringResource(id = R.string.doing), icon = R.drawable.ic_fa_clock) { currentState = 1; bucketItemObject?.let { onUpdateState(it, 1) } },
					TabItem(text = stringResource(id = R.string.done), icon = R.drawable.ic_fa_circle_check) { currentState = 2; bucketItemObject?.let { onUpdateState(it, 2) } },
				),
				selectedTabIndex = currentState,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(6.dp))

			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth()
			) {
				Button(
					shape = MaterialTheme.shapes.medium,
					colors = if (isFavourite) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
					border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
					modifier = Modifier.weight(1f),
					onClick = { bucketItemObject?.let { onToggleFavourite(it) } }
				) {
					if (isFavourite) Icon(
						painter = painterResource(id = R.drawable.ic_fa_heart_solid),
						contentDescription = stringResource(id = R.string.toggle_favourite),
						tint = Color.FavouriteContainer,
						modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
					)
					else Icon(
						painter = painterResource(id = R.drawable.ic_fa_heart),
						contentDescription = stringResource(id = R.string.toggle_favourite),
						modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
					)
					Spacer(modifier = Modifier.width(12.dp))
					Text(text = stringResource(id = R.string.favourite))
				}
				Spacer(modifier = Modifier.width(6.dp))
				Button(
					shape = MaterialTheme.shapes.medium,
					colors = if (isLocked) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
					border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
					modifier = Modifier.weight(1f),
					onClick = {
						if (isAuthenticated) bucketItemObject?.let { onToggleLock(it) }
						else Toast.makeText(context, context.getText(R.string.toast_not_authenticated), Toast.LENGTH_SHORT).show()
					}
				) {
					if (isLocked) Icon(
						painter = painterResource(id = R.drawable.ic_fa_lock_close_solid),
						contentDescription = stringResource(id = R.string.toggle_lock),
						tint = Color.LockClosedContainer,
						modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
					)
					else Icon(
						painter = painterResource(id = R.drawable.ic_fa_lock_open),
						contentDescription = stringResource(id = R.string.toggle_lock),
						modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
					)
					Spacer(modifier = Modifier.width(12.dp))
					Text(text = stringResource(id = R.string.lock))
				}
			}
		}
	}
}
