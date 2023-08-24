package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetButton2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetButtonGrid2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.tags.TagsActivity
import com.syncodec.graphite.utils.Extra


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun MenuBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = true,
	onDismissRequest: () -> Unit = { },
) {
	val context = LocalContext.current

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		GenericBottomSheetSkeleton2(
			title = "Menu",
		) {
			GenericBottomSheetButtonGrid2 {
				item {
					GenericBottomSheetButton2(
						icon = R.drawable.ic_file,
						text = "Attachments",
						contentDescription = "Attachments",
					) {
						onDismissRequest()
						Intent(context, AttachmentActivity::class.java).apply {
							putExtra(Extra.Companion.Extra.ShowAll.name, true)
							context.startActivity(this)
						}
					}
				}
				item {
					GenericBottomSheetButton2(
						icon = R.drawable.ic_tag,
						text = "Tags",
						contentDescription = "Tags",
					) {
						onDismissRequest()
						Intent(context, TagsActivity::class.java).apply {
							context.startActivity(this)
						}
					}
				}
				item {
					GenericBottomSheetButton2(
						icon = R.drawable.ic_setting,
						text = "Settings",
						contentDescription = "Settings",
					) {
						onDismissRequest()
						context.startActivity(Intent(context, SettingsActivity::class.java))
					}
				}
			}
		}
	}
}
