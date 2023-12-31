package com.syncodec.graphite.presentation.main.composable.bottomSheet

import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.genericBottomSheet2.BottomSheetActionButton
import com.syncodec.graphite.presentation.common.genericBottomSheet2.GenericBottomSheetSkeleton2
import com.syncodec.graphite.presentation.common.row.SameHeightRowGrid
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
			title = stringResource(id = R.string.menu),
		) {
			SameHeightRowGrid {
				BottomSheetActionButton(
					icon = R.drawable.ic_fa_gallery,
					text = stringResource(id = R.string.attachments),
					contentDescription = stringResource(id = R.string.attachments),
				) {
					Intent(context, AttachmentActivity::class.java).apply {
						putExtra(Extra.Companion.Extra.ShowAll.name, true)
						context.startActivity(this)
					}
					onDismissRequest()
				}
				BottomSheetActionButton(
					icon = R.drawable.ic_fa_tag,
					text = stringResource(id = R.string.tags),
					contentDescription = stringResource(id = R.string.tags),
				) {
					Intent(context, TagsActivity::class.java).apply {
						context.startActivity(this)
					}
					onDismissRequest()
				}
				BottomSheetActionButton(
					icon = R.drawable.ic_fa_settings,
					text = stringResource(id = R.string.settings),
					contentDescription = stringResource(id = R.string.settings),
				) {
					context.startActivity(Intent(context, SettingsActivity::class.java))
					onDismissRequest()
				}
			}
		}
	}
}
