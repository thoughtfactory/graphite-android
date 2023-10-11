package com.syncodec.graphite.presentation.note.composable.bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheet2ListButtonDefaults
import com.syncodec.graphite.presentation.common.bottomSheet.genericBottomSheet2.GenericBottomSheetButton2ListButton
import com.syncodec.graphite.presentation.base.ICON_BUTTON_SIZE


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ExportBottomSheet(
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	isBottomSheetVisible: Boolean = true,
	onDismissRequest: () -> Unit = { },
	onClickExportAsTxt: () -> Unit = {},
	onClickExportAsPdf: () -> Unit = {},
	onClickExportAsHtml: () -> Unit = {},
	onClickExportAsJson: () -> Unit = {},
	onClickExportAsMarkdown: () -> Unit = {},
	onClickExportAttachments: () -> Unit = {},
) {

	val colors = GenericBottomSheet2ListButtonDefaults.transparentButtonColors()

	GenericBottomSheet2(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBottomSheetVisible,
		onDismissRequest = onDismissRequest,
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			Text(
				text = "Export",
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.padding(start = 24.dp)
			)
			Spacer(modifier = Modifier.height(12.dp))
			LazyColumn(
				modifier = Modifier.fillMaxWidth()
			) {
				item { GenericBottomSheetButton2ListButton(icon = R.drawable.ic_fa_file_txt, text = "As text", colors = colors, onClick = onClickExportAsTxt) }
				item { GenericBottomSheetButton2ListButton(icon = R.drawable.ic_fa_file_pdf, text = "As pdf", colors = colors, onClick = onClickExportAsPdf) }
				item { GenericBottomSheetButton2ListButton(icon = R.drawable.ic_fa_code, text = "As html", suffixContent = { ExportButtonSuffix() }, colors = colors, onClick = onClickExportAsHtml) }
				item { GenericBottomSheetButton2ListButton(icon = R.drawable.ic_fa_file_json, text = "As json", suffixContent = { ExportButtonSuffix() }, colors = colors, onClick = onClickExportAsJson) }
				item { GenericBottomSheetButton2ListButton(icon = R.drawable.ic_fa_file, text = "As markdown", suffixContent = { ExportButtonSuffix() }, colors = colors, onClick = onClickExportAsMarkdown) }
				item { GenericBottomSheetButton2ListButton(icon = R.drawable.ic_fa_photos, text = "Attachments", colors = colors, onClick = onClickExportAttachments) }
			}
			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}

@Preview
@Composable
private fun ExportButtonSuffix() {
	Icon(
		painter = painterResource(id = R.drawable.ic_fa_pro),
		contentDescription = "Pro",
		tint = MaterialTheme.colorScheme.onBackground,
		modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
	)
}
