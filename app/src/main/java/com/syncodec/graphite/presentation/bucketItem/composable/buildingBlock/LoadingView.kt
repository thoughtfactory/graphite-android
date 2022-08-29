package com.syncodec.graphite.presentation.bucketItem.composable.buildingBlock

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.button.LargeButton
import com.syncodec.graphite.presentation.custom.button.StateButton
import com.syncodec.graphite.presentation.custom.button.StateData


@Composable
fun LoadingView() {
	LazyColumn(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp, 0.dp),
	) {
		item { Spacer(modifier = Modifier.height(24.dp)) }

		item { ThumbnailShimmerCard() }
		item { Spacer(modifier = Modifier.height(24.dp)) }

		item { BookHeaderShimmerCard() }

		item { Spacer(modifier = Modifier.height(12.dp)) }

		item {
			StateButton(
				stateList = listOf(
					StateData(
						title = "To Read",
						icon = R.drawable.ic_clock,
						stateTint = MaterialTheme.colorScheme.primary
					),
					StateData(
						title = "Reading",
						icon = R.drawable.ic_book,
						stateTint = Color(245, 118, 26)
					),
					StateData(
						title = "Read",
						icon = R.drawable.ic_done,
						stateTint = Color(81, 146, 89)
					),
				),
				currentState = 0,
				modifier = Modifier.height(32.dp)
			) { }
		}
		item { Spacer(modifier = Modifier.height(12.dp)) }

//		item{
//			ThoughtCard(
//				thoughtList = thoughtList,
//				onAction = onAction
//			)
//		}
//		item{ Spacer(modifier = Modifier.height(12.dp)) }

		item { OverviewShimmerCard() }
		item { Spacer(modifier = Modifier.height(12.dp)) }

		item {
			LargeButton(
				text = "View in Open Library",
				enabled = false,
				modifier = Modifier.fillMaxWidth()
			) {}
		}
		item { Spacer(modifier = Modifier.height(32.dp)) }

		item {
			Image(
				painter = painterResource(id = R.drawable.il_open_library),
				contentDescription = "Open Library",
				contentScale = ContentScale.Fit,
				modifier = Modifier
					.fillMaxWidth()
					.height(128.dp)
			)
		}
		item { Spacer(modifier = Modifier.height(32.dp)) }
	}
}
