package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedRealmUUIDList
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsNotebookRefreshing
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnRefresh
import com.syncodec.graphite.presentation.main.composable.buildingBlock.NotebookFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookCard
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NotebookScreen(
	notebookList : List<ChapterObject>,
	onClickFab : () -> Unit,
	onClickNotebook : (RealmUUID) -> Unit,
	onLongClickNotebook : (RealmUUID) -> Unit
) {
	val isNotebookRefreshing = LocalCompositionIsNotebookRefreshing.current
	val onRefresh = LocalCompositionOnRefresh.current

	val isSelected = LocalCompositionIsSelected.current
	val selectedRealmUUIDList = LocalCompositionSelectedRealmUUIDList.current

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		floatingActionButton = {
			AnimatedVisibility(
				visible = ! isSelected,
				enter = fadeIn(tween(300)) + scaleIn(tween(300)),
				exit = fadeOut(tween(300)) + scaleOut(tween(300))
			) {
				NotebookFloatingActionButton(isExpanded = true, onClick = onClickFab)
			}
		}
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			SwipeRefresh(
				state = rememberSwipeRefreshState(isRefreshing = isNotebookRefreshing == true),
				onRefresh = onRefresh
			) {
				LazyVerticalGrid(
					columns = GridCells.Adaptive(144.dp),
					horizontalArrangement = Arrangement.Center,
					modifier = Modifier
						.fillMaxSize()
						.padding(12.dp, 0.dp),
				) {
					notebookList.forEach { notebook ->
						item {
							NotebookCard(
								title = notebook.title,
								color = notebook.color?.let { it1 -> Color(it1) },
								thumbnail = notebook.thumbnail,
								isSelected = notebook.id in selectedRealmUUIDList,
								onClick = { onClickNotebook(notebook.id) },
								onLongClick = { onLongClickNotebook(notebook.id) }
							)
						}
					}
					item { Spacer(modifier = Modifier.height(96.dp)) }
					item { Spacer(modifier = Modifier.height(96.dp)) }
				}
			}
		}
	}
}


@Composable
private fun NoNotebookCard() {
	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Column(
			modifier = Modifier.fillMaxWidth(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Spacer(modifier = Modifier.height(8.dp))
			Image(
				painter = painterResource(id = R.drawable.il_reading),
				contentDescription = "No diary entries",
				modifier = Modifier.fillMaxWidth(0.5f)
			)

			Spacer(modifier = Modifier.height(24.dp))

			Text(
				text = "The town was paper, but the memories were not.",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.fillMaxWidth(0.71f)
			)

			Spacer(modifier = Modifier.height(16.dp))

			Text(
				text = "~ John Green, Paper Towns",
				style = MaterialTheme.typography.bodySmall,
				fontStyle = FontStyle.Italic,
				textAlign = TextAlign.End,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier.fillMaxWidth(0.71f)
			)
		}
	}
}
