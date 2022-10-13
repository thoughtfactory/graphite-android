package com.syncodec.graphite.presentation.search.composable.bar

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.search.composable.buildingBlock.SearchBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
	tag : String?,
	query: String?,
	onClickBack : () -> Unit,
	onHitSearch: (String) -> Unit,
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
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

		AnimatedVisibility(
			visible = tag != null || query != null,
			enter = expandVertically(tween(300)) + fadeIn(tween(300)),
			exit = shrinkVertically(tween(300)) + fadeOut(tween(300)),
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.surface)
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
						text = tag ?: query ?: "",
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurface,
						fontWeight = FontWeight.Bold
					)

					Spacer(modifier = Modifier.width(16.dp))
				}

				Spacer(modifier = Modifier.height(8.dp))
			}
		}
	}
}
