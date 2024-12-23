package com.syncodec.graphite.presentation.tags.composable.buildingBlock

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.button.MenuButtonDefaults
import com.syncodec.graphite.presentation.ui.ICON_SIZE
import com.syncodec.graphite.utils.getInverseBWColor


class TagObjectPreviewParameter: PreviewParameterProvider<TagObject> {
	override val values = sequenceOf(
		TagObject.getRandomInstance(),
		TagObject.getRandomInstance(),
		TagObject.getRandomInstance(),
		TagObject.getRandomInstance(),
	)
}

@Preview
@Composable
fun TagItem(
	@PreviewParameter(TagObjectPreviewParameter::class) tag: TagObject,
	onDelete: () -> Unit = {},
	onEdit: () -> Unit = {},
	onClick : () -> Unit = {},
) {
	val containerColor by animateColorAsState(targetValue =Color(tag.color) )
	val contentColor by animateColorAsState(targetValue = Color(tag.color).getInverseBWColor() )

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 2.dp),
	) {
		Icon(
			painter = painterResource(id = R.drawable.ic_tag),
			contentDescription = "Tag Icon",
			tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
		)

		Spacer(modifier = Modifier.width(12.dp))

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.weight(1f)
				.height(ICON_SIZE * 2)
				.background(containerColor, MaterialTheme.shapes.medium)
				.clip(MaterialTheme.shapes.medium)
				.clickable { onClick() },
		) {
			Spacer(modifier = Modifier.width(12.dp))

			Text(
				text = tag.tag,
				style = MaterialTheme.typography.bodyMedium,
				color = contentColor,
			)

			Spacer(modifier = Modifier.weight(1f))

			Spacer(modifier = Modifier.width(20.dp))

			StateInfo(
				noteCount = tag.objectIdList.size
			)

			Spacer(modifier = Modifier.width(12.dp))
		}

		Spacer(modifier = Modifier.width(2.dp))

		MenuButton(
			icon = R.drawable.ic_delete,
			colors = MenuButtonDefaults.deleteButtonColors(),
			onClick = onDelete
		)

		MenuButton(
			icon = R.drawable.ic_pencil,
			colors = MenuButtonDefaults.menuButtonColorsOnSurface(),
			onClick = onEdit,
		)
	}
}

@Preview
@Composable
private fun StateInfo(
	noteCount : Int = 71,
) {
	Box(
		modifier = Modifier.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.small)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(8.dp, 4.dp)
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_note),
				contentDescription = "Note count",
				tint = Color(0xFF7986CB),
				modifier = Modifier.requiredSize(14.dp)
			)
			Text(
				text = " · $noteCount",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Bold,
				maxLines = 1,
			)
		}
	}
}
