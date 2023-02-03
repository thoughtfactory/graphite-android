package com.syncodec.graphite.presentation.common.scaffold

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.ui.IconButtonSize
import kotlinx.coroutines.launch


@Preview
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun GenericScaffold(
	modifier : Modifier = Modifier,
	topBar : @Composable () -> Unit = { },
	bottomBar : (@Composable () -> Unit)? = null,
	isBottomBarVisible : Boolean = true,
	floatingActionButton : @Composable () -> Unit = { },
	isFloatingActionButtonVisible : Boolean = true,
	modalBottomSheetState : ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
	sheetContent : @Composable ColumnScope.() -> Unit = { },
	dialogContent : @Composable () -> Unit = { },
	snackbarHost : @Composable () -> Unit = { },
	primaryButton : GenericButton? = null,
	secondaryButton : GenericButton? = null,
	isButtonVisible : Boolean? = null,
	overlayContent : @Composable () -> Unit = { },
	content : @Composable () -> Unit = { },
) {
	val scope = rememberCoroutineScope()
	var bottomBarHeight by remember { mutableStateOf<Float?>(null) }

	BackHandler(enabled = modalBottomSheetState.isVisible) { scope.launch { modalBottomSheetState.hide() } }

	Box(
		modifier = Modifier.fillMaxSize()
	) {
		ModalBottomSheetLayout(
			sheetState = modalBottomSheetState,
			sheetElevation = 0.dp,
			sheetBackgroundColor = Color.Transparent,
			scrimColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f),
			sheetContent = {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
						.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.extraLarge)
				) {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally,
						modifier = Modifier
							.fillMaxWidth()
							.padding(24.dp, 12.dp, 24.dp, 20.dp),
						content = sheetContent
					)
				}
			},
			modifier = modifier.fillMaxSize()
		) {
			Scaffold(
				topBar = topBar,
				modifier = Modifier.fillMaxSize(),
				snackbarHost = snackbarHost,
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
							modifier = Modifier.weight(1f)
						) {
							content()
							androidx.compose.animation.AnimatedVisibility(
								visible = isFloatingActionButtonVisible,
								enter = fadeIn(tween(300)) + scaleIn(tween(300)),
								exit = fadeOut(tween(300)) + scaleOut(tween(300)),
								modifier = Modifier
									.align(Alignment.BottomEnd)
									.padding(16.dp)
							) {
								floatingActionButton()
							}
						}
						bottomBar?.let {
							androidx.compose.animation.AnimatedVisibility(
								visible = isBottomBarVisible,
								enter = expandVertically(tween(300)),
								exit = shrinkVertically(tween(300)),
								modifier = Modifier
									.onGloballyPositioned { coordinates ->
										bottomBarHeight = coordinates.size.height.toFloat()
									},
							) { it() }
						}
					}
					primaryButton?.let { genericButton ->
						bottomBarHeight?.let {
							Box(
								modifier = Modifier
									.align(Alignment.BottomCenter)
									.graphicsLayer { translationY = - it + 20.dp.toPx() }
							) {
								PrimaryButton(
									primaryIcon = genericButton.icon,
									primaryText = genericButton.text ?: "",
									isVisible = isButtonVisible ?: true,
									onClickPrimary = genericButton.onClick,
									secondaryButton = secondaryButton
								)
							}
						}
					}
				}
			}
		}
		dialogContent()
		overlayContent()
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PrimaryButton(
	primaryIcon : Int,
	primaryText : String,
	isVisible : Boolean = true,
	onClickPrimary : () -> Unit,
	secondaryButton : GenericButton? = null,
) {
	AnimatedVisibility(
		visible = isVisible,
		enter = scaleIn(tween(300)),
		exit = scaleOut(tween(300))
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.height(40.dp)
				.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
				.clip(RoundedCornerShape(50)),
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxHeight()
					.clickable { onClickPrimary() }
			) {
				Spacer(modifier = Modifier.width(if (secondaryButton == null) 24.dp else 16.dp))
				Icon(
					painter = painterResource(id = primaryIcon),
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onPrimary,
					modifier = Modifier.requiredSize(IconButtonSize)
				)

				Spacer(modifier = Modifier.width(12.dp))

				Text(
					text = primaryText,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onPrimary,
					fontWeight = FontWeight.Bold,
				)

				Spacer(modifier = Modifier.width(if (secondaryButton == null) 24.dp else 12.dp))
			}

			secondaryButton?.let {
				Box(
					modifier = Modifier
						.width(2.dp)
						.height(16.dp)
						.background(MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(50))
				)

				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.fillMaxHeight()
						.clickable { it.onClick() }
				) {
					Spacer(modifier = Modifier.width(12.dp))
					Icon(
						painter = painterResource(id = it.icon),
						contentDescription = it.text,
						tint = MaterialTheme.colorScheme.onPrimary,
						modifier = Modifier
							.requiredSize(IconButtonSize)
							.padding(2.dp)
					)
					Spacer(modifier = Modifier.width(16.dp))
				}
			}
		}
	}

}
