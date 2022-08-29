package com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotebookCard(
	title: String,
	color: Int?,
	isSelected: Boolean,
	onClick: (() -> Unit)?,
	onLongClick: (() -> Unit)?
) {
	val borderColor by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)

	Box(
		modifier = Modifier
			.fillMaxSize()
			.aspectRatio(0.75f)
			.padding(16.dp)
			.background(if (color != null) Color(color) else Color.Transparent, RoundedCornerShape(4.dp, 32.dp, 32.dp, 4.dp))
			.clip(RoundedCornerShape(4.dp, 32.dp, 32.dp, 4.dp))
			.combinedClickable(
				enabled = onClick != null || onLongClick != null,
				onClick = { onClick?.invoke() },
				onLongClick = { onLongClick?.invoke() }
			)
	) {
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
					text = title,
					style = MaterialTheme.typography.titleLarge,
					color = Color.White.copy(alpha = 0.88f),
					textAlign = TextAlign.Start,
					modifier = Modifier.fillMaxWidth(),
				)
//				Text(
//					text = if (notebook.notes.isEmpty()) "No entries" else if (notebook.notes.size == 1) "1 entry" else "${notebook.notes.size} entries",
//					style = MaterialTheme.typography.bodyMedium,
//					color = Color.White,
//					textAlign = TextAlign.Start,
//					modifier = Modifier.fillMaxWidth(),
//				)
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotebookCard(
	color: Int?,
	isSelected: Boolean,
	onClick: (() -> Unit)?,
) {
	val selectionColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
		animationSpec = tween(300)
	)

	Column(
		modifier = Modifier,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Box(
			modifier = Modifier
				.width(96.dp)
				.aspectRatio(0.75f)
				.padding(8.dp)
				.background(
					if (color != null) Color(color) else Color.Transparent,
					RoundedCornerShape(4.dp, 24.dp, 24.dp, 4.dp)
				)
				.combinedClickable(
					enabled = onClick != null,
					onClick = { onClick?.invoke() },
				)
		) {
			Row(
				modifier = Modifier.fillMaxSize()
			) {
				Box(
					modifier = Modifier
						.width(12.dp)
						.fillMaxHeight()
						.background(Color.Black.copy(alpha = 0.31f))
				)
			}
		}

		Box(
			modifier = Modifier
				.width(96.dp)
				.height(4.dp)
				.background(selectionColor, RoundedCornerShape(50))
		)
	}
}
