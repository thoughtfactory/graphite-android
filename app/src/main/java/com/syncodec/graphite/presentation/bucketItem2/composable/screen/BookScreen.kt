package com.syncodec.graphite.presentation.bucketItem2.composable.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookAuthorList
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookDescription
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookFirstPublishYear
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookKey
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookPageCount
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionBookTitle
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionOnChangeState
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionState
import com.syncodec.graphite.presentation.bucketItem2.composable.LocalCompositionThumbnail
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData


@Composable
fun BookScreen() {
	val context = LocalContext.current

	val bookKey = LocalCompositionBookKey.current
	val bookTitle = LocalCompositionBookTitle.current
	val bookAuthors = LocalCompositionBookAuthorList.current
	val bookDescription = LocalCompositionBookDescription.current
	val bookPageCount = LocalCompositionBookPageCount.current
	val bookPublishedDate = LocalCompositionBookFirstPublishYear.current

	val thumbnail = LocalCompositionThumbnail.current

	val currentState = LocalCompositionState.current ?: 0
	val onChangeState = LocalCompositionOnChangeState.current

	val uriHandler = LocalUriHandler.current

	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	val stateList = listOf(
		StateData(
			title = "To Read",
			icon = R.drawable.ic_book,
		),
		StateData(
			title = "Reading",
			icon = R.drawable.ic_clock,
		),
		StateData(
			title = "Read",
			icon = R.drawable.ic_check,
		),
	)

	val scrollState = rememberScrollState()

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(scrollState),
	) {
		Spacer(modifier = Modifier.height(8.dp))

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.width(screenWidth / 2)
				.aspectRatio(0.6666f)
				.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
				.clip(RoundedCornerShape(24.dp))
		) {
			AsyncImage(
				model = ImageRequest.Builder(context)
					.data(thumbnail)
					.crossfade(300)
					.build(),
				placeholder = null,
				contentDescription = bookTitle,
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize(),
			)
		}

		Spacer(modifier = Modifier.height(24.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
				.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), RoundedCornerShape(16.dp))
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			) {
				Row(
					verticalAlignment = Alignment.Bottom,
					modifier = Modifier.fillMaxWidth(),
				) {
					Text(
						text = bookTitle ?: "",
						style = MaterialTheme.typography.titleLarge,
						color = MaterialTheme.colorScheme.onSurface,
						fontWeight = FontWeight.Bold,
						overflow = TextOverflow.Ellipsis,
						modifier = Modifier.weight(1f),
					)

					Spacer(modifier = Modifier.width(16.dp))

					Text(
						text = bookPageCount?.toString()?.plus(" pages") ?: "",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface
					)
				}

				Spacer(modifier = Modifier.height(4.dp))

				Row(
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = bookAuthors.joinToString(", "),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface,
					)

					Spacer(modifier = Modifier.weight(1f))

					Spacer(modifier = Modifier.width(8.dp))

					Text(
						text = bookPublishedDate ?: "",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface
					)
				}
			}
		}

		Spacer(modifier = Modifier.height(8.dp))

		StateButton(
			stateList = stateList,
			currentState = currentState,
			containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.47f),
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
				.height(36.dp),
			onStateChange = onChangeState,
		)

		Spacer(modifier = Modifier.height(8.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
				.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), RoundedCornerShape(16.dp))
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			) {
				Text(
					text = "Description",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold,
				)

				Spacer(modifier = Modifier.height(4.dp))

				Text(
					text = bookDescription ?: "",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 6,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}

		Spacer(modifier = Modifier.height(8.dp))

		Button(
			onClick = {
				try {
					uriHandler.openUri("https://openlibrary.org$bookKey")
				} catch (e : Exception) {
					Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
				}
			},
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
		) {
			Text(text = "Open in OpenLibrary")
		}

		Spacer(modifier = Modifier.height(32.dp))
	}
}
