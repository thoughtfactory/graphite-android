package com.syncodec.graphite.presentation.settings.composable.screen

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseUser
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScreen
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@Composable
fun SettingsScreen(
	scrollState : ScrollState,
	currentUser : FirebaseUser?,
	onClickLogin: () -> Unit,
	onClickProfile: () -> Unit,
) {
	val activity = LocalContext.current as SettingsActivity

	val uriHandler = LocalUriHandler.current
	
	GenericSettingsScreen(
		title = "Settings",
		scrollState = scrollState
	) {
		Crossfade(targetState = currentUser) {
			if (it == null) {
				SettingsButton(
					title = "Login",
					icon = R.drawable.ic_account,
					onClick = onClickLogin
				)
			} else {
				ProfileButton(
					displayName = it.displayName ?: "Anonymous",
					email = it.email ?: "Anonymous",
					photoUrl = it.photoUrl,
					onClick = onClickProfile
				)
			}
		}

		SettingsButton(
			title = "Subscription",
			subTitle = "Get access to all the features",
			icon = R.drawable.ic_subscription
		) {}

		SettingsButton(
			title = "Preferences",
			subTitle = "Themes, font style, etc.",
			icon = R.drawable.ic_preference
		) { activity.navigator.value = SettingsActivity.Companion.Navigator.PREFERENCES }

		SettingsButton(
			title = "Security",
			subTitle = "Don't let intruders see your data",
			icon = R.drawable.ic_lock_close
		) { activity.navigator.value = SettingsActivity.Companion.Navigator.SECURITY }

		SettingsButton(
			title = "Extensions",
			subTitle = "Manage extensions",
			icon = R.drawable.ic_extensions
		) { activity.navigator.value = SettingsActivity.Companion.Navigator.PREFERENCES }

		SettingsButton(
			title = "Backup & Restore",
			subTitle = "Backup your data to the cloud and local storage",
			icon = R.drawable.ic_data
		) { activity.navigator.value = SettingsActivity.Companion.Navigator.BACKUP }

		SettingsButton(
			title = "Synchronization",
			subTitle = "Keep your data in sync across all your devices",
			icon = R.drawable.ic_sync
		) { activity.navigator.value = SettingsActivity.Companion.Navigator.SYNC }

		SettingsButton(
			title = "Terms of Service",
			subTitle = "Read our terms of service",
			icon = R.drawable.ic_terms
		) {
			uriHandler.openUri("https://graphite.syncodec.com/terms.html")
		}

		SettingsButton(
			title = "Privacy Policy",
			subTitle = "Read our privacy policy",
			icon = R.drawable.ic_policy
		) {
			uriHandler.openUri("https://graphite.syncodec.com/policy.html")
		}

		SettingsButton(
			title = "About Us",
			subTitle = "Hello there...",
			icon = R.drawable.ic_info
		) {}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileButton(
	displayName: String,
	email: String,
	photoUrl: Uri?,
	onClick: () -> Unit
) {
	val context = LocalContext.current

	Card(
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.31f),
			contentColor = MaterialTheme.colorScheme.onSurface,
		),
		modifier = Modifier.padding(12.dp, 4.dp),
		onClick = onClick
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			if (photoUrl == null) {
				Icon(
					painter = painterResource(id = R.drawable.ic_account),
					contentDescription = email,
					modifier = Modifier.requiredSize(48.dp)
				)

			} else {
				AsyncImage(
					model = ImageRequest.Builder(context)
						.data(photoUrl)
						.crossfade(300)
						.build(),
					placeholder = null,
					contentDescription = email,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.requiredSize(48.dp)
						.clip(CircleShape),
				)
			}

			Spacer(modifier = Modifier.width(12.dp))

			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = displayName,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurface,
					fontWeight = FontWeight.Bold
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = email,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
				)
			}
			Spacer(modifier = Modifier.width(16.dp))
		}
	}
}
