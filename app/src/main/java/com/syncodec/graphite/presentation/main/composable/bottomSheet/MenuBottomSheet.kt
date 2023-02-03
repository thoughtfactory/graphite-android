package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButton
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonGrid
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.tags.TagsActivity
import com.syncodec.graphite.utils.Extra


@Preview
@Composable
fun MenuBottomSheet(
	closeSheet : () -> Unit = { },
) {
	val context = LocalContext.current

	GenericBottomSheet(
		title = "Menu",
		icon = R.drawable.ic_menu,
	) {
		BottomSheetButtonGrid(
			buttonList = listOf(
				{
					BottomSheetButton(title = "Attachments", icon = R.drawable.ic_file) {
						closeSheet()
						Intent(context, AttachmentActivity::class.java).apply {
							putExtra(Extra.Companion.Extra.ShowAll.name, true)
							context.startActivity(this)
						}
					}
				},
				{
					BottomSheetButton(title = "Tags", icon = R.drawable.ic_tag) {
						closeSheet()
						Intent(context, TagsActivity::class.java).apply {
							context.startActivity(this)
						}
					}
				},
				{
					BottomSheetButton(title = "Settings", icon = R.drawable.ic_setting) {
						closeSheet()
						context.startActivity(Intent(context, SettingsActivity::class.java))
					}
				},
			)
		)
	}
}
