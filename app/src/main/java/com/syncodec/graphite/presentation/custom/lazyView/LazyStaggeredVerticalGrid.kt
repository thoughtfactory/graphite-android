package com.syncodec.graphite.presentation.custom.lazyView


import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LazyStaggeredVerticalGrid(
	columnCount: Int,
	contentPadding: PaddingValues = PaddingValues(0.dp),
	content: @Composable LazyStaggeredGridScope.() -> Unit,
) {
	val states: Array<LazyListState> = (0 until columnCount)
		.map { rememberLazyListState() }
		.toTypedArray()
	val scope = rememberCoroutineScope { Dispatchers.Main.immediate }
	val scroll = rememberScrollableState { delta ->
		scope.launch { states.forEach { it.scrollBy(-delta) } }
		delta
	}
	val gridScope = LazyStaggeredGridScope(columnCount)
	content(gridScope)

	Box(
		modifier = Modifier
			.scrollable(scroll, Orientation.Vertical, flingBehavior = ScrollableDefaults.flingBehavior())
	) {
		Row {
			for (index in 0 until columnCount) {
				LazyColumn(
					userScrollEnabled = false,
					contentPadding = contentPadding,
					state = states[index],
					modifier = Modifier.weight(1f)
				) {
					for ((key, itemContent) in gridScope.items[index]) {
						item(key = key) {
							itemContent()						}
					}
				}
			}
		}
	}
}

class LazyStaggeredGridScope(
	private val columnCount: Int,
) {
	private var currentIndex = 0
	val items: Array<MutableList<Pair<Any?, @Composable () -> Unit>>> = Array(columnCount) { mutableListOf() }

	fun item(key: Any? = null, content: @Composable () -> Unit) {
		items[currentIndex % columnCount] += key to content
		currentIndex += 1
	}
}








//@Composable
//fun LazyStaggeredVerticalGrid(
//	contentPadding: PaddingValues = PaddingValues(0.dp),
//	content: @Composable LazyStaggeredGridScope.() -> Unit,
//) {
//	val listState1 = rememberLazyListState()
//	val listState2 = rememberLazyListState()
//
//	var list1Size by remember { mutableStateOf(0) }
//	var list2Size by remember { mutableStateOf(0) }
//
//	var currentItem by remember { mutableStateOf(0) }
//
//	val scope = rememberCoroutineScope { Dispatchers.Main.immediate }
//	val scroll = rememberScrollableState { delta ->
//		scope.launch {
//			listState1.scrollBy(-delta)
//			listState2.scrollBy(-delta)
//		}
//		delta
//	}
//
//	val gridScope = LazyStaggeredGridScope(2)
//	content(gridScope)
//
//	Box(
//		modifier = Modifier
//			.scrollable(scroll, Orientation.Vertical, flingBehavior = ScrollableDefaults.flingBehavior())
//	) {
//		Row {
//			LazyColumn(
//				userScrollEnabled = false,
//				contentPadding = contentPadding,
//				state = listState1,
//				modifier = Modifier.weight(1f)
//			) {
//				gridScope.list1.forEach { itemContent ->
//					item {
//						itemContent()
//					}
//				}
//			}
//			LazyColumn(
//				userScrollEnabled = false,
//				contentPadding = contentPadding,
//				state = listState2,
//				modifier = Modifier.weight(1f)
//			) {
//				gridScope.list2.forEach { itemContent ->
//					item {
//						itemContent()
//					}
//				}
//			}
//		}
//	}
//}
//
//class LazyStaggeredGridScope(
//	private val columnCount: Int,
//) {
//	private var currentIndex = 0
//	val items: MutableList<@Composable () -> Unit> = mutableListOf()
//
//	val list1: SnapshotStateList<@Composable () -> Unit> = SnapshotStateList()
//	val list2: SnapshotStateList<@Composable () -> Unit> = SnapshotStateList()
//
//	private var list1Size = mutableStateOf(0)
//	private var list2Size = mutableStateOf(0)
//
//	private var keySet: MutableSet<String> = mutableSetOf()
//
//	fun item(key: String, content: @Composable ((Int) -> Unit) -> Unit) {
//		if (key !in keySet) {
//			if (list1Size.value <= list2Size.value) {
//				list1.add {
//					content {
//						list1Size.value += it
//						keySet.add(key)
//						Log.i("npr71", "list1 : ${list1.size} ; list2 : ${list2.size} list1Size : ${list1Size.value} ; list2Size : ${list2Size.value}")
//					}
//				}
//			} else {
//				list2.add {
//					content {
//						list2Size.value += it
//						keySet.add(key)
//						Log.i("npr71", "list1 : ${list1.size} ; list2 : ${list2.size} list1Size : ${list1Size.value} ; list2Size : ${list2Size.value}")
//					}
//				}
//			}
//		}
//	}
//}
