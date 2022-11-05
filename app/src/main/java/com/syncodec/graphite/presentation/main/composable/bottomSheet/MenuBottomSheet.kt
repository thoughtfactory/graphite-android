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
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonData
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonGrid
import com.syncodec.graphite.presentation.main.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.tags.TagsActivity
import com.syncodec.graphite.utils.Extra


@Preview
@Composable
fun MenuBottomSheet() {
	val context = LocalContext.current

	val closeSheet = LocalCompositionCloseBottomSheet.current

	val buttonList : List<BottomSheetButtonData> = listOf(
		BottomSheetButtonData(
			title = "Attachments",
			icon = R.drawable.ic_gallery
		) {
			closeSheet()
			Intent(context, AttachmentActivity::class.java).apply {
				putExtra(Extra.Companion.Constant.SHOW_ALL_ATTACHMENTS.name, true)
				context.startActivity(this)
			}
		},
		BottomSheetButtonData(title = "Tags", icon = R.drawable.ic_hashtag) {
			closeSheet()
			Intent(context, TagsActivity::class.java).apply {
				context.startActivity(this)
			}
		},
		BottomSheetButtonData(title = "Settings", icon = R.drawable.ic_settings) {
			closeSheet()
			context.startActivity(Intent(context, SettingsActivity::class.java))
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
