package com.syncodec.momento.mainComponent.miscellaneous

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.momento.MainActivity
import com.syncodec.momento.R
import com.syncodec.momento.custom.button.StateButton
import com.syncodec.momento.custom.button.StateData
import com.syncodec.momento.miscellaneous.ThemeUtils.Companion.tone

@OptIn(
	ExperimentalMaterialApi::class, ExperimentalAnimationApi::class,
	ExperimentalMaterial3Api::class
)
@Composable
fun TopBar(
	isSelected: Boolean,
	selectedItemSize: Int,
	componentType: MainActivity.ComponentType,
	showComponentChooser: Boolean,
	showFavorite: Boolean,
	showArchived: Boolean,
	showLocked: Boolean,
	onAction: (MainActivity.Action, Any?) -> Unit,
) {
	Column(modifier = Modifier.fillMaxWidth()) {
		Crossfade(targetState = isSelected) {
			if (it) {
				SmallTopAppBar(
					title = {
						Text(
							text = if (selectedItemSize == 0) "Select items to delete" else if (selectedItemSize == 1) "1 item selected" else "$selectedItemSize items selected",
							modifier = Modifier,
							style = MaterialTheme.typography.titleMedium,
							color = MaterialTheme.colorScheme.onSurface
						)
					},
					actions = {
						IconButton(onClick = { onAction(MainActivity.Action.SHOW_DELETE, null) }) {
							Icon(
								painter = painterResource(id = R.drawable.ic_trash),
								contentDescription = "Delete items",
								tint = Color(0xFFF05945),
								modifier = Modifier
									.requiredSize(32.dp)
									.padding(4.dp)
							)
						}
					},
					colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
						containerColor = MaterialTheme.colorScheme.surface,
					),
				)
			} else {
				CenterAlignedTopAppBar(
					navigationIcon = {
						IconButton(onClick = { onAction(MainActivity.Action.MENU, null) }) {
							Icon(
								painter = painterResource(id = R.drawable.ic_icon),
								contentDescription = null,
								tint = Color.Unspecified,
								modifier = Modifier
									.requiredSize(32.dp)
									.padding(4.dp)
							)
						}
					},
					title = {
						Text(
							text = "MOMENTO",
							modifier = Modifier,
							fontFamily = FontFamily(
								Font(R.font.graduate_regular, FontWeight.Normal)
							),
							fontWeight = FontWeight.Bold,
							fontSize = 24.sp,
							lineHeight = 28.sp,
							letterSpacing = 2.sp,
							color = MaterialTheme.colorScheme.primary
						)
					},
					actions = {
						IconButton(onClick = { onAction(MainActivity.Action.SEARCH, null) }) {
							Icon(
								painter = painterResource(id = R.drawable.ic_search),
								contentDescription = "Search",
								tint = Color(0xFF2978B5),
								modifier = Modifier
									.requiredSize(32.dp)
									.padding(4.dp)
							)
						}
					},
					colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
						containerColor = MaterialTheme.colorScheme.surface,
					),
				)
			}
		}

		AnimatedVisibility(
			visible = showComponentChooser,
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 0.dp)
		) {
			StateButton(
				stateList = listOf(
					StateData(
						title = "Diary",
						icon = R.drawable.ic_write,
						stateTint = MaterialTheme.colorScheme.primary
					),
					StateData(
						title = "Notebook",
						icon = R.drawable.ic_notebook,
						stateTint = MaterialTheme.colorScheme.primary
					),
				),
				currentState = componentType.ordinal,
				modifier = Modifier.height(32.dp)
			) { onAction(MainActivity.Action.CHANGE_COMPONENT, it) }
		}

		AnimatedVisibility(visible = showFavorite || showArchived || showLocked) {
			Spacer(modifier = Modifier.height(6.dp))

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.horizontalScroll(rememberScrollState())
			) {
				Spacer(modifier = Modifier.width(12.dp))
				FilterChip(
					selected = showFavorite,
					onClick = { onAction(MainActivity.Action.TOGGLE_FAVOURITE, null) },
					colors = ChipDefaults.filterChipColors(
						backgroundColor = MaterialTheme.colorScheme.surface.tone(
							isSystemInDarkTheme(),
							1
						),
						selectedBackgroundColor = MaterialTheme.colorScheme.primary,
					),
					leadingIcon = {
						Icon(
							painter = painterResource(id = R.drawable.ic_favourite),
							contentDescription = null,
							tint = if (showFavorite) MaterialTheme.colorScheme.onPrimary
							else MaterialTheme.colorScheme.onSurface.tone(
								isSystemInDarkTheme(),
								1
							),
							modifier = Modifier.requiredSize(20.dp)
						)
					},
				) {
					Text(
						text = "Favourite",
						style = MaterialTheme.typography.bodyMedium,
						color = if (showFavorite) MaterialTheme.colorScheme.onPrimary
						else MaterialTheme.colorScheme.onSurface.tone(
							isSystemInDarkTheme(),
							1
						)
					)
				}

				Spacer(modifier = Modifier.width(8.dp))

				FilterChip(
					selected = showArchived,
					onClick = { onAction(MainActivity.Action.TOGGLE_ARCHIVED, null) },
					colors = ChipDefaults.filterChipColors(
						backgroundColor = MaterialTheme.colorScheme.surface.tone(
							isSystemInDarkTheme(),
							1
						),
						selectedBackgroundColor = MaterialTheme.colorScheme.primary
					),
					leadingIcon = {
						Icon(
							painter = painterResource(id = R.drawable.ic_archive),
							contentDescription = null,
							tint = if (showArchived) MaterialTheme.colorScheme.onPrimary
							else MaterialTheme.colorScheme.onSurface.tone(
								isSystemInDarkTheme(),
								1
							),
							modifier = Modifier.requiredSize(20.dp)
						)
					}
				) {
					Text(
						text = "Archived",
						style = MaterialTheme.typography.bodyMedium,
						color = if (showArchived) MaterialTheme.colorScheme.onPrimary
						else MaterialTheme.colorScheme.onSurface.tone(
							isSystemInDarkTheme(),
							1
						)
					)
				}

				Spacer(modifier = Modifier.width(8.dp))

				FilterChip(
					selected = showLocked,
					onClick = { onAction(MainActivity.Action.LOCKED, null) },
					colors = ChipDefaults.filterChipColors(
						backgroundColor = MaterialTheme.colorScheme.surface.tone(
							isSystemInDarkTheme(),
							1
						),
						selectedBackgroundColor = MaterialTheme.colorScheme.primary
					),
					leadingIcon = {
						Icon(
							painter = painterResource(id = R.drawable.ic_lock_close),
							contentDescription = null,
							tint = if (showLocked) MaterialTheme.colorScheme.onPrimary
							else MaterialTheme.colorScheme.onSurface.tone(
								isSystemInDarkTheme(),
								1
							),
							modifier = Modifier.requiredSize(20.dp)
						)
					}
				) {
					Text(
						text = "Locked",
						style = MaterialTheme.typography.bodyMedium,
						color = if (showLocked) MaterialTheme.colorScheme.onPrimary
						else MaterialTheme.colorScheme.onSurface.tone(
							isSystemInDarkTheme(),
							1
						)
					)
				}
			}
		}

		AnimatedVisibility(visible = !(showFavorite || showArchived || showLocked)) {
			Spacer(modifier = Modifier.height(12.dp))
		}
	}
}
