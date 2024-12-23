package com.syncodec.graphite.presentation.explorer.screen.searchScreen.buildingBlock

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject
import com.syncodec.graphite.presentation.ui.ICON_SIZE
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.getInverseBWColor


@Preview
@Composable
fun InitView(
	tagList : List<TagObject> = listOf(),
	onClickFavorite : () -> Unit = {},
	onClickWithAttachments : () -> Unit = {},
	onClickLocked : () -> Unit = {},
	onClickTag : (TagObject) -> Unit = {},
) {
	val isAuthenticated = LocalIsAuthenticated.current

	LazyColumn(
		modifier = Modifier.fillMaxSize()
	) {
		item {
			ListItem(
				text = "Favourite",
				icon = R.drawable.ic_favourite,
				onClick = onClickFavorite
			)
		}
		item {
			AnimatedVisibility(
				visible = isAuthenticated,
				enter = expandVertically(tween(300)),
				exit = shrinkVertically(tween(300)),
			) {
				ListItem(
					text = "Locked",
					icon = R.drawable.ic_lock_close,
					onClick = onClickLocked
				)
			}
		}
		item {
			ListItem(
				text = "With attachments",
				icon = R.drawable.ic_file,
				onClick = onClickWithAttachments
			)
		}

		item {
			Spacer(
				modifier = Modifier
					.fillMaxWidth()
					.height(1.dp)
					.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f))
			)
		}

		if (tagList.isNotEmpty()) {
			item {
				Text(
					text = "Tags",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp, 16.dp, 16.dp, 12.dp)
				)
			}
		}
		tagList.forEach { tag ->
			item {
				ListItem(
					text = tag.tag,
					color = Color(tag.color),
					icon = R.drawable.ic_tag,
					size = tag.objectIdList.size,
					onClick = { onClickTag(tag) }
				)
			}
		}
	}
}

@Composable
private fun ListItem(
	text : String = "",
	color : Color? = null,
	icon : Int = R.drawable.ic_tag,
	size : Int = 0,
	onClick : () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() }
	) {
		Spacer(modifier = Modifier.height(12.dp))
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth()
		) {
			Spacer(modifier = Modifier.width(16.dp))

			Icon(
				painter = painterResource(id = icon),
				contentDescription = text,
				tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				modifier = Modifier.requiredSize(ICON_SIZE)
			)

			Spacer(modifier = Modifier.width(16.dp))

			Box(
				modifier = Modifier
					.background(
						color = color ?: Color.Transparent,
						shape = MaterialTheme.shapes.small
					)
			) {
				Text(
					text = text,
					color = color?.getInverseBWColor() ?: MaterialTheme.colorScheme.onBackground,
					style = MaterialTheme.typography.bodyMedium,
					modifier = Modifier.padding(12.dp, 6.dp)
				)
			}

			if (size > 0) {
				Spacer(modifier = Modifier.width(12.dp))
				Spacer(modifier = Modifier.weight(1f))
				Text(
					text = "$size",
					color = MaterialTheme.colorScheme.onBackground,
					style = MaterialTheme.typography.bodyMedium,
				)
				Spacer(modifier = Modifier.width(24.dp))
			}

		}

		Spacer(modifier = Modifier.height(12.dp))
	}
}
