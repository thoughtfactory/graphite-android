package com.syncodec.momento.mainComponent.screen

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.R
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.notebookComponent.NotebookActivity
import compose.icons.TablerIcons
import compose.icons.tablericons.Pencil
import compose.icons.tablericons.Plus
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun NotebookScreen() {
	val viewModel: MainViewModel = viewModel()
	val scope = rememberCoroutineScope()

	val notebookList = viewModel.notebookRepository.notebookDbEntryListLiveData.observeAsState()

	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		viewModel.mainActivityState.bottomSheetType.value = bottomSheetType
		scope.launch {
			viewModel.mainActivityState.bottomSheetState.show()
		}
	}

	val showCardView = true

	Column(
		modifier = Modifier
	) {

		if (notebookList.value?.isNotEmpty() == true) {
			if (showCardView) {
				LazyVerticalGrid(
					cells = GridCells.Adaptive(144.dp),
					horizontalArrangement = Arrangement.Center,
					verticalArrangement = Arrangement.Center,
					modifier = Modifier
						.fillMaxSize()
						.padding(12.dp, 0.dp)
				) {
					notebookList.value?.forEach { notebook ->
						item {
							NotebookGridCard(
								notebookDbEntry = notebook
							)
						}
					}
				}
			} else {
				LazyColumn(
					modifier = Modifier
						.padding(0.dp, 0.dp, 0.dp, 64.dp)
				) {
					notebookList.value?.forEach { notebook ->
						item {
							NotebookCard(
								modifier = Modifier
									.fillMaxWidth()
									.height(128.dp)
									.padding(12.dp, 0.dp, 12.dp, 4.dp),
								notebookDbEntry = notebook
							)
						}
					}
				}
			}
		} else {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight()
					.padding(16.dp),
				contentAlignment = Alignment.Center
			) {
				Column {
					NoNotebookCard(
						modifier = Modifier
							.fillMaxWidth()
							.height(320.dp)
					) { bottomSheetType ->
						openSheet(bottomSheetType)
					}
					Spacer(modifier = Modifier.height(88.dp))
				}
			}
		}
	}
}

@Composable
private fun NoNotebookCard(
	modifier: Modifier = Modifier,
	openSheet: (BottomSheetType) -> Unit
) {
	Card(
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = 0.47f),
		modifier = modifier
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(12.dp),
		) {
			Image(
				painter = painterResource(id = R.drawable.il_reading),
				contentDescription = null,
				modifier = Modifier
					.requiredSize(192.dp)
			)

			Spacer(modifier = Modifier.height(24.dp))

			OutlinedButton(
				onClick = { openSheet(BottomSheetType.NotebookBottomSheet) },
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer
				)
			) {
				Icon(
					imageVector = TablerIcons.Plus,
					contentDescription = "Add new notebook",
					tint = MaterialTheme.colorScheme.primary
				)

				Spacer(modifier = Modifier.width(16.dp))

				Text(
					text = "Add your first notebook",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.primary
				)
			}
		}
	}

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NotebookCard(
	modifier: Modifier,
	notebookDbEntry: NotebookDbEntry,
) {
	val context = LocalContext.current
	val activity = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartActivityForResult()
	) {
	}

	Card(
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		backgroundColor = MaterialTheme.colorScheme.background,
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.primaryContainer),
		modifier = modifier,
		onClick = {
			Intent(context, NotebookActivity::class.java).apply {
				putExtra(Konstant.Companion.Konstant.PRIMARY_KEY.name, notebookDbEntry.primaryKey)
				activity.launch(this)
			}
		}
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(12.dp)
		) {
			Text(
				text = notebookDbEntry.title,
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onPrimaryContainer
			)
		}
	}

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NotebookGridCard(
	notebookDbEntry: NotebookDbEntry,
) {
	val viewModel: MainViewModel = viewModel()

	Card(
		elevation = 8.dp,
		shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 4.dp),
		backgroundColor = if (notebookDbEntry.color != null) Color(notebookDbEntry.color!!) else Color.Unspecified,
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp)
			.aspectRatio(0.75f),
		onClick = {}
	) {
		if (notebookDbEntry.color == null) {
			viewModel.getNotebookImage(notebookKey = notebookDbEntry.primaryKey)?.let {
				Image(
					bitmap = it,
					contentDescription = null,
					contentScale = ContentScale.Crop
				)
			}
		}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			IconButton(
				onClick = { /*TODO*/ },
				modifier = Modifier
					.requiredSize(16.dp)
					.padding(0.dp)
					.clip(CircleShape)
					.align(Alignment.TopEnd)
			) {
				Icon(
					imageVector = TablerIcons.Pencil,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.background,
					modifier = Modifier
						.requiredSize(24.dp)
						.padding(0.dp)
				)
			}
		}

		Row(
			modifier = Modifier
				.fillMaxSize()
		) {
			Box(
				modifier = Modifier
					.width(16.dp)
					.fillMaxHeight()
					.background(Color.Black.copy(alpha = 0.31f))
			)
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Spacer(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
				)
				Text(
					text = notebookDbEntry.title,
					style = MaterialTheme.typography.titleLarge,
					color = Color.White.copy(alpha = 0.88f),
					textAlign = TextAlign.Start,
					modifier = Modifier
						.fillMaxWidth(),
				)
				Text(
					text = "71 Entries",
					style = MaterialTheme.typography.bodyMedium,
					color = Color.White,
					textAlign = TextAlign.Start,
					modifier = Modifier
						.fillMaxWidth(),
				)
			}
		}
	}
}

@Composable
private fun BreadCrumb() {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.height(48.dp)
			.padding(12.dp, 0.dp)
			.clip(RoundedCornerShape(12.dp)),
		backgroundColor = MaterialTheme.colorScheme.primaryContainer,
		shape = RoundedCornerShape(12.dp)
	) {
		Text(text = "Hello world")
	}
}
