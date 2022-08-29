package com.syncodec.graphite.presentation.main.composable.buildingBlock

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.presentation.main.MainViewModel


@Composable
fun QuoteCard() {
	val context = LocalContext.current
	val viewModel: MainViewModel = viewModel()

	val quoteObject by viewModel.quoteObject

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(96.dp)
			.padding(12.dp, 0.dp)
	) {
//		AsyncImage(
//			model = ImageRequest.Builder(context)
//				.data(quoteObject?.bg)
//				.crossfade(300)
//				.build(),
//			placeholder = null,
//			contentDescription = null,
//			contentScale = ContentScale.Crop,
//			modifier = Modifier
//				.fillMaxSize()
//				.blur(8.dp, BlurredEdgeTreatment.Rectangle),
//		)

	}
}
