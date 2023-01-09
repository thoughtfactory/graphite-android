package com.syncodec.graphite.presentation.search.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.search.composable.buildingBlock.SearchBar
import com.syncodec.graphite.presentation.ui.DeleteContainer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
	tagObject : TagObject?,
	query : String?,
	onClickBack : () -> Unit,
	onHitSearch : (String) -> Unit,
) {

	val isSelected = LocalCompositionIsSelected.current
	val selectedRealmUUIDList = LocalCompositionSelectedObjectIdList.current
	val onSelect = LocalCompositionOnSelect.current

	val containerColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface,
		animationSpec = tween(300)
	)
	val contentColor by animateColorAsState(
		targetValue = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface,
		animationSpec = tween(300)
	)

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		Crossfade(
			targetState = isSelected,
			animationSpec = tween(300)
		) {
			if (it) {
				TopAppBar(
					navigationIcon = {
						MenuButton(
							icon = R.drawable.ic_close,
							tint = MaterialTheme.colorScheme.onBackground,
						) {
							onSelect(false)
							selectedRealmUUIDList.clear()
						}
					},
					title = {
						Text(
							text = if (selectedRealmUUIDList.isEmpty()) "No items selected" else if (selectedRealmUUIDList.size == 1) "1 item selected" else "${selectedRealmUUIDList.size} items selected",
							color = MaterialTheme.colorScheme.onBackground
						)
					},
					actions = {
						IconButton(
							onClick = { }
						) {
							Icon(
								painter = painterResource(id = R.drawable.ic_delete),
								contentDescription = "Delete items",
								tint = Color.DeleteContainer
							)
						}
					},
					colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
				)
			} else {
				SearchBar(
					onHitSearch = onHitSearch,
					onClickBack = onClickBack,
				)
			}
		}

		AnimatedVisibility(
			visible = tagObject != null || query != null,
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
						color = contentColor,
						fontWeight = FontWeight.Bold
					)

					Spacer(modifier = Modifier.width(8.dp))

					SuggestionChip(
						label = {
							Text(
								text = tagObject?.tag ?: query ?: "",
								style = MaterialTheme.typography.bodyMedium,
								fontWeight = FontWeight.Bold,
								modifier = Modifier,
							)
						},
						shape = MaterialTheme.shapes.medium,
						colors = SuggestionChipDefaults.suggestionChipColors(
							containerColor = MaterialTheme.colorScheme.primary,
							labelColor = MaterialTheme.colorScheme.onPrimary,
						),
						onClick = { /*TODO*/ },
					)

					Spacer(modifier = Modifier.width(16.dp))
				}
			}
		}
	}
}
