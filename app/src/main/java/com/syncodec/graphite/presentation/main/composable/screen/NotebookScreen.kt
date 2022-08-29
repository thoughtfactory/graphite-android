package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObject
import com.syncodec.graphite.presentation.main.composable.buildingBlock.NotebookFloatingActionButton
import com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook.NotebookCard
import io.realm.kotlin.types.ObjectId


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotebookScreen(
	notebookList: List<ChapterObject>,
	onClickFab: () -> Unit,
	onClickNotebook: (ObjectId) -> Unit,
	onLongClickNotebook: (ObjectId) -> Unit
) {
	Scaffold(
		modifier = Modifier.fillMaxSize(),
		floatingActionButton = {
			NotebookFloatingActionButton(isExpanded = true, onClick = onClickFab)
		}
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
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
							color = notebook.color,
							isSelected = false,
							onClick = { onClickNotebook(notebook.id) },
							onLongClick = { onLongClickNotebook(notebook.id) }
						)
					}
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
