package com.syncodec.graphite.presentation.note.screen.editorScreen.bottomSheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyValueCard
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.timeStampToPrettyFull
import io.github.esentsov.PackagePrivate
import io.realm.kotlin.types.RealmUUID


@PackagePrivate
@Preview
@Composable
fun MetadataBottomSheet(
	noteId: RealmUUID? = null,
	createdTimestamp: Long? = null,
	modifiedTimestamp: Long? = null,
	parentChapterObject : ChapterObject? = null,
	onClickSelectParentChapter : () -> Unit = {},
) {

	GenericBottomSheet(
		title = "Metadata",
		icon = R.drawable.ic_info,
	) {

		BottomSheetKeyValueCard(
			key = "ID",
			value = noteId?.toString() ?: "Unsaved",
		)
		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetKeyValueCard(
			key = "Created",
			value = createdTimestamp?.timeStampToPrettyFull() ?: "Unsaved",
		)
		Spacer(modifier = Modifier.height(4.dp))

		BottomSheetKeyValueCard(
			key = "Modified",
			value = modifiedTimestamp?.timeStampToPrettyFull() ?: "Unsaved",
		)
		Spacer(modifier = Modifier.height(4.dp))

		ParentCard(
			chapterId = parentChapterObject?.id,
			chapterTitle = parentChapterObject?.title,
			onClick = onClickSelectParentChapter,
		)
	}
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentCard(
	chapterId: RealmUUID? = null,
	chapterTitle: String? = null,
	onClick: () -> Unit = {},
) {
	Card(
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.71f),
			contentColor = MaterialTheme.colorScheme.onSurface
		),
		onClick = onClick,
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
				.padding(12.dp, 8.dp)
				.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = chapterTitle ?: "Chapter Untitled",
					style = MaterialTheme.typography.titleMedium,
					fontStyle = if (chapterTitle == null) FontStyle.Italic else FontStyle.Normal,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)

				Text(
					text = "$chapterId",
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold
				)
			}

			Spacer(modifier = Modifier.width(16.dp))

			Icon(
				painter = painterResource(id = R.drawable.ic_notebook),
				contentDescription = "Parent chapter",
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.requiredSize(IconButtonSize)
			)
			Spacer(modifier = Modifier.width(8.dp))
		}
	}
}
