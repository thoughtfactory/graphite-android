package com.syncodec.momento.bucketItemComponent.thought

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun ThoughtContent(
	thought: String,
	isFirst: Boolean,
	onSave: (String) -> Unit
) {
	var showEditor by remember { mutableStateOf(false) }

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.combinedClickable(
				onClick = {},
				onLongClick = { showEditor = true }
			)
	) {
		AnimatedContent(targetState = showEditor) {
			if (it) {
				Column(
					modifier = Modifier
				) {
					ThoughtEditor(
						text = thought,
						isFirst = isFirst,
						onSave = {
							onSave(it)
							showEditor = false
						},
						onDiscard = { showEditor = false }
					)
				}
			} else {
				Text(
					text = thought,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp, if (isFirst) 16.dp else 12.dp, 16.dp, 12.dp)
				)
			}
		}
	}
}
