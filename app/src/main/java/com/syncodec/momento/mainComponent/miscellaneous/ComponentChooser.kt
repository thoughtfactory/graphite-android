package com.syncodec.momento.mainComponent.miscellaneous

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.screen.MomentoScreenType
import compose.icons.TablerIcons
import compose.icons.tablericons.Trash
import dev.jorgecastillo.androidcolorx.library.tints

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ComponentChooser(
	isSelected: Boolean = false,
	selectedSize: Int = 0,
	onClickDelete: () -> Unit
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	val buttonWidth = (screenWidth - 24.dp) / 3

	val viewModel: MainViewModel = viewModel()
	var momentoScreenType by viewModel.mainActivityState.momentoScreenType

	val spacerWidth by animateDpAsState(
		targetValue = when (momentoScreenType) {
			MomentoScreenType.Diary -> 0.dp
			MomentoScreenType.Notebook -> (screenWidth - 24.dp) / 3
			MomentoScreenType.Scratchpad -> (screenWidth - 24.dp) * 2 / 3
		},
		tween(
			durationMillis = 400
		)
	)

	val diaryColor by animateColorAsState(
		targetValue = if (momentoScreenType == MomentoScreenType.Diary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
		tween(durationMillis = 400)
	)
	val notebookColor by animateColorAsState(
		targetValue = if (momentoScreenType == MomentoScreenType.Notebook) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
		tween(durationMillis = 400)
	)
	val scratchpadColor by animateColorAsState(
		targetValue = if (momentoScreenType == MomentoScreenType.Scratchpad) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
		tween(durationMillis = 400)
	)

	val interactionSource = remember { MutableInteractionSource() }

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.padding(12.dp, 0.dp),
		contentAlignment = Alignment.Center
	) {
		AnimatedContent(
			targetState = isSelected,
			modifier = Modifier,
		) { targetState ->
			if (targetState) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(40.dp)
						.clip(RoundedCornerShape(12.dp))
						.background(MaterialTheme.colorScheme.primaryContainer),
					contentAlignment = Alignment.Center
				) {
					Row(
						modifier = Modifier
							.padding(16.dp, 0.dp, 0.dp, 0.dp)
							.fillMaxWidth(),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.SpaceBetween
					) {
						Text(
							text = "${if(selectedSize==0) "No entry" else if (selectedSize==1) "1 entry" else "$selectedSize entries"} selected",
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onPrimaryContainer
						)
						IconButton(
							onClick = {
								onClickDelete()
							}
						) {
							Icon(
								imageVector = TablerIcons.Trash,
								contentDescription = "Delete selected entries",
								tint = MaterialTheme.colorScheme.onPrimaryContainer,
								modifier = Modifier
							)
						}
					}
				}
			} else {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(40.dp),
					contentAlignment = Alignment.Center
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(28.dp)
							.padding(4.dp, 0.dp)
							.clip(RoundedCornerShape(12.dp))
							.background(
								Color(
									MaterialTheme.colorScheme.primary
										.toArgb()
										.tints()[9]
								)
							)
					)

					Row(
						modifier = Modifier
							.fillMaxSize()
					) {
						Spacer(modifier = Modifier.width(spacerWidth))
						Box(
							modifier = Modifier
								.fillMaxHeight()
								.width(buttonWidth)
								.clip(RoundedCornerShape(24.dp))
								.background(
									Color(
										MaterialTheme.colorScheme.primary
											.toArgb()
											.tints()[3]
									)
								),
						)
					}

					Row(
						modifier = Modifier
							.fillMaxWidth()
							.height(28.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier
								.fillMaxHeight()
								.weight(1f)
								.clip(RoundedCornerShape(24.dp))
								.clickable(interactionSource = interactionSource, indication = null) {
									momentoScreenType = MomentoScreenType.Diary
								},
						) {
							Text(
								text = "Diary",
								style = MaterialTheme.typography.bodySmall,
								color = diaryColor,
								fontWeight = FontWeight.Bold,
								textAlign = TextAlign.Center,
								maxLines = 1
							)
						}
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier
								.fillMaxHeight()
								.weight(1f)
								.clip(RoundedCornerShape(24.dp))
								.clickable(interactionSource = interactionSource, indication = null) {
									momentoScreenType = MomentoScreenType.Notebook
								},
						) {
							Text(
								text = "Notebook",
								style = MaterialTheme.typography.bodySmall,
								color = notebookColor,
								fontWeight = FontWeight.Bold,
								textAlign = TextAlign.Center,
								maxLines = 1
							)
						}
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier
								.fillMaxHeight()
								.weight(1f)
								.clip(RoundedCornerShape(24.dp))
								.clickable(interactionSource = interactionSource, indication = null) {
									momentoScreenType = MomentoScreenType.Scratchpad
								},
						) {
							Text(
								text = "Scratchpad",
								style = MaterialTheme.typography.bodySmall,
								color = scratchpadColor,
								fontWeight = FontWeight.Bold,
								textAlign = TextAlign.Center,
								maxLines = 1
							)
						}
					}
				}
			}
		}
	}
}
