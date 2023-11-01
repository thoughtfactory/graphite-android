package com.syncodec.graphite.presentation.sync.dropbox2.composable.dropboxConnectionCard

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.di.cloud.dropbox.DropboxApi
import com.syncodec.graphite.di.network.NetworkRequest
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS
import com.syncodec.graphite.presentation.base.ICON_SIZE
import com.syncodec.graphite.presentation.common.info.InfoCardType


@Preview
@Composable
fun DropboxProfileCard(
	modifier: Modifier = Modifier,
	dropboxAccountInfo: NetworkRequest<DropboxApi.Companion.DropboxAccountInfo> = NetworkRequest.Init,
) {
	Log.d("npr71", "DropboxProfileCard: $dropboxAccountInfo")
	AnimatedContent(
		targetState = dropboxAccountInfo,
		transitionSpec = { fadeIn(tween(ANIMATION_DURATION_MILLIS)) togetherWith fadeOut(tween(ANIMATION_DURATION_MILLIS)) },
		label = "DropboxAccountInfo_animation"
	) {
		when (it) {
			is NetworkRequest.Init -> Unit
			is NetworkRequest.Loading -> DropboxConnectingCard(modifier = modifier)
			is NetworkRequest.Success -> OutlinedCard(
				modifier = modifier,
				colors = CardDefaults.outlinedCardColors(
					containerColor = Color.Transparent
				)
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
				) {
					DropboxAccountView(
						profilePhotoUrl = it.data.profilePhotoUrl,
						name = it.data.name,
						email = it.data.email
					)
					Spacer(modifier = Modifier.height(12.dp))
					DropboxSpaceView(dropboxSpaceUsage = it.data.spaceUsage)
				}
			}
			is NetworkRequest.Error -> InfoCardType.Error(
				modifier = modifier,
				title = stringResource(R.string.connection_Error),
				description = stringResource(R.string.error_retrieving_dropbox_account_info),
			)
		}
	}
}

@Preview
@Composable
private fun DropboxAccountView(
	profilePhotoUrl: String? = null,
	name: String? = "Dropbox User",
	email: String = "dropboxUser@example.com"
) {
	val context = LocalContext.current

	Row(
		modifier = Modifier.fillMaxWidth(),
		verticalAlignment = Alignment.CenterVertically
	) {
		SubcomposeAsyncImage(
			model = ImageRequest.Builder(context)
				.data(profilePhotoUrl)
				.memoryCachePolicy(CachePolicy.ENABLED)
				.memoryCacheKey(profilePhotoUrl)
				.crossfade(ANIMATION_DURATION_MILLIS)
				.build(),
			contentDescription = stringResource(id = R.string.thumbnail),
			error = {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_user),
					contentDescription = stringResource(id = R.string.thumbnail),
					tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
					modifier = Modifier.requiredSize(24.dp)
				)
			},
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.requiredSize(64.dp)
				.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.47f), shape = CircleShape)
		)

		Spacer(modifier = Modifier.width(16.dp))

		Column(
			modifier = Modifier.weight(1f)
		) {
			Text(
				text = name ?: stringResource(R.string.unavailable),
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = FontWeight.Bold
			)
			Text(
				text = email,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.padding(start = 2.dp)
			)
		}
	}
}

@Preview
@Composable
private fun DropboxSpaceView(
	dropboxSpaceUsage: DropboxApi.Companion.DropboxSpaceUsage = DropboxApi.Companion.DropboxSpaceUsage(used = 71, allocated = 100)
) {

	val spaceUsedMb by remember(dropboxSpaceUsage.used) { derivedStateOf { dropboxSpaceUsage.used / 1024 / 1024 } }
	val spaceTotalMb by remember(dropboxSpaceUsage.allocated) { derivedStateOf { dropboxSpaceUsage.allocated / 1024 / 1024 } }
	val spaceUsedPercent by remember(dropboxSpaceUsage.allocated, dropboxSpaceUsage.used) { derivedStateOf { (dropboxSpaceUsage.used.toFloat() / dropboxSpaceUsage.allocated.toFloat()) } }

	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		LinearProgressIndicator(
			progress = spaceUsedPercent,
			trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.71f),
			color = if (spaceUsedPercent > 0.8f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.47f),
			modifier = Modifier
				.fillMaxWidth()
				.height(8.dp)
		)

		Spacer(modifier = Modifier.height(12.dp))

		Text(
			text = "${stringResource(R.string.space_used)}: $spaceUsedMb MB",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			fontWeight = FontWeight.Bold,
		)
		Spacer(modifier = Modifier.height(2.dp))
		Text(
			text = "${stringResource(R.string.space_available)}: $spaceTotalMb MB",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			fontWeight = FontWeight.Bold,
		)
		if (spaceUsedPercent > 0.8) {
			Spacer(modifier = Modifier.height(12.dp))
			Card(
				colors = CardDefaults.cardColors(
					containerColor = MaterialTheme.colorScheme.errorContainer,
					contentColor = MaterialTheme.colorScheme.onErrorContainer
				),
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp, 12.dp)
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_warning),
						contentDescription = stringResource(R.string.running_out_of_space_message),
						modifier = Modifier.requiredSize(ICON_SIZE)
					)
					Spacer(modifier = Modifier.width(12.dp))
					Text(
						text = stringResource(R.string.running_out_of_space_message),
						style = MaterialTheme.typography.bodyMedium,
					)
				}
			}
		}
	}
}
