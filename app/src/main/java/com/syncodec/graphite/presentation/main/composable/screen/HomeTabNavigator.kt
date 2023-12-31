package com.syncodec.graphite.presentation.main.composable.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.common.button.FilterButton
import com.syncodec.graphite.presentation.common.tab.GenericTabRow
import com.syncodec.graphite.presentation.common.tab.TabItem
import com.syncodec.graphite.presentation.main.HomeComponent

@Preview
@Composable
fun HomeTabNavigator(
	currentRoute: HomeComponent = HomeComponent.Note,
	isVisible: Boolean = false,
	onNavigate: (HomeComponent) -> Unit = {}
) {
	AnimatedVisibility(
		visible = isVisible,
		enter = expandVertically(tween(ANIMATION_DURATION_MILLIS)),
		exit = shrinkVertically(tween(ANIMATION_DURATION_MILLIS)),
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(modifier = Modifier.width(12.dp))
			GenericTabRow(
				tabItemList = listOf(
					TabItem(text = stringResource(id = HomeComponent.Note.title), icon = HomeComponent.Note.icon) { onNavigate(HomeComponent.Note) },
					TabItem(text = stringResource(id = HomeComponent.Bucket.title), icon = HomeComponent.Bucket.icon) { onNavigate(HomeComponent.Bucket) },
					TabItem(text = stringResource(id = HomeComponent.Notebook.title), icon = HomeComponent.Notebook.icon) { onNavigate(HomeComponent.Notebook) },
				),
				selectedTabIndex = when (currentRoute) {
					HomeComponent.Note -> 0
					HomeComponent.Bucket -> 1
					HomeComponent.Notebook -> 2
				},
				modifier = Modifier.weight(1f)
			)
			FilterButton()

			Spacer(modifier = Modifier.width(4.dp))
		}
	}
}
