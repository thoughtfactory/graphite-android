package com.syncodec.graphite.presentation.note.composable.buildingBlock

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.attachment.AttachmentActivity
import com.syncodec.graphite.presentation.common.attachment.AttachmentPreview
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.ShareButton
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.share
import com.syncodec.graphite.utils.viewExternally
import io.realm.kotlin.types.RealmUUID
import java.io.File


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun AttachmentCarousel(
	modifier: Modifier = Modifier,
	noteId: RealmUUID? = null,
	fileList: List<File> = listOf()
) {
	val context = LocalContext.current

	val pagerState = rememberPagerState { fileList.size }

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Box(
			modifier = Modifier
		) {
			HorizontalPager(
				state = pagerState,
				beyondBoundsPageCount = 3,
				modifier = modifier
			) {
				val file = fileList[it]
				AttachmentPreview(file = file, onClick = { file.viewExternally(context = context) })
			}
			Column {
				Spacer(modifier = Modifier.height(16.dp))
				Row(
					verticalAlignment = Alignment.CenterVertically,
				) {
					Spacer(modifier = Modifier.width(16.dp))
					AttachmentCountButton(
						currentIndex = pagerState.currentPage + 1,
						totalAttachment = pagerState.pageCount,
					)
					Spacer(modifier = Modifier.weight(1f))
					GenericButton(
						icon = R.drawable.ic_fa_file,
					) {
						Intent(context, AttachmentActivity::class.java).apply {
							putExtra(Extra.Companion.Extra.NoteId.name, noteId?.bytes)
							context.startActivity(this)
						}
					}
					Spacer(modifier = Modifier.width(16.dp))
				}
			}
		}
		AttachmentName(
			file = fileList[pagerState.currentPage]
		)
		Spacer(
			modifier = Modifier
				.fillMaxWidth()
				.height(1.dp)
				.background(MaterialTheme.colorScheme.onBackground)
		)
	}
}


@Preview
@Composable
private fun AttachmentName(
	file: File? = File("")
) {
	val context = LocalContext.current

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(48.dp)
			.background(MaterialTheme.colorScheme.background)
			.padding(horizontal = 12.dp, vertical = 8.dp)
	) {
		Text(
			text = file?.name ?: "",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.weight(1f)
		)
		Spacer(modifier = Modifier.width(8.dp))
		ShareButton { file?.share(context = context) }
	}
}

@Preview
@Composable
private fun AttachmentCountButton(
	modifier: Modifier = Modifier,
	currentIndex: Int = 0,
	totalAttachment: Int = 3,
) {
	Box(
		modifier = modifier
			.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.small)
			.padding(horizontal = 12.dp, vertical = 8.dp)
	) {
		Text(
			text = "$currentIndex / $totalAttachment",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold
		)
	}
}

