package com.syncodec.momento.bucketItemComponent.thought

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.momento.bucketItemComponent.BucketItemActivity


@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun ThoughtCard(
	thoughtList: SnapshotStateList<String>,
	onClick: (BucketItemActivity.Click) -> Unit
) {
	var showEditor by remember { mutableStateOf(false) }

	Card(
		modifier = Modifier
			.fillMaxWidth(),
		elevation = 0.dp,
		shape = RoundedCornerShape(12.dp),
		border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondaryContainer),
		backgroundColor = Color.Transparent,
	) {
		Column(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			thoughtList.forEachIndexed { index, thought ->
				ThoughtContent(
					thought = thought,
					isFirst = index == 0
				) { thoughtList[index] = it }
				Box(
					modifier = Modifier
						.fillMaxWidth(0.71f)
						.height(2.dp)
						.padding(16.dp, 0.dp)
						.background(MaterialTheme.colorScheme.secondaryContainer)
				)
			}

			AnimatedContent(targetState = showEditor) {
				if (it) ThoughtEditor(
					isFirst = thoughtList.isEmpty(),
					onSave = { thought ->
						thoughtList.add(thought)
						onClick(BucketItemActivity.Click.ADD_THOUGHT)
					},
					onDiscard = { showEditor = false }
				)
				else AddThoughtButton { showEditor = true }
			}
		}
	}
}
