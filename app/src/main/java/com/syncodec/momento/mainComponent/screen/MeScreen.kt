package com.syncodec.momento.mainComponent.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.MainActivity
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.MainTopBar
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import compose.icons.TablerIcons
import compose.icons.tablericons.Book
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun MeScreen() {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val screenWidth = configuration.screenWidthDp.dp

	val viewModel: MainViewModel = viewModel()
	val scope = rememberCoroutineScope()
	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		scope.launch {
			viewModel.mainActivityState.bottomSheetState.show()
		}
	}

	Scaffold(
		topBar = {
			MainTopBar(
				showBackground = true,
				openSheet = openSheet
			)
		}
	) {
		LazyVerticalGrid(
			cells = GridCells.Adaptive(minSize = screenWidth / 3),
			modifier = Modifier.padding(4.dp)
		) {
			items(5) {
				Card(
					modifier = Modifier
						.padding(6.dp)
						.height(96.dp),
					elevation = 0.dp,
					backgroundColor = Color(244, 244, 245, 255),
					onClick = {}
				) {
					Column(
						modifier = Modifier
							.padding(12.dp)
							.fillMaxWidth()
							.fillMaxHeight(),
						verticalArrangement = Arrangement.SpaceBetween,
						horizontalAlignment = Alignment.Start
					) {
						Row(
							horizontalArrangement = Arrangement.SpaceBetween,
							verticalAlignment = Alignment.CenterVertically,
							modifier = Modifier.fillMaxWidth()
						) {
							Icon(
								imageVector = TablerIcons.Book,
								contentDescription = null,
								modifier = Modifier
									.size(24.dp)
							)
							Text(
								text = "71",
								style = MaterialTheme.typography.bodyMedium,
								modifier = Modifier.alpha(0.4f)
							)
						}
						Text(
							text = "Books 2021",
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.ExtraBold
						)
					}
				}
			}
		}
	}
}
