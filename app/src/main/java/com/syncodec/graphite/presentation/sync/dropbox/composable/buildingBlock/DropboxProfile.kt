package com.syncodec.graphite.presentation.sync.dropbox.composable.buildingBlock

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R

@Preview
@Composable
fun DropboxProfile(
	modifier : Modifier = Modifier,
	email : String? = null,
	name : String? = null,
	profilePhotoUrl : String? = null,
	spaceTotal : Long? = null,
	spaceUsed : Long? = null,
) {
	val context = LocalContext.current
	val density = LocalDensity.current

	var height by remember { mutableStateOf(0) }

	Box(
		modifier = modifier
			.fillMaxWidth()
			.clip(MaterialTheme.shapes.medium)
			.background(
				MaterialTheme.colorScheme
					.surfaceColorAtElevation(8.dp)
					.copy(alpha = 0.17f)
			)
			.clickable {
				try {
					context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dropbox.com/home/Apps/Graphite%20Data")))
				} catch (e : Exception) {
					Toast.makeText(context, "Error opening link", Toast.LENGTH_SHORT).show()
				}
			}
			.padding(16.dp)
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
			) {
				profilePhotoUrl?.let {
					AsyncImage(
						model = ImageRequest.Builder(context)
							.data(it)
							.crossfade(300)
							.error(R.drawable.ic_user)
							.build(),
						placeholder = null,
						contentDescription = "Profile photo",
						contentScale = ContentScale.Crop,
						modifier = Modifier
							.requiredSize(with(density) { height.toDp() })
							.clip(CircleShape),
					)
					Spacer(modifier = Modifier.width(16.dp))
				}

				Column(
					modifier = Modifier
						.weight(1f)
						.onGloballyPositioned { coordinates -> if (coordinates.size.height > height) height = coordinates.size.height }
				) {
					Text(
						text = name ?: "Unknown",
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurface,
						fontWeight = FontWeight.Bold
					)
					Text(
						text = email ?: "Unknown",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface
					)
					Spacer(modifier = Modifier.height(8.dp))
					if (spaceTotal != null && spaceUsed != null) {
						val spaceTotalMb = spaceTotal / 1024 / 1024
						val spaceUsedMb = spaceUsed / 1024 / 1024
						val spaceUsedPercent = (spaceUsed.toFloat() / spaceTotal.toFloat()) * 100

						Row(
							modifier = Modifier.fillMaxWidth(),
						) {
							Text(
								text = "Used: ${spaceUsedMb}MB / ${spaceTotalMb}MB",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurface,
							)
							Spacer(modifier = Modifier.weight(1f))
							Text(
								text = "${spaceUsedPercent.toInt()}%",
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurface,
							)
						}

						Spacer(modifier = Modifier.height(6.dp))

						var startAnimation by remember { mutableStateOf(false) }
						val progress by animateFloatAsState(
							targetValue = if (startAnimation) (spaceUsed.toFloat() / spaceTotal.toFloat()) else 0f,
							animationSpec = tween(2400),
							label = ""
						)

						LaunchedEffect(key1 = startAnimation) { startAnimation = true }

						LinearProgressIndicator(
							progress = progress,
							trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.31f),
							color = if (spaceUsedPercent > 90) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
							strokeCap = StrokeCap.Round,
							modifier = Modifier
								.fillMaxWidth()
								.height(4.dp)
						)
					}
				}
			}

			val spaceUsedPercent = spaceUsed?.toFloat()?.div(spaceTotal?.toFloat() ?: 1f) ?: 0f

			if (spaceUsedPercent > 0.9) {
				Spacer(modifier = Modifier.height(12.dp))
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.fillMaxWidth()
						.clip(MaterialTheme.shapes.medium)
						.background(MaterialTheme.colorScheme.errorContainer)
						.padding(16.dp, 12.dp)
				) {

					Icon(
						painter = painterResource(id = R.drawable.ic_warning),
						contentDescription = "Warning",
						tint = MaterialTheme.colorScheme.onErrorContainer,
					)
					Spacer(modifier = Modifier.width(12.dp))
					Text(
						text = "You are running out of space. Synchronization might fail if no more space is available.",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onErrorContainer,
					)
				}
			}
		}
	}
}
