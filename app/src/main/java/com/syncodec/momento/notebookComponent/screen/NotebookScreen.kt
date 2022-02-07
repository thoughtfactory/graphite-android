package com.syncodec.momento.notebookComponent.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.insets.navigationBarsPadding
import com.syncodec.momento.R
import com.syncodec.momento.database.notebook.Chapter
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.notebookComponent.NotebookViewModel
import com.syncodec.momento.notebookComponent.miscellaneous.NotebookTopBar
import com.syncodec.momento.notebookComponent.modalBottomSheet.BottomSheetType
import compose.icons.TablerIcons
import compose.icons.tablericons.Pencil
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NotebookScreen() {
	val viewModel: NotebookViewModel = viewModel()
	val scope = rememberCoroutineScope()

	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		viewModel.notebookActivityState.bottomSheetType.value = bottomSheetType
		scope.launch {
			viewModel.notebookActivityState.bottomSheetState.show()
		}
	}

	val status by viewModel.status
	val notebook = viewModel.notebook

	Scaffold(
		topBar = { NotebookTopBar(openSheet = {}) },
		floatingActionButton = {
			FloatingActionButton(
				onClick = {
					openSheet(BottomSheetType.NewNoteBottomSheet)
				},
				modifier = Modifier
					.navigationBarsPadding(),
			) {
				Icon(imageVector = TablerIcons.Pencil, contentDescription = null)
			}

		}
	) {
		Box(
			modifier = Modifier
				.fillMaxSize(),
			contentAlignment = Alignment.TopCenter
		) {
			when (status) {
				Status.INIT -> {}
				Status.LOADING -> {
					val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))
					LottieAnimation(
						composition = lottieComposition,
						iterations = LottieConstants.IterateForever,
						modifier = Modifier
							.requiredSize(64.dp)
					)
				}
				Status.SUCCESS -> {
					Button(
						onClick = {
							openSheet(BottomSheetType.NewChapterBottomSheet)
						},
						modifier = Modifier
							.align(Alignment.BottomCenter)
					) {
						Text(text = "Add new chapter")
					}
					Column {
						Breadcrumb(
							currentRoute = viewModel.currentRoute,
							notebookTitle = notebook.title
						)
						LazyColumn(
							modifier = Modifier
						) {

							notebook.chapterMap.forEach{(chapterKey, chapter) ->
								if (chapter.notebookRoute == viewModel.currentRoute) {
									item {
										ChapterCard(
											chapter = chapter
										) {
											viewModel.currentRoute.add(chapter.primaryKey)
										}
									}
								}
							}
						}
					}
				}
				Status.ERROR -> {}
			}
		}
	}
}

@Composable
private fun Breadcrumb(
	currentRoute: SnapshotStateList<String>,
	notebookTitle: String
) {
	val viewModel: NotebookViewModel = viewModel()
	val scrollState = rememberScrollState()
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.height(48.dp)
			.padding(12.dp, 0.dp),
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		backgroundColor = Color.Transparent,
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer)
	) {
		Row(
			modifier = Modifier
				.padding(0.dp, 0.dp)
				.horizontalScroll(
					state = scrollState
				),
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.width(8.dp))
			Box(
				modifier = Modifier
					.fillMaxHeight()
					.padding(0.dp, 8.dp)
					.clip(RoundedCornerShape(12.dp))
					.clickable { viewModel.openNotebook() },
				contentAlignment = Alignment.Center
			) {
				Text(
					text = "$notebookTitle /",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onPrimaryContainer,
					textAlign = TextAlign.Center,
					modifier = Modifier
						.padding(4.dp, 4.dp, 0.dp, 0.dp)
				)
			}

			currentRoute.forEach {
				Text(
					text = it,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onPrimaryContainer,
					modifier = Modifier
						.padding(2.dp)
						.clip(RoundedCornerShape(4.dp))
						.clickable { }
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ChapterCard(
	chapter: Chapter,
	onClick: () -> Unit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.height(128.dp)
			.padding(12.dp, 4.dp, 12.dp, 4.dp),
		backgroundColor = Color.Companion.Transparent,
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer),
		shape = RoundedCornerShape(12.dp),
		elevation = 0.dp,
		onClick = { onClick() }
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			Text(
				text = chapter.title,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.primary
			)
		}
	}
}

@Composable
private fun NoteCard() {

}
