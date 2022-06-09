package com.syncodec.graphite.bucketComponent.modalBottomSheet

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.bucketComponent.BucketActivity
import com.syncodec.graphite.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.custom.button.MenuBottomSheetButton
import com.syncodec.graphite.custom.button.MenuBottomSheetButtonData
import com.syncodec.graphite.database.bucketItem.BucketItemType
import com.syncodec.graphite.miscellaneous.TimeUtils
import java.util.*
import java.util.concurrent.TimeUnit


@Composable
fun MenuBottomSheet(
	createdTimestamp: Long,
	modifiedTimestamp: Long,
	bucketTitle: String,
	bucketItemType: BucketItemType,
	alphaCount: Int,
	betaCount: Int,
	gammaCount: Int,
	totalCount: Int,
	onAction: (BucketActivity.Action, Any?) -> Unit
) {
	val menuBottomSheetButtonDataList: List<MenuBottomSheetButtonData?> = listOf(
//		MenuBottomSheetButtonData(
//			title = "Delete",
//			icon = R.drawable.ic_trash,
//			highlight = false
//		) { },
//		MenuBottomSheetButtonData(
//			title = "Export",
//			icon = R.drawable.ic_export,
//			highlight = false
//		) { },
//		MenuBottomSheetButtonData(
//			title = "Share",
//			icon = R.drawable.ic_share,
//			highlight = false
//		) { },
//		MenuBottomSheetButtonData(
//			title = "Edit",
//			icon = R.drawable.ic_pencil,
//			highlight = false
//		) { onAction(BucketActivity.Action.EDIT_BUCKET, null) }
	)

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color = MaterialTheme.colorScheme.surface,
		modifier = Modifier.heightIn(360.dp),
	) {
		Column(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			BottomSheetStrip()

			BottomSheetHeader(
				title = "Menu",
				icon = R.drawable.ic_menu
			)

			TitleCard(
				title = bucketTitle
			) { onAction(BucketActivity.Action.UPDATE_BUCKET, it) }

			Spacer(modifier = Modifier.height(8.dp))

			TimestampCard(
				createdTimestamp = createdTimestamp,
				modifiedTimestamp = modifiedTimestamp
			)

			Spacer(modifier = Modifier.height(8.dp))

			DataCard(
				bucketItemType = bucketItemType,
				alphaCount = alphaCount,
				betaCount = betaCount,
				gammaCount = gammaCount,
				totalCount = totalCount,
			)

			Spacer(modifier = Modifier.height(12.dp))

			LazyVerticalGrid(
				columns = GridCells.Adaptive(72.dp),
				modifier = Modifier.padding(24.dp, 0.dp),
			) {
				itemsIndexed(menuBottomSheetButtonDataList) { _, menuBottomSheetButtonData ->
					MenuBottomSheetButton(menuBottomSheetButtonData)
				}
			}

			Spacer(modifier = Modifier.height(32.dp))
		}
	}
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TitleCard(
	title: String,
	onUpdateTitle: (String) -> Unit
) {
	val keyboardController = LocalSoftwareKeyboardController.current
	val focusRequester = remember { FocusRequester() }

	var isEditing by remember { mutableStateOf(false) }
	var newTitle by remember { mutableStateOf(title) }

	LaunchedEffect(key1 = isEditing) {
		if (isEditing) {
			focusRequester.requestFocus()
		}
	}

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
			.clip(RoundedCornerShape(12.dp))
			.background(MaterialTheme.colorScheme.background),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(12.dp))

		if (isEditing) {
			BasicTextField(
				value = newTitle,
				onValueChange = { newTitle = it },
				singleLine = true,
				keyboardOptions = KeyboardOptions.Default,
				keyboardActions = KeyboardActions.Default,
				cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
				textStyle = MaterialTheme.typography.titleMedium,
				modifier = Modifier.focusRequester(focusRequester),
				decorationBox = { innerTextField ->
					innerTextField()
				}
			)
		} else {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier
			)
		}

		Spacer(modifier = Modifier.weight(1f))

		Crossfade(targetState = isEditing) {
			if (it) {
				IconButton(
					onClick = {
						isEditing = false
						onUpdateTitle(newTitle)
						keyboardController?.hide()
					}
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_check),
						contentDescription = "Update bucket title",
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.requiredSize(20.dp)
					)
				}
			} else {
				IconButton(
					onClick = {
						isEditing = true
						keyboardController?.show()
					}
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_pencil),
						contentDescription = "Edit bucket title",
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.requiredSize(20.dp)
					)
				}
			}
		}
	}
}

