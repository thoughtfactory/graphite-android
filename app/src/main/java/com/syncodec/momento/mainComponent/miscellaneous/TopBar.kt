package com.syncodec.momento.mainComponent.miscellaneous

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.MainActivity
import com.syncodec.momento.R
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.searchComponent.SearchActivity
import compose.icons.TablerIcons
import compose.icons.tablericons.Search
import compose.icons.tablericons.Trash
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun TopBar(
	isSelected: Boolean,
	selectedItemSize: Int,
	onClick: (MainActivity.Click, Any?) -> Unit,
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(88.dp)
			.background(MaterialTheme.colorScheme.secondaryContainer),
	) {
		Card(
			elevation = 0.dp,
			shape = RoundedCornerShape(12.dp),
			backgroundColor = MaterialTheme.colorScheme.background,
			modifier = Modifier
				.fillMaxSize()
				.padding(12.dp, 16.dp)
		) {
			AnimatedContent(
				targetState = isSelected,
				transitionSpec = { fadeIn(tween(600)) with fadeOut(tween(600)) }
			) {
				if (it) {
					DeleteCard(selectedItemSize = selectedItemSize) { onClick(MainActivity.Click.SHOW_DELETE, null) }
				} else {
					ToolBarCard()
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ToolBarCard() {
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val viewModel: MainViewModel = viewModel()

	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		viewModel.activityState.bottomSheetType.value = bottomSheetType
		scope.launch {
			viewModel.activityState.bottomSheetState.show()
		}
	}
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier.padding(8.dp)
	) {
		IconButton(
			onClick = {
				openSheet(BottomSheetType.MenuBottomSheet)
			},
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_icon),
				contentDescription = null,
				tint = Color.Unspecified,
				modifier = Modifier
					.size(48.dp)
					.padding(6.dp)
			)
		}

		Text(
			text = "MOMENTO",
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.secondary,
		)

		Spacer(modifier = Modifier.weight(1f))

		IconButton(
			onClick = {
				Intent(context, SearchActivity::class.java).apply {
					context.startActivity(this)
				}
			},
		) {
			Icon(
				imageVector = TablerIcons.Search,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onBackground,
			)
		}
	}
}

@Composable
private fun DeleteCard(
	selectedItemSize: Int,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxSize()
			.padding(24.dp, 0.dp, 8.dp, 0.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		Text(
			text = if (selectedItemSize == 0) "Select items to delete" else if (selectedItemSize == 1) "1 item selected" else "$selectedItemSize items selected",
			style = MaterialTheme.typography.bodyLarge,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground
		)

		IconButton(onClick = { onClick() }) {
			Icon(
				imageVector = TablerIcons.Trash,
				contentDescription = "Delete items",
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier
			)
		}
	}

}
