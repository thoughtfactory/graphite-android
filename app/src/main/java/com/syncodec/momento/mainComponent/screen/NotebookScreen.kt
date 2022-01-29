package com.syncodec.momento.mainComponent.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.MainActivity
import com.syncodec.momento.R
import com.syncodec.momento.database.notebook.Notebook
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.MainTopBar
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import compose.icons.TablerIcons
import compose.icons.tablericons.Plus
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun NotebookScreen() {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp
	val screenHeight = configuration.screenHeightDp.dp

	val viewModel: MainViewModel = viewModel()
	val scope = rememberCoroutineScope()

	val notebookList = viewModel.notebookRepository.notebookListLiveData.observeAsState()

	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		viewModel.mainActivityState.bottomSheetType.value = bottomSheetType
		scope.launch {
			viewModel.mainActivityState.bottomSheetState.show()
		}
	}

	Column {
		MainTopBar(openSheet = openSheet)
		if (notebookList.value?.isNotEmpty() == true) {
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
								.padding(12.dp, 16.dp, 12.dp, 4.dp),
							notebook = notebook
						)
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

@Composable
private fun NotebookCard(
	modifier: Modifier,
	notebook: Notebook,
) {
	Card(
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		backgroundColor = MaterialTheme.colorScheme.primaryContainer,
		modifier = modifier
			.alpha(0.71f)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(12.dp)
		) {
			Text(
				text = notebook.title,
				style = MaterialTheme.typography.bodyLarge,
			)
		}
	}

}
