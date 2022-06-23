package com.syncodec.graphite.bucketItemComponent.miscellaneous.thought

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.bucketItemComponent.BucketItemActivity


@OptIn(
	ExperimentalAnimationApi::class,
	ExperimentalMaterial3Api::class
)
@Composable
fun ThoughtCard(
	thoughtList: SnapshotStateList<String>,
	onAction: (BucketItemActivity.Action, Any?) -> Unit
) {
	var showEditor by remember { mutableStateOf(false) }

	Card(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(Color.Transparent),
		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
		shape = RoundedCornerShape(12.dp),
	) {
		Column(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			thoughtList.forEachIndexed { index, thought ->
				ThoughtContent(
					thought = thought,
					isFirst = index == 0,
					onDelete = {
						thoughtList.removeAt(index)
						onAction(BucketItemActivity.Action.ADD_THOUGHT, null)
					},
					onSave = {
						thoughtList[index] = it
						onAction(BucketItemActivity.Action.ADD_THOUGHT, null)
					}
				)
				Box(
					modifier = Modifier
						.fillMaxWidth(0.71f)
						.height(1.dp)
						.padding(16.dp, 0.dp)
						.background(MaterialTheme.colorScheme.secondaryContainer)
				)
			}

			AnimatedContent(targetState = showEditor) {
				if (it) ThoughtEditor(
					isFirst = thoughtList.isEmpty(),
					onSave = { thought ->
						thoughtList.add(thought)
						onAction(BucketItemActivity.Action.ADD_THOUGHT, null)
					},
					onDiscard = { showEditor = false },
					onDelete = null
				)
				else AddThoughtButton { showEditor = true }
			}
		}
	}
}
