package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.custom.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.custom.bottomSheet.bottomSheetButtonGrid.BottomSheetButton
import com.syncodec.graphite.presentation.custom.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonData
import com.syncodec.graphite.presentation.main.MainActivity
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.utils.FunctionPreviewParameter


@Preview
@Composable
fun MenuBottomSheet(
	@PreviewParameter(FunctionPreviewParameter::class) closeSheet: () -> Unit
) {
	val activity: MainActivity = LocalContext.current as MainActivity

	val buttonList: List<BottomSheetButtonData> = listOf(
		BottomSheetButtonData(title = "Attachments", icon = R.drawable.ic_gallery) {},
		BottomSheetButtonData(title = "Settings", icon = R.drawable.ic_settings) {
			activity.startActivity(Intent(activity, SettingsActivity::class.java))
		},
		BottomSheetButtonData(title = "Attachments", icon = R.drawable.ic_gallery) {},
		BottomSheetButtonData(title = "Attachments", icon = R.drawable.ic_gallery) {},
		BottomSheetButtonData(title = "Attachments", icon = R.drawable.ic_gallery) {},
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(360.dp)
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Menu",
			icon = R.drawable.ic_state
		)

		repeat((buttonList.size / 3) + 1) { i ->
			Row(
				modifier = Modifier.padding(24.dp, 0.dp),
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				repeat(3) {
					val buttonData = buttonList.getOrNull(i * 3 + it)
					if (buttonData != null) {
						BottomSheetButton(
							title = buttonData.title,
							icon = buttonData.icon,
							modifier = Modifier.weight(1f),
							onClick = buttonData.onClick
						)
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}

}
