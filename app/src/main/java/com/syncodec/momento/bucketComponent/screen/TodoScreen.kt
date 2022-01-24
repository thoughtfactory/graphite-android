package com.syncodec.momento.bucketComponent.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.syncodec.momento.bucketComponent.miscellaneous.BucketTopBar
import org.burnoutcrew.reorderable.*


data class BucketTodo(val title: String, val key: Long, val isLocked: Boolean = false)

@Composable
fun TodoScreen() {
	val bucketList = mutableListOf(
		BucketTodo("The question is, what color will everything be at the moment I come for you, what will the sky be saying", 0, false),
		BucketTodo("beta", 1, true),
		BucketTodo("gamma", 2, false),
		BucketTodo("gamma", 3, false),
		BucketTodo("gamma", 4, false),
		BucketTodo("gamma", 5, false),
		BucketTodo("gamma", 6, false),
		BucketTodo("gamma", 7, false),
		BucketTodo("gamma", 8, false),
		BucketTodo("gamma", 9, false),
	).toMutableStateList()

	val state = rememberReorderState()

	Scaffold(
	) {
		VerticalReorderList(
			items = bucketList,
			state = state,
			onMove = { fromPos, toPos -> bucketList.move(fromPos.index, toPos.index) },
			canDragOver = { true }
		)
	}
}


@Composable
private fun VerticalReorderList(
	modifier: Modifier = Modifier,
	items: List<BucketTodo>,
	state: ReorderableState = rememberReorderState(),
	onMove: (fromPos: ItemPosition, toPos: ItemPosition) -> (Unit),
	canDragOver: ((pos: ItemPosition) -> Boolean),
) {
	Column {
		BucketTopBar()
		LazyColumn(
			state = state.listState,
			modifier = modifier
				.fillMaxWidth()
				.then(Modifier.reorderable(state, onMove = onMove, canDragOver = canDragOver))
		) {
			items(items, { it.key }) { item ->
				TodoItem(
					title = item.title,
					modifier = Modifier
						.draggedItem(state.offsetByKey(item.key))
						.detectReorderAfterLongPress(state)
						.clickable { Log.i("NPR", "${state.draggedOffset}") }
				)
			}
		}
	}
}

@Preview
@Composable
private fun TodoItem(
	modifier: Modifier = Modifier,
	title: String = "Apple"
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(16.dp)
		)

		Row(
			verticalAlignment = Alignment.Top,
			modifier = Modifier
				.fillMaxWidth()
				.heightIn(48.dp, Dp.Unspecified)
				.padding(16.dp, 0.dp),
		) {
			Checkbox(
				checked = false,
				modifier = Modifier
					.size(48.dp),
				onCheckedChange = {}
			)

			Spacer(modifier = Modifier.width(16.dp))

			Column(
				modifier = Modifier
					.fillMaxWidth()
			) {
				Spacer(modifier = Modifier.height(14.dp))
				Text(
					text = title,
					style = MaterialTheme.typography.bodyMedium,
					modifier = Modifier
						.fillMaxWidth()
				)
			}
		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(16.dp)
		)

//		Box(
//			modifier = Modifier
//				.fillMaxWidth()
//				.heightIn(64.dp, Dp.Unspecified)
//				.padding(16.dp)
//		) {
//			Row(
//				verticalAlignment = Alignment.Top,
//				modifier = Modifier
//					.fillMaxWidth()
//			) {
//				Column {
//					Spacer(modifier = Modifier.height(14.dp))
//					Checkbox(
//						checked = false,
//						onCheckedChange = {}
//					)
//				}
//
//				Spacer(modifier = Modifier.width(16.dp))
//
//				Box(
//				modifier = Modifier
//					.fillMaxWidth()
//					.heightIn(48.dp, Dp.Unspecified)
//				) {
//					Column {
//						Spacer(modifier = Modifier.height(14.dp))
//						Text(
//							text = title,
//							style = MaterialTheme.typography.bodyMedium,
//							modifier = Modifier
//								.fillMaxWidth()
//						)
//					}
//				}
//			}
//		}
//		Row(
//			verticalAlignment = Alignment.CenterVertically,
//			modifier = Modifier
//				.fillMaxWidth()
//				.heightIn(64.dp, Dp.Unspecified)
//				.padding(16.dp)
//		) {
//			Checkbox(
//				checked = false,
//				onCheckedChange = {}
//			)
//
//			Spacer(modifier = Modifier.width(16.dp))
//
//			Box(
//				modifier = Modifier
//					.fillMaxWidth()
//					.heightIn(48.dp, Dp.Unspecified)
//			) {
//				Text(
//					text = title,
//					style = MaterialTheme.typography.bodyMedium,
//				)
//			}
//		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(1.dp)
				.background(Color.LightGray),
		)
	}
}
