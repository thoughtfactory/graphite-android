package com.syncodec.graphite.presentation.main.composable.buildingBlock.notebook

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.decodeBase64ToBitmap
import com.syncodec.graphite.utils.getInverseBWColor


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotebookCard(
	title: String?,
	color: Color?,
	thumbnail: String?,
	isSelected: Boolean,
	onClick: (() -> Unit)?,
	onLongClick: (() -> Unit)?
) {
	val borderColor by animateColorAsState(targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
	val scale by animateFloatAsState(targetValue = if (isSelected) 0.895f else 1f)

	Box(
		modifier = Modifier
			.padding(16.dp)
			.border(BorderStroke(2.dp, borderColor), RoundedCornerShape(4.dp, 32.dp, 32.dp, 4.dp))
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.aspectRatio(0.75f)
				.background(color ?: MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp, 32.dp, 32.dp, 4.dp))
				.clip(RoundedCornerShape(4.dp, 32.dp, 32.dp, 4.dp))
				.combinedClickable(
					enabled = onClick != null || onLongClick != null,
					onClick = { onClick?.invoke() },
					onLongClick = { onLongClick?.invoke() }
				)
				.graphicsLayer {
					this.scaleX = scale
					this.scaleY = scale
				}
		) {
			thumbnail?.decodeBase64ToBitmap()?.let {
				Image(
					bitmap = it.asImageBitmap(),
					contentDescription = title,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize()
				)
			}

			if (thumbnail != null) {
				Box(
					modifier = Modifier
						.fillMaxSize()
						.background(Color.Black.copy(alpha = 0.17f))
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
						text = title ?: "Untitled",
						style = MaterialTheme.typography.titleLarge,
						color = color?.getInverseBWColor() ?: Color.White,
						textAlign = TextAlign.Start,
						maxLines = 1,
						modifier = Modifier.fillMaxWidth(),
					)
				}
			}
		}
	}
}
