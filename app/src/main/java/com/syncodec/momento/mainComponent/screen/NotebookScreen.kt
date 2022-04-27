package com.syncodec.momento.mainComponent.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.MainActivity
import com.syncodec.momento.R
import com.syncodec.momento.custom.notebook.NotebookGridCard
import com.syncodec.momento.database.notebook.NotebookDbEntry


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun NotebookScreen(
	notebookListFlow: List<NotebookDbEntry>,
	isSelected: Boolean,
	selectedItemList: List<String>,
	filterTag: List<String>,
	onAction: (MainActivity.Action, Any?) -> Unit
) {
	Crossfade(
		targetState = notebookListFlow.isNotEmpty(),
		modifier = Modifier.fillMaxSize()
	) {
		if (it) {
			LazyVerticalGrid(
				columns = GridCells.Adaptive(144.dp),
				horizontalArrangement = Arrangement.Center,
				modifier = Modifier
					.fillMaxSize()
					.padding(12.dp, 0.dp),
			) {
				notebookListFlow.forEach {
					item {
						NotebookGridCard(
							notebook = it,
							isSelected = it.key in selectedItemList,
							onClick = { onAction(MainActivity.Action.CLICK_NOTEBOOK, it) },
							onLongClick = { onAction(MainActivity.Action.LONG_CLICK_NOTEBOOK, it) }
						)
					}
				}
			}
		} else {
			NoNotebookCard()
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
