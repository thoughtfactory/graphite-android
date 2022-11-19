package com.syncodec.graphite.presentation.bucket.composable.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnShare
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalVaultIsOpened
import com.syncodec.graphite.utils.tone


@Preview
@Composable
fun BottomBar(
	modifier : Modifier = Modifier,
) {
	val containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
	val contentColor = MaterialTheme.colorScheme.onSurface.tone(isSystemInDarkTheme(), 1)

	val openSheet = LocalCompositionOpenBottomSheet.current

	val isSelected = LocalCompositionIsSelected.current

	val isVaultOpened = LocalVaultIsOpened.current
	val onAuthenticatorAction = LocalAuthenticatorAction.current

	val onShare = LocalCompositionOnShare.current

	AnimatedVisibility(
		visible = ! isSelected,
		enter = slideInVertically(animationSpec = tween(300), initialOffsetY = { it }),
		exit = slideOutVertically(animationSpec = tween(300), targetOffsetY = { it }),
		modifier = modifier
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.height(80.dp)
				.background(containerColor)
		) {
			Spacer(modifier = Modifier.width(16.dp))

//			MenuButton(
//				icon = R.drawable.ic_share,
//				contentDescription = "Share bucket",
//				tint = contentColor
//			) { onShare() }

			Spacer(modifier = Modifier.weight(1f))

			Spacer(modifier = Modifier.width(16.dp))

			MenuButton(
				icon = R.drawable.ic_vault,
				contentDescription = "Vault",
				tint = if (isVaultOpened) MaterialTheme.colorScheme.onBackground else contentColor,
				containerColor = if (isVaultOpened) MaterialTheme.colorScheme.background else Color.Companion.Transparent
			) { onAuthenticatorAction(Authenticator.AUTHENTICATE) }

			MenuButton(
				icon = R.drawable.ic_menu,
				contentDescription = "Menu",
				tint = contentColor
			) {
				openSheet(BucketBottomSheetType.MENU)
			}

			Spacer(modifier = Modifier.width(16.dp))
		}
	}
}
