package com.syncodec.graphite.presentation.note2.composable.bar.editor.bottomSheet

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.toColor


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ColorBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = false,
	onDismissRequest: () -> Unit = { },
	title: String = "Text color",
	color: String? = null,
	onSetColor: (Color) -> Unit = {},
	onUnSetColor: () -> Unit = {},
	onExtendSelection: () -> Unit = {},
) {
	val context = LocalContext.current

	var selectedColor by remember { mutableStateOf<Color?>(null) }
	LaunchedEffect(key1 = color) { selectedColor = color?.toColor() }

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = title,
		) {

			ColorGrid(
				selectedColor = selectedColor,
				onSelectColor = { selectedColor = it }
			)

			Spacer(modifier = Modifier.height(8.dp))

			OutlinedButton(
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier.fillMaxWidth(),
				onClick = onExtendSelection
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_extend_selection),
					contentDescription = "Extend selection",
					modifier = Modifier.requiredSize(IconButtonSize)
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
					onClick = {
						onUnSetColor()
						Toast.makeText(context, "Color removed", Toast.LENGTH_SHORT).show()
					},
				) {
					Text(text = "Unset")
				}

				Spacer(modifier = Modifier.width(4.dp))

				Button(
					shape = MaterialTheme.shapes.medium,
					modifier = Modifier.weight(1f),
					onClick = {
						selectedColor?.let {
							onSetColor(it)
						} ?: Toast.makeText(context, "No color selected", Toast.LENGTH_SHORT).show()
					},
				) {
					Text(text = "Set")
				}
			}
		}
	}
}


@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
private fun ColorGrid(
	selectedColor: Color? = null,
	onSelectColor: (Color) -> Unit = {}
) {
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

	FlowRow(
		modifier = Modifier.fillMaxWidth()
	) {
		colorList.forEach {

			val borderColor by animateColorAsState(targetValue = if (selectedColor == it) MaterialTheme.colorScheme.onBackground else Color.Transparent, label = "borderColor_animation")

			Box(
				modifier = Modifier
					.requiredSize(40.dp)
					.padding(4.dp)
					.background(it, MaterialTheme.shapes.small)
					.clip(MaterialTheme.shapes.small)
					.border(2.dp, borderColor, MaterialTheme.shapes.small)
					.clickable { onSelectColor(it) }
			)
		}
	}
}
