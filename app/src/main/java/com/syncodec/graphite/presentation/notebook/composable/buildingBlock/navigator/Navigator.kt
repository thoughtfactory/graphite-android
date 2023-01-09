package com.syncodec.graphite.presentation.notebook.composable.buildingBlock.navigator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.getInverseBWColor
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun Navigator(
	showRoot : Boolean = true,
	defaultChapterId : RealmUUID? = null,
	@PreviewParameter(ChapterObjectLiteListPreviewParameter::class) chapterPath : List<ChapterObjectLite?> = listOf(),
	onClick : (RealmUUID?) -> Unit = {},
) {
	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		LazyRow(
			modifier = Modifier.fillMaxWidth()
		) {
			item { Spacer(modifier = Modifier.width(12.dp)) }
			if (showRoot) {
				item {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Center,
					) {
						SuggestionChip(
							onClick = { onClick(null) },
							label = {
								Icon(
									painter = painterResource(id = R.drawable.ic_home),
									contentDescription = "Root",
									modifier = Modifier.requiredSize(IconButtonSize)
								)
							},
							shape = MaterialTheme.shapes.medium,
							colors = SuggestionChipDefaults.suggestionChipColors(
								containerColor = MaterialTheme.colorScheme.onBackground,
								labelColor = MaterialTheme.colorScheme.background,
							),
							border = null,
							modifier = Modifier.padding(0.dp)
						)

						Icon(
							painter = painterResource(id = R.drawable.ic_caret),
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onBackground,
							modifier = Modifier
								.requiredSize(IconButtonSize)
								.graphicsLayer { rotationZ = 90f }
						)
					}
				}
			}
			chapterPath
				.reversed()
				.forEach {
					item {
						NavigatorItem(
							title = it?.title ?: it?.id.toString(),
							color = it?.color?.let { it1 -> Color(it1) },
							isDefault = it?.id == defaultChapterId
						) { onClick(it?.id) }
					}
				}
			item { Spacer(modifier = Modifier.width(12.dp)) }
		}
	}
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigatorItem(
	title : String? = null,
	color : Color? = null,
	isDefault : Boolean = false,
	onClick : () -> Unit = {},
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center,
	) {
		SuggestionChip(
			icon = {
				if (isDefault) Icon(
					painter = painterResource(id = R.drawable.ic_sparkle),
					contentDescription = "Default",
					modifier = Modifier.requiredSize(IconButtonSize)
				)
			},
			label = {
				Text(
					text = title ?: "",
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold,
				)
			},
			shape = MaterialTheme.shapes.medium,
			colors = SuggestionChipDefaults.suggestionChipColors(
				containerColor = if (color == null || color == Color.Unspecified) MaterialTheme.colorScheme.surface else color,
				labelColor = if (color == null || color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color.getInverseBWColor(),
				iconContentColor = if (color == null || color == Color.Unspecified) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f) else color.getInverseBWColor()
					.copy(alpha = 0.47f),
			),
			border = null,
			onClick = onClick,
			modifier = Modifier.padding(0.dp)
		)

		Icon(
			painter = painterResource(id = R.drawable.ic_caret),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier
				.requiredSize(IconButtonSize)
				.graphicsLayer { rotationZ = 90f }
		)
	}
}

class ChapterObjectLiteListPreviewParameter : PreviewParameterProvider<List<ChapterObjectLite>> {
	override val values = sequenceOf(
		listOf(
			ChapterObjectLite.getRandomInstance(),
			ChapterObjectLite.getRandomInstance(),
			ChapterObjectLite.getRandomInstance(),
			ChapterObjectLite.getRandomInstance(),
		)
	)
}
