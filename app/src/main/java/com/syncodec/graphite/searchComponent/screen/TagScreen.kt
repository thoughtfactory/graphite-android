package com.syncodec.graphite.searchComponent.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.database.tag.TagDbEntry
import com.syncodec.graphite.R


@Composable
fun TagScreen(
	tagList: List<TagDbEntry>,
	onClick: (String) ->  Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		tagList.forEach {
			item { TagCard(tag = it) { onClick(it.tag) } }
			item {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(1.dp)
						.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f))
				)
			}
		}
	}
}

@Composable
private fun TagCard(
	tag: TagDbEntry,
	onClick: () -> Unit
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.clickable { onClick() },
	) {
		Spacer(modifier = Modifier.width(24.dp))
		Icon(
			painter = painterResource(id = R.drawable.ic_hashtag),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.requiredSize(20.dp)
		)

		Spacer(modifier = Modifier.width(24.dp))

		Text(
			text = tag.tag,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
		)
	}
}
