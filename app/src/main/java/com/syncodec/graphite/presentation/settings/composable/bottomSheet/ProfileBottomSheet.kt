package com.syncodec.graphite.presentation.settings.composable.bottomSheet

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseUser
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetHeader
import com.syncodec.graphite.presentation.common.bottomSheet.BottomSheetStrip
import com.syncodec.graphite.presentation.common.bottomSheet.GenericBottomSheet
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.dialog.SettingsDialogType
import com.syncodec.graphite.presentation.ui.DeleteContainer
import com.syncodec.graphite.presentation.ui.DeleteContent


@Composable
fun ProfileBottomSheet(
	firebaseUser : FirebaseUser?,
	signOut : () -> Unit,
	closeSheet : () -> Unit
) {
	GenericBottomSheet(
		title = "Profile",
		icon = R.drawable.ic_account
	) {

		ProfileView(
			displayName = firebaseUser?.displayName ?: "Anonymous",
			email = firebaseUser?.email ?: "Anonymous",
			photoUrl = firebaseUser?.photoUrl,
			userForTimeInDays = null
		)

		Spacer(modifier = Modifier.height(8.dp))

		Button(
			modifier = Modifier.fillMaxWidth(),
			onClick = {
				closeSheet()
				signOut()
			}
		) {
			Text(text = "Sign Out")
		}

//		Button(
//			colors = ButtonDefaults.buttonColors(
//				containerColor = Color.Companion.DeleteContainer,
//				contentColor = Color.Companion.DeleteContent,
//			),
//			modifier = Modifier
//				.fillMaxWidth()
//				.padding(24.dp, 0.dp),
//			onClick = { openDialog(SettingsDialogType.DELETE_ACCOUNT, null) }
//		) {
//			Text(text = "Delete Account")
//		}
	}
}

@Composable
fun ProfileView(
	displayName : String,
	email : String,
	photoUrl : Uri?,
	userForTimeInDays : Long?
) {
	val context = LocalContext.current

	Box(
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			if (photoUrl == null) {
				Icon(
					painter = painterResource(id = R.drawable.ic_account),
					contentDescription = email,
					tint = MaterialTheme.colorScheme.onBackground,
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
					color = MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Bold
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = email,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				)
			}

			Spacer(modifier = Modifier.width(16.dp))
		}
	}
	if (userForTimeInDays != null) {
		Spacer(modifier = Modifier.height(6.dp))
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(24.dp, 0.dp)
				.background(MaterialTheme.colorScheme.background.copy(alpha = 0.31f), RoundedCornerShape(12.dp))
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "User for $userForTimeInDays days and counting...",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.padding(12.dp)
				)
			}
		}
	}
}
