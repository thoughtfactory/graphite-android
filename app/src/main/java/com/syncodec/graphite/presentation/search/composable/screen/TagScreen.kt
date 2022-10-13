package com.syncodec.graphite.presentation.search.composable.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.TagObject


@Composable
fun TagScreen(
	tagList : List<TagObject>,
	onClickFavorite : () -> Unit,
	onClickWithAttachments: () -> Unit,
	onClickTag : (TagObject) -> Unit,
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		item {
			ListItem(
				title = "Favourite",
				icon = R.drawable.ic_favourite
			) { onClickFavorite() }
		}
		item {
			Spacer(
				modifier = Modifier
					.fillMaxWidth(0.71f)
					.height(2.dp)
					.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.31f))
			)
		}
		item {
			ListItem(
				title = "With attachments",
				icon = R.drawable.ic_attachment
			) { onClickWithAttachments() }
		}
		item {
			Spacer(
				modifier = Modifier
					.fillMaxWidth(0.71f)
					.height(2.dp)
					.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.31f))
			)
		}
		if (tagList.isNotEmpty()) {
			item {
				Text(
					text = "Tags",
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
					modifier = Modifier
						.padding(16.dp, 24.dp, 16.dp, 0.dp)
						.fillMaxWidth()
				)
			}
		}
		tagList.forEach {
			item {
				ListItem(
					title = it.tag,
					color = Color(it.color),
					icon = R.drawable.ic_hashtag
				) { onClickTag(it) }
				Spacer(
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
private fun ListItem(
	title:String,
	color : Color? = null,
	icon: Int,
	onClick : () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() }
	) {
		Spacer(modifier = Modifier.height(20.dp))
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth()
		) {
			Spacer(modifier = Modifier.width(16.dp))

			Icon(
				painter = painterResource(id = icon),
				contentDescription = title,
				tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
				modifier = Modifier.requiredSize(24.dp)
			)

			Spacer(modifier = Modifier.width(16.dp))

			Text(
				text = title,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold
			)

			Spacer(modifier = Modifier.weight(1f))

			color?.let {
				Box(
					modifier = Modifier
						.width(64.dp)
						.height(8.dp)
						.background(it, RoundedCornerShape(50))
				)
			}
			Spacer(modifier = Modifier.width(16.dp))
		}
		Spacer(modifier = Modifier.height(20.dp))
	}
}
