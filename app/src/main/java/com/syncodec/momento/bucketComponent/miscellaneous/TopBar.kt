package com.syncodec.momento.bucketComponent.miscellaneous

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.custom.button.StateButton
import com.syncodec.momento.custom.button.StateData
import com.syncodec.momento.database.bucket.BucketItemType
import compose.icons.TablerIcons
import compose.icons.tablericons.*


@Composable
fun TopBar(
	bucketTitle: String,
	bucketItemType: BucketItemType.Type,
	showStateSelector: Boolean,
	onClick : (BucketActivity.Click) -> Unit
) {
	var currentState by remember { mutableStateOf(0) }
	LaunchedEffect(key1 = currentState) {
		onClick(
			when(currentState)  {
				0 -> BucketActivity.Click.STATE_ALPHA
				1 -> BucketActivity.Click.STATE_BETA
				2 -> BucketActivity.Click.STATE_GAMMA
				3 -> BucketActivity.Click.STATE_DELTA
				else ->BucketActivity.Click.STATE_ALPHA
			}
		)
	}

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.secondaryContainer)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(64.dp)
				.padding(8.dp)
		) {
			IconButton(
				onClick = { onClick(BucketActivity.Click.BACK) },
			) {
				Icon(
					imageVector = TablerIcons.ArrowBack,
					contentDescription = "Back",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}

			Spacer(modifier = Modifier.width(8.dp))

			Text(
				text = bucketTitle,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSecondaryContainer
			)

			Spacer(modifier = Modifier.weight(1f))

			IconButton(
				onClick = { onClick(BucketActivity.Click.SEARCH) },
			) {
				Icon(
					imageVector = TablerIcons.Search,
					contentDescription = "Search",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}

			IconButton(
				onClick = { onClick(BucketActivity.Click.MENU) },
			) {
				Icon(
					imageVector = TablerIcons.Dots,
					contentDescription = "Menu",
					tint = MaterialTheme.colorScheme.onSecondaryContainer,
				)
			}
		}

		StateSelectorCard(
			bucketItemType = bucketItemType,
			showStateSelector = showStateSelector,
			currentState = currentState
		) { currentState = it }
	}
}

@Composable
private fun StateSelectorCard(
	bucketItemType: BucketItemType.Type,
	showStateSelector: Boolean,
	currentState: Int,
	onClick: (Int) -> Unit
) {
	val stateList = when(bucketItemType) {
		BucketItemType.Type.TODO -> listOf(
			StateData(title = "All", icon = TablerIcons.Notes, color = MaterialTheme.colorScheme.onBackground),
			StateData(title = "To Read", icon = TablerIcons.Clock, color = MaterialTheme.colorScheme.primary),
			StateData(title = "Reading", icon = TablerIcons.Book, color = Color(245, 118, 26)),
			StateData(title = "Read", icon = TablerIcons.Check, color = Color(81, 146, 89)),
		)
		BucketItemType.Type.BOOKS -> listOf(
			StateData(title = "All", icon = TablerIcons.Notes, color = MaterialTheme.colorScheme.onBackground),
			StateData(title = "To Read", icon = TablerIcons.Clock, color = MaterialTheme.colorScheme.primary),
			StateData(title = "Reading", icon = TablerIcons.Book, color = Color(245, 118, 26)),
			StateData(title = "Read", icon = TablerIcons.Check, color = Color(81, 146, 89)),
		)
		BucketItemType.Type.SHOWS -> listOf(
			StateData(title = "All", icon = TablerIcons.Notes, color = MaterialTheme.colorScheme.onBackground),
			StateData(title = "To Watch", icon = TablerIcons.Clock, color = MaterialTheme.colorScheme.primary),
			StateData(title = "Watching", icon = TablerIcons.DeviceTv, color = Color(245, 118, 26)),
			StateData(title = "Watched", icon = TablerIcons.Check, color = Color(81, 146, 89)),
		)
		BucketItemType.Type.MEDIA -> listOf(
			StateData(title = "All", icon = TablerIcons.Notes, color = MaterialTheme.colorScheme.onBackground),
			StateData(title = "To Watch", icon = TablerIcons.Clock, color = MaterialTheme.colorScheme.primary),
			StateData(title = "Watching", icon = TablerIcons.DeviceTv, color = Color(245, 118, 26)),
			StateData(title = "Watched", icon = TablerIcons.Check, color = Color(81, 146, 89)),
		)
		BucketItemType.Type.LINKS -> listOf(
			StateData(title = "All", icon = TablerIcons.Notes, color = MaterialTheme.colorScheme.onBackground),
			StateData(title = "To Watch", icon = TablerIcons.Clock, color = MaterialTheme.colorScheme.primary),
			StateData(title = "Watching", icon = TablerIcons.DeviceTv, color = Color(245, 118, 26)),
			StateData(title = "Watched", icon = TablerIcons.Check, color = Color(81, 146, 89)),
		)
	}

	AnimatedVisibility(visible = showStateSelector) {
		Column(modifier = Modifier.fillMaxWidth()) {
			StateButton(
				stateList = stateList,
				currentState = currentState,
				modifier = Modifier
					.padding(8.dp, 0.dp)
					.height(32.dp)
			) { onClick(it) }

			Spacer(modifier = Modifier.height(8.dp))
		}
	}
}
