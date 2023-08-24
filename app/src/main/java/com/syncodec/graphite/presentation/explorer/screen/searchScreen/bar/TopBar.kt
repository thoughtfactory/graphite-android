package com.syncodec.graphite.presentation.explorer.screen.searchScreen.bar

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.button.GenericButtonDefaults
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.buildingBlock.SearchBar
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.SearchScreenViewModel
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.utils.getInverseBWColor


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Preview
@Composable
fun TopBar(
	searchFilterType : SearchScreenViewModel.Companion.SearchFilterType = SearchScreenViewModel.Companion.SearchFilterType.None,
	isSelecting : Boolean = false,
	selectedSize : Int = 0,
	onClickCancelSelect : () -> Unit = {},
	onClickSearch : (String) -> Unit = {},
	onClickDelete : () -> Unit = {},
) {
	val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Crossfade(
			targetState = isSelecting,
			animationSpec = tween(300)
		) {
			if (it) {
				TopAppBar(
					navigationIcon = {
						GenericButton(
							icon = R.drawable.ic_close,
							onClick = onClickCancelSelect
						)
					},
					title = {
						AnimatedText(
							text = if (selectedSize == 0) "No items selected" else if (selectedSize == 1) "1 item selected" else "$selectedSize items selected",
							color = MaterialTheme.colorScheme.onBackground
						)
					},
					actions = {
						GenericButton(
							icon = R.drawable.ic_delete,
							colors = GenericButtonDefaults.deleteButtonColors(),
							onClick = onClickDelete
						)
					},
					colors = TopAppBarDefaults.topAppBarColors(
						containerColor = MaterialTheme.colorScheme.background,
						navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
						titleContentColor = MaterialTheme.colorScheme.onBackground,
						actionIconContentColor = MaterialTheme.colorScheme.onBackground,
					)
				)
			} else {
				SearchBar(
					onHitSearch = onClickSearch,
					onClickBack = {
						backPressedDispatcher?.onBackPressed()
					},
				)
			}
		}

		AnimatedVisibility(
			visible = searchFilterType != SearchScreenViewModel.Companion.SearchFilterType.None,
			enter = expandVertically(tween(300)) + fadeIn(tween(300)),
			exit = shrinkVertically(tween(300)) + fadeOut(tween(300)),
			modifier = Modifier.fillMaxWidth()
		) {
			Column(
				modifier = Modifier.fillMaxWidth()
			) {
				Spacer(modifier = Modifier.height(8.dp))
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.fillMaxWidth()
				) {
					Spacer(modifier = Modifier.width(16.dp))

					Text(
						text = "Searching for",
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onBackground,
					)

					Spacer(modifier = Modifier.width(8.dp))

					SuggestionChip(
						label = {
							AnimatedText(
								text = when (searchFilterType) {
									is SearchScreenViewModel.Companion.SearchFilterType.None -> ""
									is SearchScreenViewModel.Companion.SearchFilterType.Favourite -> "Favourite"
									is SearchScreenViewModel.Companion.SearchFilterType.WithAttachment -> "With attachments"
									is SearchScreenViewModel.Companion.SearchFilterType.Locked -> "Locked"
									is SearchScreenViewModel.Companion.SearchFilterType.Tag -> searchFilterType.tag.tag
									is SearchScreenViewModel.Companion.SearchFilterType.Query -> searchFilterType.query
								},
								style = MaterialTheme.typography.bodyMedium,
							)
						},
						shape = MaterialTheme.shapes.medium,
						colors = SuggestionChipDefaults.suggestionChipColors(
							containerColor = if (searchFilterType is SearchScreenViewModel.Companion.SearchFilterType.Tag) Color(searchFilterType.tag.color) else MaterialTheme.colorScheme.primary,
							labelColor = if (searchFilterType is SearchScreenViewModel.Companion.SearchFilterType.Tag) Color(searchFilterType.tag.color).getInverseBWColor() else MaterialTheme.colorScheme.onPrimary,
						),
						border = null,
						modifier = Modifier.height((IconButtonSize * 2) - 2.dp),
						onClick = { /*TODO*/ }
					)

					Spacer(modifier = Modifier.weight(1f))

					GenericButton(
						icon = R.drawable.ic_info,
						tooltip = "Searching with words is experimental and may not work as expected.",
						showTooltipOnClick = true,
					)

					Spacer(modifier = Modifier.width(3.dp))
				}
			}
		}
	}
}
