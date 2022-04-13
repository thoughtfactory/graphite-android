package com.syncodec.momento.custom.notebook

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.momento.database.notebook.NotebookDbEntry

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NotebookGridCard(
	notebook: NotebookDbEntry,
	notebookSize: Int,
	onClick: (String) -> Unit
) {
	Card(
		elevation = 8.dp,
		shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 4.dp),
		backgroundColor = if (notebook.color != null) Color(notebook.color!!) else Color.Unspecified,
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp)
			.aspectRatio(0.75f),
		onClick = { onClick(notebook.key) }
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
				Spacer(modifier = Modifier
					.fillMaxWidth()
					.weight(1f))
				Text(
					text = notebook.title,
					style = MaterialTheme.typography.titleLarge,
					color = Color.White.copy(alpha = 0.88f),
					textAlign = TextAlign.Start,
					modifier = Modifier.fillMaxWidth(),
				)
				Text(
					text = if (notebookSize == 0) "No entries" else if (notebookSize == 1) "1 entry" else "$notebookSize entries",
					style = MaterialTheme.typography.bodyMedium,
					color = Color.White,
					textAlign = TextAlign.Start,
					modifier = Modifier.fillMaxWidth(),
				)
			}
		}
	}
}
