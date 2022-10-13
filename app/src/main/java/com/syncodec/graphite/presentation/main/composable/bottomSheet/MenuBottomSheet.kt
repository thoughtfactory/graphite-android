package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonData
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonGrid
import com.syncodec.graphite.presentation.main.MainActivity
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.FunctionPreviewParameter


@Preview
@Composable
fun MenuBottomSheet(
	@PreviewParameter(FunctionPreviewParameter::class) closeSheet : () -> Unit
) {
	val activity : MainActivity = LocalContext.current as MainActivity

	val buttonList : List<BottomSheetButtonData> = listOf(
		BottomSheetButtonData(
			title = "Attachments",
			icon = R.drawable.ic_gallery
		) {
			Intent(activity, AttachmentActivity::class.java).apply {
				putExtra(Extra.Companion.Constant.SHOW_ALL_ATTACHMENTS.name, true)

				activity.startActivity(this)
			}
		},
		BottomSheetButtonData(title = "Tags", icon = R.drawable.ic_hashtag) {},
		BottomSheetButtonData(title = "Settings", icon = R.drawable.ic_settings) {
			activity.startActivity(Intent(activity, SettingsActivity::class.java))
		},
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Menu",
			icon = R.drawable.ic_menu
		)

		BottomSheetButtonGrid(buttonList = buttonList)

		Spacer(modifier = Modifier.height(32.dp))
	}

}
