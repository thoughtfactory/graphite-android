package com.syncodec.graphite.presentation.bucketItem.composable.screen.bookScreen

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucketItem.composable.screen.InfoSurface
import com.syncodec.graphite.presentation.common.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateData
import com.syncodec.graphite.presentation.common.dialog.DialogType


@Preview
@Composable
fun BookScreen(
	currentState : Int = 0,
	onChangeState : (Int) -> Unit = {},
) {
	val context = LocalContext.current
	val viewModel : BookScreenViewModel = viewModel()

	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	val uriHandler = LocalUriHandler.current

	val bookKey by viewModel.bookKey.collectAsState()
	val bookTitle by viewModel.bookTitle.collectAsState()
	val bookPageCount by viewModel.bookPageCount.collectAsState()
	val bookDescription by viewModel.bookDescription.collectAsState()
	val bookPublishedDate by viewModel.bookFirstPublishYear.collectAsState()
	val bookAuthors by viewModel.bookAuthorList.collectAsState()

	val thumbnail by viewModel.thumbnail.collectAsState()

	val openDialog = LocalCompositionOpenDialog.current

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
				.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraLarge)
				.clip(MaterialTheme.shapes.extraLarge)
		) {
			thumbnail?.let {
				AsyncImage(
					model = ImageRequest.Builder(context)
						.data(it)
						.crossfade(300)
						.build(),
					placeholder = null,
					contentDescription = bookTitle,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize(),
				)
			}
		}

		Spacer(modifier = Modifier.height(24.dp))

		InfoSurface(
			onClick = {openDialog(DialogType.BOOK_INFO)}
		) {
			Column(
				modifier = Modifier.fillMaxWidth()
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
			containerColor = MaterialTheme.colorScheme.surface,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp, 0.dp)
				.height(32.dp),
			onChangeState = onChangeState,
		)

		Spacer(modifier = Modifier.height(8.dp))

		InfoSurface {
			Column(
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "Description",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold,
				)

				Text(
					text = bookDescription ?: "",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
					maxLines = 6,
					overflow = TextOverflow.Ellipsis,
				)
			}
		}

		Spacer(modifier = Modifier.height(6.dp))

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
