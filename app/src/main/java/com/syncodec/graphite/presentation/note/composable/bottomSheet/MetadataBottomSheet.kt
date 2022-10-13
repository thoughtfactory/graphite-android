package com.syncodec.graphite.presentation.note.composable.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.graphite.R
import com.syncodec.graphite.di.Repository
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyCard
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetTitleCard
import com.syncodec.graphite.presentation.note.NoteViewModel
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun MetadataBottomSheet(
	closeSheet: () -> Unit
) {
	val viewModel: NoteViewModel = viewModel()

	val isViewer by viewModel.isViewer

	val id by viewModel.noteId
	val parentChapterId by viewModel.parentChapterId
	val createdTimestamp by viewModel.createdTimestamp
	val modifiedTimestamp by viewModel.modifiedTimestamp
	val title by viewModel.title

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {
		BottomSheetStrip()

		BottomSheetHeader(
			title = "Metadata",
			icon = R.drawable.ic_info
		)

		BottomSheetKeyCard(
			id = id,
			createdTimestamp = createdTimestamp ?: 0,
			modifiedTimestamp = modifiedTimestamp ?: 0
		)

		Spacer(modifier = Modifier.height(8.dp))

		BottomSheetTitleCard(
			title = title,
			placeholder = "Note title"
		) { viewModel.updateTitle(it) }

		Spacer(modifier = Modifier.height(8.dp))

		ParentCard(
			parentChapterId = parentChapterId,
		) {
			viewModel.showChapterSelectorDialog.value = true
			closeSheet()
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParentCard(
	parentChapterId: ObjectId?,
	onClick: () -> Unit
) {
	val scope = rememberCoroutineScope()
	var chapterTitle by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(key1 = parentChapterId) {
		scope.launch(Dispatchers.IO) {
			chapterTitle = parentChapterId?.let { Repository.getChapterTitle(id = it) }
		}
	}

	Card(
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground
		),
		onClick = onClick,
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
	) {
		Row(
			modifier = Modifier
				.padding(12.dp, 8.dp)
				.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(
				modifier = Modifier
			) {
				Text(
					text = "$chapterTitle",
					style = MaterialTheme.typography.headlineMedium,
				)

				Text(
					text = "$parentChapterId",
					style = MaterialTheme.typography.bodyMedium,
				)
			}

			Spacer(modifier = Modifier.width(8.dp))

			Spacer(modifier = Modifier.weight(1f))

			Icon(
				painter = painterResource(id = R.drawable.ic_notebook),
				contentDescription = "Parent chapter",
				modifier = Modifier.requiredSize(32.dp),
				tint = MaterialTheme.colorScheme.onBackground
			)
		}
	}
}
