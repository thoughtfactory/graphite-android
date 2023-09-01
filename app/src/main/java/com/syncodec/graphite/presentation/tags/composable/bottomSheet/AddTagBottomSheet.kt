package com.syncodec.graphite.presentation.tags.composable.bottomSheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.button.CancelButton
import com.syncodec.graphite.presentation.common.dialog.ColorPickerDialog
import com.syncodec.graphite.utils.getInverseBWColor
import com.syncodec.graphite.utils.getRandomColor
import com.syncodec.graphite.utils.toHexString


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AddTagBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = true,
	onDismissRequest: () -> Unit = { },
	tagText: String = "",
	onChangeTagText: (String) -> Unit = { },
	onCreateTag: (String, Color) -> Unit = { _, _ -> }
) {

	var selectedColor by remember { mutableStateOf(getRandomColor()) }

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = stringResource(id = R.string.new_tag)
		) {
			OutlinedTextField(
				value = tagText,
				shape = MaterialTheme.shapes.medium,
				onValueChange = onChangeTagText,
				label = { Text(text = stringResource(id = R.string.tag)) },
				placeholder = { Text(text = stringResource(id = R.string.add_tag)) },
				trailingIcon = { CancelButton { onChangeTagText("") } },
				maxLines = 1,
				singleLine = true,
				modifier = Modifier.fillMaxWidth()
			)

			Spacer(modifier = Modifier.height(16.dp))

			ColorPicker(
				selectedColor = selectedColor,
				onSelectColor = { selectedColor = it }
			)

			Spacer(modifier = Modifier.height(12.dp))

			Button(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.fillMaxWidth(),
				onClick = { onCreateTag(tagText, selectedColor) }
			) {
				Text(text = stringResource(id = R.string.create))
			}
		}
	}
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
private fun ColorPicker(
	selectedColor: Color = getRandomColor(),
	onSelectColor: (Color) -> Unit = {},
) {

	var isCustomColorPickerVisible by remember { mutableStateOf(false) }

	val containerColor by animateColorAsState(targetValue = selectedColor, label = "containerColor_animation")
	val contentColor by animateColorAsState(targetValue = selectedColor.getInverseBWColor(), label = "containerColor_animation")

	val colorList = remember {
		listOf(
			Color(0xFFE57373),
			Color(0xFFF06292),
			Color(0xFFBA68C8),
			Color(0xFF9575CD),
			Color(0xFF7986CB),
			Color(0xFF64B5F6),
			Color(0xFF4FC3F7),
			Color(0xFF4DD0E1),
			Color(0xFF4DB6AC),
			Color(0xFF81C784),
			Color(0xFFAED581),
			Color(0xFFDCE775),
			Color(0xFFFFD54F),
			Color(0xFFFFB74D),
			Color(0xFFFF8A65),
			Color(0xFFA1887F),
			Color(0xFF90A4AE),
			Color(0xFFE0E0E0),
			Color(0xFFBDBDBD),
			Color(0xFF9E9E9E),
			Color(0xFF757575),
			Color(0xFF616161),
			Color(0xFF424242),
			Color(0xFF000000),
		)
	}

	Column {
		FlowRow(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceAround
		) {
			colorList.forEach { color ->
				Box(
					contentAlignment = Alignment.Center,
					modifier = Modifier
						.requiredSize(40.dp)
						.padding(4.dp)
						.background(color, MaterialTheme.shapes.small)
						.clip(MaterialTheme.shapes.small)
						.clickable { onSelectColor(color) }
				) {
					if (selectedColor == color) {
						Icon(
							painter = painterResource(id = R.drawable.ic_fa_check),
							contentDescription = "Selected color",
							tint = color.getInverseBWColor(),
							modifier = Modifier.requiredSize(16.dp)
						)
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(16.dp))

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth()
		) {
			Text(text = stringResource(id = R.string.custom_color))
			Spacer(modifier = Modifier.weight(1f))
			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.width(96.dp)
					.height(40.dp)
					.background(containerColor, MaterialTheme.shapes.medium)
					.clip(MaterialTheme.shapes.medium)
					.clickable { isCustomColorPickerVisible = true }
			) {
				Text(
					text = selectedColor.toHexString(),
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
					color = contentColor
				)
			}
		}
	}

	ColorPickerDialog(
		isDialogVisible = isCustomColorPickerVisible,
		onDismissRequest = { isCustomColorPickerVisible = false },
		onSelectColor = {
			isCustomColorPickerVisible = false
			onSelectColor(it)
		}
	)
}
