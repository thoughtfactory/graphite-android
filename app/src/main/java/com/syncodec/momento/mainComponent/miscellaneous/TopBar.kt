package com.syncodec.momento.mainComponent.miscellaneous

import android.content.Intent
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.R
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import com.syncodec.momento.searchComponent.SearchActivity
import com.syncodec.momento.ui.theme.Stardos
import compose.icons.TablerIcons
import compose.icons.tablericons.Search
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopBar() {
	val scope = rememberCoroutineScope()
	val context = LocalContext.current
	val viewModel: MainViewModel = viewModel()

	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		viewModel.activityState.bottomSheetType.value = bottomSheetType
		scope.launch {
			viewModel.activityState.bottomSheetState.show()
		}
	}

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
							.padding(6.dp, 6.dp)
					)
				}

				Text(
					text = "MOMENTO",
					style = TextStyle(
						fontFamily = Stardos,
						fontSize = 16.sp,
						fontWeight = FontWeight.Bold,
						letterSpacing = 2.sp,
					),
					color = MaterialTheme.colorScheme.secondary,
					modifier = Modifier
						.height(20.dp)
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
	}
}
