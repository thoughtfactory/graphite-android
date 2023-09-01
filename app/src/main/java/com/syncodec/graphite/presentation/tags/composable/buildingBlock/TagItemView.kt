package com.syncodec.graphite.presentation.tags.composable.buildingBlock

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.getInverseBWColor


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun TagItemView(
	tagObject: TagObject = TagObject.getRandomInstance(),
	onClick : () -> Unit = {},
	onLongClick : (() -> Unit)? = null,
) {
	val hapticFeedback = LocalHapticFeedback.current

	Column {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(64.dp)
				.combinedClickable(
					onClick = onClick,
					onLongClick = onLongClick?.let {
						{
							hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
							it()
						}
					}
				)
		) {
			Spacer(modifier = Modifier.width(24.dp))
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_tag),
				contentDescription = tagObject.tag,
				modifier = Modifier.requiredSize(IconButtonSize)
			)

			Spacer(modifier = Modifier.width(12.dp))

			Text(
				text = tagObject.tag,
				style = MaterialTheme.typography.bodyMedium,
				modifier = Modifier.weight(1f)
			)

			Spacer(modifier = Modifier.width(12.dp))

			Box(
				contentAlignment = Alignment.Center,
				modifier = Modifier
					.requiredSize(32.dp)
					.background(Color(tagObject.color), MaterialTheme.shapes.small)
			) {
				Text(
					text = tagObject.objectIdList.size.toString(),
					style = MaterialTheme.typography.bodyMedium,
					color = Color(tagObject.color).getInverseBWColor(),
					fontWeight = FontWeight.Bold,
				)
			}

			Spacer(modifier = Modifier.width(24.dp))
		}
		Divider()
	}
}
