package com.syncodec.graphite.presentation.tags.composable.buildingBlock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.ui.DeleteContainer


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
	onClick: () -> Unit = {},
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 4.dp)
			.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.31f), MaterialTheme.shapes.medium),
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(4.dp)
		) {
			Spacer(modifier = Modifier.width(12.dp))

			Text(
				text = tag.tag,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
			)

			Spacer(modifier = Modifier.weight(1f))

			Spacer(
				modifier = Modifier
					.width(80.dp)
					.height(12.dp)
					.background(Color(tag.color), RoundedCornerShape(25))
			)

			Spacer(modifier = Modifier.width(12.dp))

			MenuButton(
				icon = R.drawable.ic_delete,
				tint = Color.Companion.DeleteContainer,
				onClick = onDelete,
			)

			MenuButton(
				icon = R.drawable.ic_pencil,
				tint = MaterialTheme.colorScheme.onSurface,
				onClick = onClick,
			)
		}
	}
}
