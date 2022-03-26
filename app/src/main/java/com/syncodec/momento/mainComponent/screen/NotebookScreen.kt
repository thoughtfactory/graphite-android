package com.syncodec.momento.mainComponent.screen

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.syncodec.momento.MainActivity
import com.syncodec.momento.R
import com.syncodec.momento.database.notebook.NotebookDbEntry
import com.syncodec.momento.konstant.Konstant
import com.syncodec.momento.notebookComponent.NotebookActivity
import compose.icons.TablerIcons
import compose.icons.tablericons.Pencil


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun NotebookScreen(
	notebookList: List<NotebookDbEntry>,
	isSelected: Boolean,
	selectedItemList: List<String>,
	filterTag: List<String>,
	onClick: (MainActivity.Click, Any?) -> Unit
) {
	Crossfade(
		targetState = notebookList.isNotEmpty(),
		modifier = Modifier.fillMaxSize()
	) {
		if (it) {
			LazyVerticalGrid(
				columns = GridCells.Adaptive(144.dp),
				modifier = Modifier
					.fillMaxSize()
					.padding(12.dp, 0.dp),
			) { notebookList.forEach { notebook -> item { NotebookGridCard(notebookDbEntry = notebook){onClick(MainActivity.Click.CLICK_NOTEBOOK, it)} } } }
		} else { NoNotebookCard() }
	}
}

@Composable
private fun NoNotebookCard() {
	Box(
		modifier = Modifier
			.fillMaxSize(),
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
				modifier = Modifier
					.fillMaxWidth(0.5f)
			)

			Spacer(modifier = Modifier.height(24.dp))

			Text(
				text = "The town was paper, but the memories were not.",
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier
					.fillMaxWidth(0.71f)
			)

			Spacer(modifier = Modifier.height(16.dp))

			Text(
				text = "~ John Green, Paper Towns",
				style = MaterialTheme.typography.bodySmall,
				fontStyle = FontStyle.Italic,
				textAlign = TextAlign.End,
				color = MaterialTheme.colorScheme.primary,
				modifier = Modifier
					.fillMaxWidth(0.71f)
			)
		}
	}

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NotebookGridCard(
	notebookDbEntry: NotebookDbEntry,
	onClick: (String) -> Unit
) {
	val context = LocalContext.current
	val activity = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.StartActivityForResult()
	) {
	}

	Card(
		elevation = 8.dp,
		shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 4.dp),
		backgroundColor = if (notebookDbEntry.color != null) Color(notebookDbEntry.color!!) else Color.Unspecified,
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp)
			.aspectRatio(0.75f),
		onClick = {
			Intent(context, NotebookActivity::class.java).apply {
				putExtra(Konstant.Companion.Konstant.PRIMARY_KEY.name, notebookDbEntry.key)
				activity.launch(this)
			}
		}
	) {
		if (notebookDbEntry.color == null) {
//			viewModel.getNotebookImage(notebookKey = notebookDbEntry.key)?.let {
//				Image(
//					bitmap = it,
//					contentDescription = null,
//					contentScale = ContentScale.Crop
//				)
//			}
		}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp)
		) {
			IconButton(
				onClick = {},
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
