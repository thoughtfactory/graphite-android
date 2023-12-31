package com.syncodec.graphite.presentation.common.scaffold

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun GenericScaffold2(
	modifier: Modifier = Modifier,
	bottomSheetState: SheetState = rememberModalBottomSheetState(),
	topBar: @Composable () -> Unit = { },
	bottomBar: @Composable (() -> Unit)? = null,
	isTopBarVisible: Boolean = true,
	isBottomBarVisible: Boolean = true,
	floatingActionButton: @Composable (ColumnScope.() -> Unit) = { },
	isFloatingActionButtonVisible: Boolean = true,
	dialogContent: @Composable () -> Unit = { },
	overlayContent: @Composable () -> Unit = { },
	content: @Composable (BoxScope.() -> Unit) = { },
) {
	Box(
		modifier = Modifier.fillMaxSize()
	) {
		Scaffold(
			topBar = {
				AnimatedVisibility(
					visible = isTopBarVisible,
					enter = expandVertically(tween(ANIMATION_DURATION_MILLIS)),
					exit = shrinkVertically(tween(ANIMATION_DURATION_MILLIS)),
					label = "topBar_visibility_animation"
				) {
					topBar()
				}
			},
			modifier = modifier.fillMaxSize()
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(it)
			) {
				Column(
					modifier = Modifier.fillMaxSize()
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.weight(1f)
					) {
						content()
						androidx.compose.animation.AnimatedVisibility(
							visible = isFloatingActionButtonVisible,
							enter = fadeIn(tween(ANIMATION_DURATION_MILLIS)) + scaleIn(tween(ANIMATION_DURATION_MILLIS)),
							exit = fadeOut(tween(ANIMATION_DURATION_MILLIS)) + scaleOut(tween(ANIMATION_DURATION_MILLIS)),
							modifier = Modifier
								.padding(16.dp)
								.align(Alignment.BottomEnd)
						) {
							Column(
								horizontalAlignment = Alignment.End
							) {
								floatingActionButton()
							}
						}
					}
					bottomBar?.let {
						androidx.compose.animation.AnimatedVisibility(
							visible = isBottomBarVisible,
							enter = expandVertically(tween(ANIMATION_DURATION_MILLIS)),
							exit = shrinkVertically(tween(ANIMATION_DURATION_MILLIS)),
						) { it() }
					}
				}
			}
		}

		dialogContent()
		overlayContent()
	}
}
