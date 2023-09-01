package com.syncodec.graphite.presentation.search.composable.buildingBlock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.search.SearchViewModel
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.TagItemView
import com.syncodec.graphite.presentation.ui.AttachmentContainer
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.IconButtonSize
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.LocalIsAuthenticated


@Preview
@Composable
fun SearchWhatView(
	tagList: Set<TagObject> = setOf(),
	onAddFilter: (SearchViewModel.Companion.NoteFilter) -> Unit = {},
) {
	val isAuthenticated = LocalIsAuthenticated.current

	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			FilterButton(
				title = stringResource(id = R.string.favourite),
				icon = R.drawable.ic_fa_heart,
				iconTint = Color.FavouriteContainer,
				onClick = { onAddFilter(SearchViewModel.Companion.NoteFilter.Favourite) },
			)
		}
		item {
			AnimatedVisibility(
				visible = isAuthenticated,
				enter = expandVertically(tween(470)),
				exit = shrinkVertically(tween(470))
			) {
				FilterButton(
					title = stringResource(id = R.string.locked),
					icon = R.drawable.ic_fa_lock_close,
					iconTint = Color.LockClosedContainer,
					onClick = { onAddFilter(SearchViewModel.Companion.NoteFilter.Locked) },
				)
			}
		}
		item {
			FilterButton(
				title = stringResource(id = R.string.with_attachments),
				icon = R.drawable.ic_fa_gallery,
				iconTint = Color.AttachmentContainer,
				onClick = { onAddFilter(SearchViewModel.Companion.NoteFilter.WithAttachment) },
			)
		}
		tagList.forEach { tagObject ->
			item {
				TagItemView(
					tagObject = tagObject,
					onClick = { onAddFilter(SearchViewModel.Companion.NoteFilter.Tag(tagObject = tagObject)) }
				)
			}
		}
	}
}

@Preview
@Composable
private fun FilterButton(
	title: String = "Favourites",
	icon: Int = R.drawable.ic_fa_heart_solid,
	iconTint: Color = Color.FavouriteContainer,
	onClick: () -> Unit = {},
) {
	Column {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(64.dp)
				.clickable { onClick() }
		) {
			Spacer(modifier = Modifier.width(24.dp))
			Icon(
				painter = painterResource(id = icon),
				contentDescription = title,
				tint = iconTint,
				modifier = Modifier.requiredSize(IconButtonSize)
			)

			Spacer(modifier = Modifier.width(12.dp))

			Text(
				text = title,
				style = MaterialTheme.typography.bodyMedium,
			)

			Spacer(modifier = Modifier.width(24.dp))
		}
		Divider()
	}
}
