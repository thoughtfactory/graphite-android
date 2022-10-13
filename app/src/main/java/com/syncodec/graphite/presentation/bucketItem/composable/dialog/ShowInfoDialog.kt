package com.syncodec.graphite.presentation.bucketItem.composable.dialog

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.dialog.GenericDialog


@Composable
fun ShowInfoDialog(
	showDialog : Boolean,
	movieId : String?,
	tvId: String?,
	movieImdbId : String?,
	movieOriginalTitle : String?,
	onDismiss : () -> Unit,
) {
	val context = LocalContext.current
	val uriHandler = LocalUriHandler.current

	GenericDialog(
		showDialog = showDialog,
		onDismissRequest = onDismiss
	) {
		Text(
			text = movieOriginalTitle ?: "Info",
			style = MaterialTheme.typography.headlineMedium,
			color = MaterialTheme.colorScheme.onSurface,
		)

		Spacer(modifier = Modifier.height(12.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f), RoundedCornerShape(12.dp))
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp, 4.dp, 4.dp, 4.dp)
			) {
				Text(
					text = "TMDB ID: $movieId",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier
				)

				Spacer(modifier = Modifier.weight(1f))

				MenuButton(icon = R.drawable.ic_open_link) {
					try {
						when {
							movieId != null -> uriHandler.openUri("https://www.themoviedb.org/movie/$movieId")
							tvId != null -> uriHandler.openUri("https://www.themoviedb.org/tv/$tvId")
						}
					} catch (e : Exception) {
						Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
					}
				}
			}
		}


		Spacer(modifier = Modifier.height(6.dp))

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f), RoundedCornerShape(12.dp))
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp, 4.dp, 4.dp, 4.dp)
			) {
				Text(
					text = "IMDB ID: $movieImdbId",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface,
					modifier = Modifier
				)

				Spacer(modifier = Modifier.weight(1f))

				MenuButton(icon = R.drawable.ic_open_link) {
					try {
						uriHandler.openUri("https://www.imdb.com/title/$movieImdbId/")
					} catch (e : Exception) {
						Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
					}
				}
			}
		}

		Spacer(modifier = Modifier.height(12.dp))
	}
}
