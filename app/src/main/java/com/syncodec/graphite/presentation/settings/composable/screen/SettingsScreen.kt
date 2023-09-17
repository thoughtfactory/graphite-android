package com.syncodec.graphite.presentation.settings.composable.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseUser
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.pro.ProActivity
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.bottomSheet.AccountBottomSheet
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SettingsScreen(
	firebaseUser: FirebaseUser? = null,
	onClickSignIn: () -> Unit = { },
	onClickSignOut: () -> Unit = { },
	onClickDeleteAccount: () -> Unit = { },
	onNavigate: (SettingsActivity.Companion.SettingsScreen) -> Unit = {}
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val accountBottomSheetState = rememberModalBottomSheetState()
	var showAccountBottomSheet by remember { mutableStateOf(false) }

	GenericSettingsScaffold(
		title = stringResource(id = R.string.settings),
		onClickBack = { /*TODO*/ }
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			item {
				ProfileCard(
					displayName = firebaseUser?.displayName,
					email = firebaseUser?.email,
					photoUrl = firebaseUser?.photoUrl,
					isPro = true,
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.account),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_user),
					onClick = { showAccountBottomSheet = true },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.graphite_pro),
					subTitle = stringResource(id = R.string.unleash_the_full_potential_of_graphite),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_pro),
					onClick = { context.startActivity(Intent(context, ProActivity::class.java)) },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.preferences),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_preferences),
					onClick = { onNavigate(SettingsActivity.Companion.SettingsScreen.Preferences) },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.security),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_lock_close),
					onClick = { onNavigate(SettingsActivity.Companion.SettingsScreen.Security) },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.data),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_data),
					onClick = { onNavigate(SettingsActivity.Companion.SettingsScreen.Data) },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.backup_and_sync),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_network),
					onClick = { onNavigate(SettingsActivity.Companion.SettingsScreen.BackUpAndSync) },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.extensions),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_extension),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.knowledge_base),
					subTitle = stringResource(id = R.string.knowledge_base_sub),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_knowledge_base),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.feedback),
					subTitle = stringResource(id = R.string.feedback_sub),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_bug),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.graphite_progress),
					subTitle = stringResource(id = R.string.graphite_progress_sub),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_kanban),
					trailingIcon = SettingsButtonDefaults.settingsButtonTrailingIcon(icon = R.drawable.ic_fa_open_externally),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.privacy_policy),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_policy),
					trailingIcon = SettingsButtonDefaults.settingsButtonTrailingIcon(icon = R.drawable.ic_fa_open_externally),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.terms_of_service),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_file_txt),
					trailingIcon = SettingsButtonDefaults.settingsButtonTrailingIcon(icon = R.drawable.ic_fa_open_externally),
					onClick = { },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.about),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_info),
					onClick = { },
				)
			}
			item { Spacer(modifier = Modifier.height(128.dp)) }
		}
	}

	if (showAccountBottomSheet) {
		ModalBottomSheet(
			sheetState = accountBottomSheetState,
			onDismissRequest = { showAccountBottomSheet = false },
			containerColor = MaterialTheme.colorScheme.background,
			contentColor = MaterialTheme.colorScheme.onBackground,
		) {
			AccountBottomSheet(
				isSignedIn = firebaseUser != null,
				onClickSignIn = {
					onClickSignIn()
					scope.launch {
						accountBottomSheetState.hide()
						showAccountBottomSheet = false
					}
				},
				onClickSignOut = {
					onClickSignOut()
					scope.launch {
						accountBottomSheetState.hide()
						showAccountBottomSheet = false
					}
				},
				onClickDeleteAccount = {
					onClickDeleteAccount()
					scope.launch {
						accountBottomSheetState.hide()
						showAccountBottomSheet = false
					}
				},
			)
		}
	}
}

@Preview
@Composable
private fun ProfileCard(
	displayName: String? = null,
	email: String? = null,
	photoUrl: Uri? = null,
	isPro: Boolean = false,
) {
	val context = LocalContext.current

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier.fillMaxWidth(),
	) {
		Spacer(modifier = Modifier.height(32.dp))
		AsyncImage(
			model = ImageRequest.Builder(context)
				.data(photoUrl)
				.crossfade(300)
				.error(R.drawable.ic_user_male)
				.build(),
			placeholder = null,
			contentDescription = email,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.requiredSize(96.dp)
				.clip(CircleShape),
		)
		Spacer(modifier = Modifier.height(8.dp))
		Text(
			text = displayName ?: "Anonymous",
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onBackground,
			fontWeight = FontWeight.Bold,
		)
		Text(
			text = email ?: "Email not available",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
		)
		Spacer(modifier = Modifier.height(32.dp))
	}
}

@Preview
@Composable
private fun MadeWithLove() {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = "made with",
			style = MaterialTheme.typography.bodyLarge,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)
		)

		Spacer(modifier = Modifier.width(6.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_heart_c),
			contentDescription = "Love",
			tint = Color.Unspecified,
			modifier = Modifier.requiredSize(24.dp)
		)

		Spacer(modifier = Modifier.width(6.dp))

		Text(
			text = "on",
			style = MaterialTheme.typography.bodyLarge,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)
		)

		Spacer(modifier = Modifier.width(10.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_earth_c),
			contentDescription = "Earth",
			tint = Color.Unspecified,
			modifier = Modifier.requiredSize(20.dp)
		)
	}
}
