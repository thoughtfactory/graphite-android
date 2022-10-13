package com.syncodec.graphite.presentation.settings.composable.bottomSheet

import android.net.Uri
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetKeyText
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.button.LargeButton
import com.syncodec.graphite.presentation.common.button.stateButton.StateButton
import com.syncodec.graphite.utils.SortBy
import com.syncodec.graphite.utils.SortOn
import com.syncodec.graphite.utils.ViewType


@Composable
fun ProfileBottomSheet(
	closeSheet: () -> Unit
) {
	val auth = FirebaseAuth.getInstance()

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxWidth()
			.background(MaterialTheme.colorScheme.surface)
	) {

		BottomSheetStrip()

		BottomSheetHeader(
			title = "Profile",
			icon = R.drawable.ic_account
		)

		Spacer(modifier = Modifier.height(8.dp))

		ProfileView(
			displayName = auth.currentUser?.displayName ?: "Anonymous",
			email = auth.currentUser?.email ?: "Anonymous",
			photoUrl = auth.currentUser?.photoUrl
		) {}

		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Composable
fun ProfileView(
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
