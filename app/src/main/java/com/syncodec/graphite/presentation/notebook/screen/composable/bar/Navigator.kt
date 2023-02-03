package com.syncodec.graphite.presentation.notebook.screen.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.utils.getInverseBWColor
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun Navigator(
	chapterPath : List<ChapterObjectLite> = listOf(),
	defaultChapterId : RealmUUID? = null,
	showRoot : Boolean = false,
	isVisible : Boolean = true,
	onClickNavigatorChapter : (RealmUUID?) -> Unit = {},
) = AnimatedVisibility(
	visible = isVisible, enter = expandVertically(tween(300)), exit = shrinkVertically(tween(300))
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState()),
	) {
		Spacer(modifier = Modifier.width(10.dp))
		if (showRoot) {
			NavigatorItem(
				id = null,
				title = null,
				color = null,
				isDefault = false,
			) { onClickNavigatorChapter(null) }
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Preview
@Composable
private fun NavigatorItem(
	id : RealmUUID? = RealmUUID.random(),
	title : String? = null,
	color : Color? = null,
	isDefault : Boolean = false,
	onClick : () -> Unit = {},
) {
	val containerColor = color ?: MaterialTheme.colorScheme.surface
	val contentColor = color?.getInverseBWColor() ?: MaterialTheme.colorScheme.onSurface

	SuggestionChip(
		onClick = onClick,
		label = {
			(title ?: id?.toString())?.let { title -> AnimatedText(text = title) } ?: Icon(
				painter = painterResource(id = R.drawable.ic_home),
				tint = contentColor,
				contentDescription = "Root Chapter"
			)
		},
		icon = if (isDefault) {
			{ Icon(painter = painterResource(id = R.drawable.ic_sparkle), tint = contentColor, contentDescription = "Default Chapter") }
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
