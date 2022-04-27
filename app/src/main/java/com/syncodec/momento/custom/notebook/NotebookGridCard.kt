package com.syncodec.momento.custom.notebook

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.momento.database.notebook.NotebookDbEntry

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun NotebookGridCard(
	notebook: NotebookDbEntry,
	isSelected: Boolean,
	onClick: (String) -> Unit,
	onLongClick: (String) -> Unit
) {
	val borderColor by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)

	Card(
		elevation = 8.dp,
		shape = RoundedCornerShape(4.dp, 32.dp, 32.dp, 4.dp),
		backgroundColor = if (notebook.color != null) Color(notebook.color!!) else Color.Unspecified,
		border = BorderStroke(4.dp, borderColor),
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp)
			.aspectRatio(0.75f)
			.clip(RoundedCornerShape(4.dp, 32.dp, 32.dp, 4.dp))
			.combinedClickable(
				onClick = { onClick(notebook.key) },
				onLongClick = { onLongClick(notebook.key) }
			),
	) {
		if (notebook.color == null && notebook.bitmap != null) {
			Image(
				bitmap = notebook.bitmap!!.asImageBitmap(),
				contentDescription = null,
				contentScale = ContentScale.Crop
			)
		}

		Row(
			modifier = Modifier.fillMaxSize()
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
					text = notebook.title,
					style = MaterialTheme.typography.titleLarge,
					color = Color.White.copy(alpha = 0.88f),
					textAlign = TextAlign.Start,
					modifier = Modifier.fillMaxWidth(),
				)
				Text(
					text = if (notebook.notebookSize == 0) "No entries" else if (notebook.notebookSize == 1) "1 entry" else "$notebook.notebookSize entries",
					style = MaterialTheme.typography.bodyMedium,
					color = Color.White,
					textAlign = TextAlign.Start,
					modifier = Modifier.fillMaxWidth(),
				)
			}
		}
	}
}
