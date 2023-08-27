package com.syncodec.graphite.presentation.notebook.screen.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.utils.getInverseBWColor
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun Navigator(
	modifier: Modifier = Modifier,
	chapterPath: List<ChapterObjectLite> = listOf(),
	defaultChapterId: RealmUUID? = null,
	showRoot: Boolean = false,
	isVisible: Boolean = true,
	onClickNavigatorChapter: (RealmUUID?) -> Unit = {},
) {
	AnimatedVisibility(
		visible = isVisible, enter = expandVertically(tween(470)),
		exit = shrinkVertically(tween(470)),
		label = "navigatorVisibility_animation",
		modifier = modifier,
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.horizontalScroll(rememberScrollState()),
		) {
			Spacer(modifier = Modifier.width(10.dp))
			if (showRoot) {
				RootNavigatorItem { onClickNavigatorChapter(null) }
				Icon(painter = painterResource(id = R.drawable.ic_caret),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.requiredSize(20.dp)
						.padding(2.dp)
						.graphicsLayer { rotationZ = 90f })
			}
			chapterPath.forEach { chapterObjectLite ->
				NavigatorItem(
					id = chapterObjectLite.id,
					title = chapterObjectLite.title,
					color = chapterObjectLite.color?.let { Color(it) },
					isDefault = chapterObjectLite.id == defaultChapterId,
				) { onClickNavigatorChapter(chapterObjectLite.id) }
				Icon(painter = painterResource(id = R.drawable.ic_caret),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.requiredSize(20.dp)
						.padding(2.dp)
						.graphicsLayer { rotationZ = 90f })
			}
			Spacer(modifier = Modifier.width(10.dp))
		}
	}
}

@Preview
@Composable
private fun NavigatorItem(
	id: RealmUUID = RealmUUID.random(),
	title: String? = null,
	color: Color? = null,
	isDefault: Boolean = false,
	onClick: () -> Unit = {},
) {
	val containerColor = color ?: MaterialTheme.colorScheme.surface
	val contentColor = color?.getInverseBWColor() ?: MaterialTheme.colorScheme.onSurface

	SuggestionChip(
		onClick = onClick,
		label = { Text(text = title ?: id.toString()) },
		icon = if (isDefault) {
			{
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_sparkles),
					contentDescription = "Default Chapter",
					tint = contentColor,
					modifier = Modifier.requiredSize(16.dp)
				)
			}
		} else null,
		colors = SuggestionChipDefaults.suggestionChipColors(
			containerColor = containerColor,
			labelColor = contentColor,
			iconContentColor = contentColor,
		),
		border = null,
		modifier = Modifier.padding(2.dp, 0.dp),
	)
}

@Preview
@Composable
private fun RootNavigatorItem(
	onClick: () -> Unit = {},
) {
	SuggestionChip(
		onClick = onClick,
		label = {
			Icon(
				painter = painterResource(id = R.drawable.ic_fa_home),
				contentDescription = "Root Chapter",
				modifier = Modifier.requiredSize(16.dp)
			)
		},
		colors = SuggestionChipDefaults.suggestionChipColors(
			containerColor = MaterialTheme.colorScheme.surface,
			labelColor = MaterialTheme.colorScheme.onSurface,
		),
		border = null,
		modifier = Modifier.padding(2.dp, 0.dp),
	)
}

