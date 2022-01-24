package com.syncodec.momento.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.database.bucket.BucketItemType
import com.syncodec.momento.database.diary.DiaryDbEntry
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.miscellaneous.generatePrimaryKey
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun AddDataPopup() {
	val viewModel: MainViewModel = viewModel()

	MaterialTheme {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight(),
			horizontalAlignment = Alignment.End
		) {
			val openDialog = remember { mutableStateOf(false) }

			Button(
				onClick = {
					openDialog.value = true
				}) {
				Text("Add debug data")
			}

			if (openDialog.value) {
				AlertDialog(
					onDismissRequest = { openDialog.value = false },
					title = { Text(text = "Debug data") },
					confirmButton = {
						Button(
							onClick = {
								openDialog.value = false

								for (i in 0..71) {
									DiaryDbEntry(generatePrimaryKey(), abs(Random.nextInt())).apply {
										this.title = "Alphabet"
										this.contentThumbnail = "Hello world"
										viewModel.insertDiary(this)
									}
								}

								viewModel.createNewBucket(BucketItemType.TODO, "todo")
								viewModel.createNewBucket(BucketItemType.BOOKS, "books")
								viewModel.createNewBucket(BucketItemType.MOVIES, "movies")
								viewModel.createNewBucket(BucketItemType.TVSHOWS, "tv shows")
								viewModel.createNewBucket(BucketItemType.MEDIA, "media")
								viewModel.createNewBucket(BucketItemType.LINKS, "links")

							}
						) {
							Text("add")
						}
					},
					dismissButton = {
						Button(
							onClick = {
								openDialog.value = false
								viewModel.deleteAllDiary()
								viewModel.deleteAllBucket()
							}
						) {
							Text("delete")
						}
					}
				)
			}
		}
	}
}
