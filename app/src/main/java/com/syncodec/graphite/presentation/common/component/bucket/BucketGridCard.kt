package com.syncodec.graphite.presentation.common.component.bucket

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.BucketType
import com.syncodec.graphite.di.model.local.bucketTypeIconMap
import com.syncodec.graphite.presentation.base.ICON_SIZE
import com.syncodec.graphite.presentation.common.component.composable.DotSeparator
import com.syncodec.graphite.presentation.common.component.composable.HeaderText
import com.syncodec.graphite.presentation.common.component.composable.StateInfo
import com.syncodec.graphite.presentation.common.selectable.SelectableContainer
import com.syncodec.graphite.presentation.common.selectable.SelectableContainerColors
import com.syncodec.graphite.presentation.common.selectable.SelectableContainerDefaults
import com.syncodec.graphite.utils.isTablet


@Preview
@Composable
fun BucketCard(
	modifier: Modifier = Modifier,
	title: String? = null,
	bucketSize: Int = 0,
	bucketType: BucketType = BucketType.BOOK,
	isLocked: Boolean = false,
	isFavourite: Boolean = false,
	selected: Boolean = false,
	isDragging: Boolean = false,
	handle: @Composable () -> Unit = {},
	colors: SelectableContainerColors = SelectableContainerDefaults.selectableContainerColors(),
	onClick: () -> Unit = {},
	onLongClick: () -> Unit = {},
) {
	val scale by animateFloatAsState(
		targetValue = if (isDragging) 1.17f else 1f,
		label = "scale_animation"
	)

	val contentColor by colors.contentColor(selected = selected)

	SelectableContainer(
		shape = MaterialTheme.shapes.small,
		border = BorderStroke(1.dp, contentColor.copy(alpha = 0.31f)),
		selected = selected,
		colors = colors,
		onClick = onClick,
		onLongClick = onLongClick,
		modifier = modifier
			.fillMaxWidth()
			.height(if (isTablet()) 144.dp else 96.dp)
			.graphicsLayer { this.scaleX = scale; this.scaleY = scale }
	) {
		Column(
			verticalArrangement = Arrangement.SpaceBetween,
			horizontalAlignment = Alignment.Start,
			modifier = Modifier
				.fillMaxSize()
				.padding(10.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth()
			) {
				Icon(
					painter = painterResource(id = bucketTypeIconMap.getOrElse(bucketType) { R.drawable.ic_bucket }),
					contentDescription = null,
					modifier = Modifier.requiredSize(ICON_SIZE)
				)

				Spacer(modifier = Modifier.weight(1f))

				StateInfo(
					isFavourite = isFavourite,
					isLocked = isLocked,
				) {
					DotSeparator()
					HeaderText(text = "$bucketSize")
				}
			}

			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth()
			) {
				HeaderText(
					text = title ?: stringResource(id = R.string.untitled),
					fontStyle = if (title.isNullOrEmpty()) FontStyle.Italic else FontStyle.Normal,
					modifier = Modifier.weight(1f)
				)

				Spacer(modifier = Modifier.width(12.dp))

				handle()
			}
		}
	}
}
