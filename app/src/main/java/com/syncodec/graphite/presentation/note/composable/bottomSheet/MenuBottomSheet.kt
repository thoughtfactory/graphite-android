package com.syncodec.graphite.presentation.note.composable.bottomSheet

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.ExpandableBox
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonData
import com.syncodec.graphite.presentation.common.bottomSheet.bottomSheetButtonGrid.BottomSheetButtonGrid
import com.syncodec.graphite.presentation.common.composable.ProTag
import com.syncodec.graphite.presentation.note.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.note.composable.LocalCompositionContent
import com.syncodec.graphite.presentation.note.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.note.composable.LocalCompositionTitle
import com.syncodec.graphite.presentation.note.composable.LocalOnShareText
import com.syncodec.graphite.presentation.note.composable.dialog.NoteDialogType
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent
import com.syncodec.graphite.utils.LocalCompositionRichTextEditor


@Composable
fun MenuBottomSheet() {
	val title = LocalCompositionTitle.current
	val content = LocalCompositionContent.current

	val richTextEditor = LocalCompositionRichTextEditor.current

	val openDialog = LocalCompositionOpenDialog.current
	val closeBottomSheet = LocalCompositionCloseBottomSheet.current

	var showExportOptions by remember { mutableStateOf(false) }

	val onShareText = LocalOnShareText.current

	val buttonList : List<BottomSheetButtonData> = listOf(
		BottomSheetButtonData(title = "Export", icon = R.drawable.ic_export) { showExportOptions = ! showExportOptions },
		BottomSheetButtonData(title = "Copy", icon = R.drawable.ic_copy) {
			onShareText()
		},
		BottomSheetButtonData(
			title = "Delete",
			icon = R.drawable.ic_delete,
			containerColor = Color.DeleteContainer,
			contentColor = Color.DeleteContent,
		) {
			closeBottomSheet()
			openDialog(NoteDialogType.DELETE, null)
		},
	)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.wrapContentHeight()
			.background(MaterialTheme.colorScheme.surface)
	) {
		BottomSheetStrip()

		BottomSheetHeader(
			title = "Menu",
			icon = R.drawable.ic_menu
		)

		BottomSheetButtonGrid(buttonList = buttonList)

		ExpandableBox(
			isVisible = showExportOptions
		) {
			val containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.71f)
			val contentColor = MaterialTheme.colorScheme.onBackground
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 0.dp)
			) {
				Spacer(modifier = Modifier.height(24.dp))

				Text(
					text = "Export",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold
				)

				Spacer(modifier = Modifier.height(8.dp))

				ExportButton(
					title = "As Text",
					icon = R.drawable.ic_file_text,
					containerColor = containerColor,
					contentColor = contentColor,
				) { richTextEditor.exec("editor.setAndGetData('${title}', ${content}, 'export_text');") }

				ExportButton(
					title = "As PDF",
					icon = R.drawable.ic_file_pdf,
					containerColor = containerColor,
					contentColor = contentColor
				) { richTextEditor.exec("editor.setAndGetData('${title}', ${content}, 'export_pdf');") }

				ExportButton(
					title = "As HTML",
					icon = R.drawable.ic_file_html,
					containerColor = containerColor,
					contentColor = contentColor,
					isProFeature = true
				) { richTextEditor.exec("editor.setAndGetData('${title}', ${content}, 'export_html');") }

				ExportButton(
					title = "As Markdown",
					icon = R.drawable.ic_file_pdf,
					containerColor = containerColor,
					contentColor = contentColor,
					isProFeature = true
				) { richTextEditor.exec("editor.setAndGetData('${title}', ${content}, 'export_markdown');") }

//				ExportButton(
//					title = "As Image",
//					icon = R.drawable.ic_file_image,
//					containerColor = containerColor,
//					contentColor = contentColor
//				) { richTextEditor.exec("editor.getData(\"export_image\");") }

//				ExportButton(
//					title = "Attachments",
//					icon = R.drawable.ic_gallery,
//					containerColor = containerColor,
//					contentColor = contentColor
//				) { richTextEditor.exec("editor.getData(\"export_attachment\");") }
			}
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportButton(
	title : String,
	icon : Int? = null,
	subTitle : String? = null,
	containerColor : Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.47f),
	contentColor : Color = MaterialTheme.colorScheme.onSurface,
	iconColor : Color = MaterialTheme.colorScheme.onSurface,
	isProFeature : Boolean = false,
	enabled : Boolean = true,
	onClick : () -> Unit
) {
	val context = LocalContext.current
	val isPro by BaseApplication.isPro

	Card(
		colors = CardDefaults.cardColors(
			containerColor = containerColor,
			contentColor = contentColor,
			disabledContainerColor = containerColor.copy(alpha = 0.47f),
			disabledContentColor = contentColor.copy(alpha = 0.47f),
		),
		modifier = Modifier.padding(0.dp, 4.dp),
		enabled = enabled,
		onClick = {
			if (isProFeature && ! isPro) Toast.makeText(context, "Join Graphite Pro to access this feature", Toast.LENGTH_SHORT).show()
			else onClick()
		}
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			icon?.let {
				Icon(
					painter = painterResource(id = it),
					contentDescription = title,
					tint = iconColor,
					modifier = Modifier.requiredSize(24.dp)
				)
				Spacer(modifier = Modifier.width(16.dp))
			} ?: Spacer(modifier = Modifier.width(40.dp))

			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.Bold
				)
				subTitle?.let {
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = it,
						style = MaterialTheme.typography.bodySmall,
					)
				}
			}

			if (isProFeature && ! isPro) {
				Spacer(modifier = Modifier.width(16.dp))
				ProTag()
			}

			Spacer(modifier = Modifier.width(16.dp))

			Icon(
				painter = painterResource(id = R.drawable.ic_chevron_right),
				contentDescription = title,
				modifier = Modifier.requiredSize(24.dp)
			)
		}
	}
}
