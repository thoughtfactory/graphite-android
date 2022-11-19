package com.syncodec.graphite.presentation.notebook.composable.buildingBlock.navigator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.utils.getInverseBWColor
import io.realm.kotlin.types.RealmUUID


@Composable
fun Navigator(
	showRoot : Boolean = false,
	defaultChapterId : RealmUUID?,
	chapterObjectLiteList : List<ChapterObjectLite?>,
	onClick : (RealmUUID?) -> Unit
) {
	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.horizontalScroll(rememberScrollState())
		) {
			Spacer(
				modifier = Modifier
					.width(12.dp)
					.height(48.dp)
			)
			if (showRoot) {
				NavigatorItem(
					title = "/",
					color = MaterialTheme.colorScheme.onBackground,
					isDefault = false
				) { onClick(null) }
			}
			chapterObjectLiteList.reversed().forEach {
				NavigatorItem(
					title = it?.title,
					color = it?.color?.let { it1 -> Color(it1) },
					isDefault = it?.id == defaultChapterId
				) { onClick(it?.id) }
			}
			Spacer(modifier = Modifier.width(12.dp))
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigatorItem(
	title : String?,
	color : Color?,
	isDefault : Boolean = false,
	onClick : () -> Unit
) {
	var isVisible by remember { mutableStateOf(false) }

	LaunchedEffect(key1 = isVisible) { isVisible = true }

	AnimatedVisibility(
		visible = isVisible,
		enter = fadeIn(tween(300)),
		exit = fadeOut(tween(300))
	) {
		Row(
			modifier = Modifier,
			verticalAlignment = Alignment.CenterVertically
		) {
			SuggestionChip(
				onClick = onClick,
				icon = {
					if (isDefault) Icon(
						painter = painterResource(id = R.drawable.ic_state),
						contentDescription = "check",
					)
					else null
				},
				label = {
					Text(
						text = title ?: "",
						fontWeight = FontWeight.Bold
					)
				},
				colors = SuggestionChipDefaults.suggestionChipColors(
					containerColor = if (color == null || color == Color.Unspecified) MaterialTheme.colorScheme.surface else color,
					labelColor = if (color == null || color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color.getInverseBWColor(),
					iconContentColor = if (color == null || color == Color.Unspecified) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f) else color.getInverseBWColor()
						.copy(alpha = 0.47f),
				),
				border = SuggestionChipDefaults.suggestionChipBorder(borderColor = color ?: MaterialTheme.colorScheme.surface),
			)

			Spacer(modifier = Modifier.width(4.dp))

			Icon(
				painter = painterResource(id = R.drawable.ic_chevron_right),
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onBackground
			)

			Spacer(modifier = Modifier.width(4.dp))
		}
	}
}
