package com.syncodec.graphite.presentation.bucket.composable.buildingBlock

import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import io.realm.kotlin.types.RealmUUID


@Preview
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BucketGridItem(
	id: RealmUUID = RealmUUID.random(),
	title: String? = null,
	thumbnail: String? = null,
	isSelected: Boolean = false,
	onLongClick: () -> Unit = {},
	onClick: () -> Unit = {}
) {
	Column(
		horizontalAlignment = Alignment.Start,
		modifier = Modifier
			.fillMaxSize()
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(0.6666f)
				.combinedClickable(
					onLongClick = onLongClick,
					onClick = onClick,
					interactionSource = remember { MutableInteractionSource() },
					indication = null
				)
		) {
			BucketItemThumbnail(
				id = id,
				thumbnail = thumbnail,
				modifier = Modifier.padding(8.dp)
			)

			androidx.compose.animation.AnimatedVisibility(
				visible = isSelected,
				enter = scaleIn(tween(300)),
				exit = scaleOut(tween(300)),
				modifier = Modifier.align(Alignment.TopEnd)
			) {
				Box(
					modifier = Modifier
						.requiredSize(32.dp)
						.align(Alignment.TopEnd)
						.background(MaterialTheme.colorScheme.background, CircleShape)
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_circle_check),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier
							.requiredSize(24.dp)
							.align(Alignment.Center)
					)
				}
			}
		}
		Text(
			text = title ?: stringResource(id = R.string.untitled),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
			fontStyle = if (title == null) FontStyle.Italic else FontStyle.Normal,
			maxLines = 3,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(horizontal = 4.dp)
		)
	}
}
