package com.syncodec.graphite.notebookComponent.miscellaneous

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview


@Preview
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ComponentChooser(
	isSelected: Boolean = false,
	selectedSize: Int = 0,
	onClickDelete: () -> Unit = {}
) {
//	val configuration = LocalConfiguration.current
//	val screenWidth = configuration.screenWidthDp.dp
//
//	val scope = rememberCoroutineScope()
//
//	val buttonWidth = (screenWidth - 24.dp) / 3
//
//	val notebookViewModel: NotebookViewModel = viewModel()
//	var componentType by notebookViewModel.activityState.notebookComponentType
//
//	val spacerWidth by animateDpAsState(
//		targetValue = when (componentType) {
//			NotebookActivity.ComponentType.ALL -> 0.dp
//			NotebookActivity.ComponentType.CHAPTER -> (screenWidth - 24.dp) / 3
//			NotebookActivity.ComponentType.NOTE -> (screenWidth - 24.dp) * 2 / 3
//		},
//		tween(
//			durationMillis = 400
//		)
//	)
//
//	val allColor by animateColorAsState(
//		targetValue = if (componentType == NotebookActivity.ComponentType.ALL) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
//		tween(durationMillis = 400)
//	)
//	val chapterColor by animateColorAsState(
//		targetValue = if (componentType == NotebookActivity.ComponentType.CHAPTER) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
//		tween(durationMillis = 400)
//	)
//	val noteColor by animateColorAsState(
//		targetValue = if (componentType == NotebookActivity.ComponentType.NOTE) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
//		tween(durationMillis = 400)
//	)
//
//	val interactionSource = remember { MutableInteractionSource() }
//
//	Box(
//		modifier = Modifier
//			.fillMaxWidth()
//			.height(56.dp)
//			.padding(12.dp, 8.dp),
//		contentAlignment = Alignment.Center
//	) {
//		AnimatedContent(
//			targetState = isSelected,
//			modifier = Modifier,
//		) { targetState ->
//			if (targetState) {
//				Box(
//					modifier = Modifier
//						.fillMaxWidth()
//						.height(40.dp)
//						.clip(RoundedCornerShape(12.dp))
//						.background(MaterialTheme.colorScheme.primaryContainer),
//					contentAlignment = Alignment.Center
//				) {
//					Row(
//						modifier = Modifier
//							.padding(16.dp, 0.dp, 0.dp, 0.dp)
//							.fillMaxWidth(),
//						verticalAlignment = Alignment.CenterVertically,
//						horizontalArrangement = Arrangement.SpaceBetween
//					) {
//						Text(
//							text = "${if (selectedSize == 0) "No entry" else if (selectedSize == 1) "1 entry" else "$selectedSize entries"} selected",
//							style = MaterialTheme.typography.bodyMedium,
//							fontWeight = FontWeight.Bold,
//							color = MaterialTheme.colorScheme.onPrimaryContainer
//						)
//						IconButton(
//							onClick = {
//								onClickDelete()
//							}
//						) {
//							Icon(
//								imageVector = TablerIcons.Trash,
//								contentDescription = "Delete selected entries",
//								tint = MaterialTheme.colorScheme.onPrimaryContainer,
//								modifier = Modifier
//							)
//						}
//					}
//				}
//			} else {
//				Box(
//					modifier = Modifier
//						.fillMaxWidth()
//						.height(40.dp),
//					contentAlignment = Alignment.Center
//				) {
//					Box(
//						modifier = Modifier
//							.fillMaxWidth()
//							.height(28.dp)
//							.padding(4.dp, 0.dp)
//							.clip(RoundedCornerShape(12.dp))
//							.background(
//								Color(
//									MaterialTheme.colorScheme.primary
//										.toArgb()
//										.tints()[9]
//								)
//							)
//					)
//
//					Row(
//						modifier = Modifier
//							.fillMaxSize()
//					) {
//						Spacer(modifier = Modifier.width(spacerWidth))
//						Box(
//							modifier = Modifier
//								.fillMaxHeight()
//								.width(buttonWidth)
//								.clip(RoundedCornerShape(24.dp))
//								.background(
//									Color(
//										MaterialTheme.colorScheme.primary
//											.toArgb()
//											.tints()[3]
//									)
//								),
//						)
//					}
//
//					Row(
//						modifier = Modifier
//							.fillMaxWidth()
//							.height(28.dp),
//						verticalAlignment = Alignment.CenterVertically
//					) {
//						Box(
//							contentAlignment = Alignment.Center,
//							modifier = Modifier
//								.fillMaxHeight()
//								.weight(1f)
//								.clip(RoundedCornerShape(24.dp))
//								.clickable(interactionSource = interactionSource, indication = null) {
//									componentType = NotebookActivity.ComponentType.ALL
//								},
//						) {
//							Text(
//								text = "All",
//								style = MaterialTheme.typography.bodySmall,
//								color = allColor,
//								fontWeight = FontWeight.Bold,
//								textAlign = TextAlign.Center,
//								maxLines = 1
//							)
//						}
//						Box(
//							contentAlignment = Alignment.Center,
//							modifier = Modifier
//								.fillMaxHeight()
//								.weight(1f)
//								.clip(RoundedCornerShape(24.dp))
//								.clickable(interactionSource = interactionSource, indication = null) {
//									componentType = NotebookActivity.ComponentType.CHAPTER
//								},
//						) {
//							Text(
//								text = "Chapter",
//								style = MaterialTheme.typography.bodySmall,
//								color = chapterColor,
//								fontWeight = FontWeight.Bold,
//								textAlign = TextAlign.Center,
//								maxLines = 1
//							)
//						}
//						Box(
//							contentAlignment = Alignment.Center,
//							modifier = Modifier
//								.fillMaxHeight()
//								.weight(1f)
//								.clip(RoundedCornerShape(24.dp))
//								.clickable(interactionSource = interactionSource, indication = null) {
//									componentType = NotebookActivity.ComponentType.NOTE
//								},
//						) {
//							Text(
//								text = "Note",
//								style = MaterialTheme.typography.bodySmall,
//								color = noteColor,
//								fontWeight = FontWeight.Bold,
//								textAlign = TextAlign.Center,
//								maxLines = 1
//							)
//						}
//					}
//				}
//			}
//		}
//	}
}
