package com.syncodec.graphite.presentation.search.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedRealmUUIDList
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.search.composable.buildingBlock.SearchBar
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.utils.getInverseBWColor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
	tagObject : TagObject?,
	query: String?,
	onClickBack : () -> Unit,
	onHitSearch: (String) -> Unit,
) {

	val isSelected = LocalCompositionIsSelected.current
	val selectedRealmUUIDList = LocalCompositionSelectedRealmUUIDList.current
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
							onClick = {  }
						) {
							Icon(
								painter = painterResource(id = R.drawable.ic_delete),
								contentDescription = "Delete items",
								tint = Color.DeleteContainer
							)
						}
					},
					colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
				)
			} else {
				TopAppBar(
					modifier = Modifier.fillMaxWidth(),
					navigationIcon = {
						MenuButton(
							icon = R.drawable.ic_back,
							tint = MaterialTheme.colorScheme.onBackground,
							onClick = onClickBack
						)
					},
					title = {
						SearchBar(onHitSearch = onHitSearch)
					},
				)
			}
		}

		AnimatedVisibility(
			visible = tagObject != null || query != null,
			enter = expandVertically(tween(300)) + fadeIn(tween(300)),
			exit = shrinkVertically(tween(300)) + fadeOut(tween(300)),
			modifier = Modifier
				.fillMaxWidth()
				.background(containerColor)
		) {
			Column(
				modifier = Modifier.fillMaxWidth()
			) {
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

					Spacer(modifier = Modifier.width(16.dp))

					Button(
						shape = RoundedCornerShape(12.dp),
						onClick = {  },
						colors = ButtonDefaults.buttonColors(
							containerColor = tagObject?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.primary,
							contentColor = tagObject?.color?.let { Color(it).getInverseBWColor() } ?: MaterialTheme.colorScheme.onPrimary
						)

					) {
						Text(
							text = tagObject?.tag ?: query ?: "",
						)
					}

					Spacer(modifier = Modifier.width(16.dp))
				}

				Spacer(modifier = Modifier.height(8.dp))
			}
		}
	}
}
