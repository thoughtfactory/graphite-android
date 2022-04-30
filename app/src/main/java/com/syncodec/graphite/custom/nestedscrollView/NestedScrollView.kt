package com.syncodec.graphite.custom.nestedscrollView

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Define a [VerticalNestedScrollView].
 *
 * @param state the state object to be used to observe the [VerticalNestedScrollView] state.
 * @param modifier the modifier to apply to this layout.
 * @param content a block which describes the header.
 * @param content a block which describes the content.
 */
@Composable
fun NestedScrollView(
	modifier: Modifier = Modifier,
	state: NestedScrollViewState,
	header: @Composable () -> Unit,
	content: @Composable () -> Unit,
	onScroll: (Float) -> Unit
) {
	Layout(
		modifier = modifier
			.scrollable(
				orientation = Orientation.Vertical,
				state = rememberScrollableState {
					onScroll(it)
					state.drag(it)
				}
			)
			.nestedScroll(state.nestedScrollConnectionHolder),
		content = {
			Box {
				header.invoke()
			}
			Box {
				content.invoke()
			}
		},
	) { measurables, constraints ->
		layout(constraints.maxWidth, constraints.maxHeight) {
			val headerPlaceable = measurables[0].measure(constraints.copy(maxHeight = Constraints.Infinity))
			headerPlaceable.place(0, state.offset.roundToInt())
			state.updateBounds(-((headerPlaceable.height - (88.dp + 24.dp).toPx())))
			val contentPlaceable = measurables[1].measure(constraints.copy(maxHeight = constraints.maxHeight))
			contentPlaceable.place(0, state.offset.roundToInt() + headerPlaceable.height)
		}
	}
}
