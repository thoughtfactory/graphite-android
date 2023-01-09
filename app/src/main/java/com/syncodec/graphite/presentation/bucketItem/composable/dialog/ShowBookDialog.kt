package com.syncodec.graphite.presentation.bucketItem.composable.dialog

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun ShowBookDialog(
	showDialog : Boolean,
	bookKey: String?,
	onDismiss : () -> Unit,
) {
	val context = LocalContext.current
	val uriHandler = LocalUriHandler.current

	GenericDialog(
		showDialog = showDialog,
		title = "Info",
		onDismissRequest = onDismiss
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f), RoundedCornerShape(12.dp))
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp, 8.dp, 4.dp, 8.dp)
			) {
				Column(
					modifier = Modifier.weight(1f)
				) {
					Text(
						text = "Open Library Key:",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
					)

					Spacer(modifier = Modifier.height(6.dp))

					Text(
						text = "$bookKey",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
					)
				}

				Spacer(modifier = Modifier.width(12.dp))

				MenuButton(icon = R.drawable.ic_launch) {
					try {
						uriHandler.openUri("https://openlibrary.org$bookKey")
					} catch (e : Exception) {
						Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
					}
				}
			}
		}
	}
}
