package com.syncodec.momento.mainComponent.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.R
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
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

	Column(
		modifier = Modifier
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Spacer(modifier = Modifier.height(96.dp))
		
		Card(
			modifier = Modifier
				.requiredSize(96.dp),
			shape = RoundedCornerShape(12.dp),
			elevation = 16.dp
		) {
			Image(
				painter = painterResource(id = R.drawable.debug_profile_picture),
				contentDescription = null
			)
		}

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = "Bruce Wayne",
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.primary
		)

		Text(
			text = "bruce.wayne@gmail.com",
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground
		)
	}
}