@Composable
fun TimestampCard(
	createdTimestamp: Long,
	modifiedTimestamp: Long
) {
	Card(
		elevation = 0.dp,
		backgroundColor = MaterialTheme.colorScheme.background,
		shape = RoundedCornerShape(12.dp),
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "Created on : ",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground
				)
				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = TimeUtils.timeStampToPrettyFull(createdTimestamp),
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onBackground
				)
			}

			Spacer(modifier = Modifier.height(8.dp))

			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "Last edited on : ",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground
				)
				Spacer(modifier = Modifier.weight(1f))

				val calendar = Calendar.getInstance()
				val days = TimeUnit.MILLISECONDS.toDays(calendar.timeInMillis - modifiedTimestamp)
				val hours = TimeUnit.MILLISECONDS.toHours(calendar.timeInMillis - modifiedTimestamp)
				val minutes =
					TimeUnit.MILLISECONDS.toMinutes(calendar.timeInMillis - modifiedTimestamp)

				val modifiedTimestampPretty = if (days in 1..7) {
					"About $days days ago"
				} else if (days > 7) {
					TimeUtils.timeStampToPrettyFull(modifiedTimestamp)
				} else {
					if (hours in 1..24) {
						"About $hours hour${if (hours == 1L) "" else "s"} ago"
					} else {
						if (minutes in 1..60) {
							"About $minutes minute${if (minutes == 1L) "" else "s"} ago"
						} else {
							"About few seconds ago"
						}
					}
				}

				Text(
					text = modifiedTimestampPretty,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onBackground
				)
			}
		}
	}
}

@Composable
fun DataCard(
	bucketItemType: BucketItemType,
	alphaCount: Int,
	betaCount: Int,
	gammaCount: Int,
	totalCount: Int,
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Row(
			Modifier
				.fillMaxWidth()
				.height(48.dp)
		) {
			DataButton(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.background),
				icon = R.drawable.ic_state,
				title = "All",
				count = totalCount
			)

			Spacer(modifier = Modifier.width(8.dp))

			DataButton(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.background),
				icon = R.drawable.ic_clock,
				title = when (bucketItemType) {
					BucketItemType.TODO -> "To Do"
					BucketItemType.BOOKS -> "To Read"
					BucketItemType.SHOWS -> "To Watch"
				},
				count = alphaCount
			)
		}

		Spacer(modifier = Modifier.height(8.dp))

		Row(
			Modifier
				.fillMaxWidth()
				.height(48.dp)
		) {
			DataButton(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.background),
				icon = when (bucketItemType) {
					BucketItemType.TODO -> R.drawable.ic_todo
					BucketItemType.BOOKS -> R.drawable.ic_book
					BucketItemType.SHOWS -> R.drawable.ic_show
				},
				title = when (bucketItemType) {
					BucketItemType.TODO -> "Doing"
					BucketItemType.BOOKS -> "Reading"
					BucketItemType.SHOWS -> "Watching"
				},
				count = betaCount
			)

			Spacer(modifier = Modifier.width(8.dp))

			DataButton(
				modifier = Modifier
					.fillMaxHeight()
					.weight(1f)
					.clip(RoundedCornerShape(12.dp))
					.background(MaterialTheme.colorScheme.background),
				icon = R.drawable.ic_done,
				title = when (bucketItemType) {
					BucketItemType.TODO -> "Done"
					BucketItemType.BOOKS -> "Read"
					BucketItemType.SHOWS -> "Watched"
				},
				count = gammaCount
			)
		}

	}
}

@Composable
private fun DataButton(
	modifier: Modifier,
	icon: Int,
	title: String,
	count: Int
) {
	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center
	) {
		Icon(
			painter = painterResource(id = icon),
			contentDescription = title,
			tint = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.requiredSize(20.dp)
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(
			text = "$count",
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onBackground
		)
	}
}
