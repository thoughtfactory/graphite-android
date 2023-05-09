package com.syncodec.graphite.presentation.sync.googleDrive.composable.buildingBlobk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.api.services.drive.model.About
import com.syncodec.graphite.R
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.presentation.sync.dropbox.composable.buildingBlock.DropboxProfile
import com.syncodec.graphite.presentation.sync.googleDrive.GoogleDriveSyncActivity


@Preview
@Composable
fun ConnectionCard(
	aboutState : GoogleDriveSyncActivity.Companion.AboutState = GoogleDriveSyncActivity.Companion.AboutState.Init,
) {
	when (aboutState) {
		is GoogleDriveSyncActivity.Companion.AboutState.Init -> null
		is GoogleDriveSyncActivity.Companion.AboutState.Loading -> null
		is GoogleDriveSyncActivity.Companion.AboutState.NotLoggedIn -> null
		is GoogleDriveSyncActivity.Companion.AboutState.Error -> null
		is GoogleDriveSyncActivity.Companion.AboutState.Success -> AboutSuccessCard(
			aboutState = aboutState
		)
	}
}

@Composable
private fun AboutSuccessCard(
	aboutState : GoogleDriveSyncActivity.Companion.AboutState.Success
) {
	val context = LocalContext.current
	val density = LocalDensity.current

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp)
			.background(
				MaterialTheme.colorScheme
					.surfaceColorAtElevation(8.dp)
					.copy(alpha = 0.17f),
				MaterialTheme.shapes.medium
			)
			.padding(16.dp),
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
		) {
			aboutState.about.user.photoLink?.let {
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
						.requiredSize(64.dp)
						.clip(CircleShape),
				)
				Spacer(modifier = Modifier.width(16.dp))
			}
			Spacer(modifier = Modifier.width(0.dp))
			Column {
				aboutState.about.user.displayName?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onSurface,
						fontWeight = FontWeight.Bold
					)
				}
				aboutState.about.user.emailAddress?.let {
					Text(
						text = it,
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurface
					)
				}
			}
		}
	}
}
