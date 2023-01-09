package com.syncodec.graphite.presentation.bucketItem.composable.dialog

import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionBookKey
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieId
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieImdbId
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionMovieOriginalTitle
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionShowBookInfoDialog
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionShowShowInfoDialog
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvId
import com.syncodec.graphite.presentation.bucketItem.composable.LocalCompositionTvName
import com.syncodec.graphite.presentation.common.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.common.dialog.DeleteDialog
import com.syncodec.graphite.presentation.common.dialog.DialogType


@Composable
fun BucketItemDialog() {

	val movieId = LocalCompositionMovieId.current
	val movieImdbId = LocalCompositionMovieImdbId.current
	val movieOriginalTitle = LocalCompositionMovieOriginalTitle.current

	val tvId = LocalCompositionTvId.current
	val tvName = LocalCompositionTvName.current

	val bookKey = LocalCompositionBookKey.current

	val showShowInfoDialog = LocalCompositionShowShowInfoDialog.current
	val showBookInfoDialog = LocalCompositionShowBookInfoDialog.current
	val showDeleteDialog = LocalCompositionShowDeleteDialog.current

	val onDelete = LocalCompositionOnDelete.current

	val closeDialog = LocalCompositionCloseDialog.current

	ShowInfoDialog(
		showDialog = showShowInfoDialog,
		movieId = movieId,
		tvId = tvId,
		movieImdbId = movieImdbId,
		title = if (movieId == null) tvName else movieOriginalTitle,
	) { closeDialog(DialogType.SHOW_INFO) }

	ShowBookDialog(
		showDialog = showBookInfoDialog,
		bookKey = bookKey,
	) { closeDialog(DialogType.BOOK_INFO) }

	DeleteDialog(
		showDialog = showDeleteDialog,
		message = "Delete ${movieOriginalTitle}?",
		onDismiss = { closeDialog(DialogType.DELETE) },
	) {
		onDelete()
		closeDialog(DialogType.DELETE)
	}
}
