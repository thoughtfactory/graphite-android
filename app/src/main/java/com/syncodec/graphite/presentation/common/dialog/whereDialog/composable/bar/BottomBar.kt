package com.syncodec.graphite.presentation.common.dialog.whereDialog.composable.bar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


@Preview
@Composable
fun BottomBar(
	onClickSelect : () -> Unit = {},
	onClickEverywhere : () -> Unit = {},
) {
////	val isAuthenticated = LocalIsRepoUnlocked.current
////	val onAuthenticationAction = LocalAuthenticatorAction.current
//
//	BottomAppBar(
//		modifier = Modifier.fillMaxWidth(),
//		tonalElevation = 8.dp,
//	) {
//
//		Spacer(modifier = Modifier.width(12.dp))
//
//		Box(
//			contentAlignment = Alignment.CenterStart,
//			modifier = Modifier
//				.weight(1f)
//				.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
//				.clip(MaterialTheme.shapes.medium)
//				.clickable(onClick = onClickSelect)
//				.height((IconButtonSize * 2) - 2.dp),
//		) {
//			Text(
//				text = "Select",
//				style = MaterialTheme.typography.bodyMedium,
//				color = MaterialTheme.colorScheme.onBackground,
//				modifier = Modifier.padding(start = 12.dp),
//			)
//		}
//
//		Spacer(modifier = Modifier.width(8.dp))
//
//		Box(
//			contentAlignment = Alignment.CenterStart,
//			modifier = Modifier
//				.weight(1f)
//				.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
//				.clip(MaterialTheme.shapes.medium)
//				.clickable(onClick = onClickEverywhere)
//				.height((IconButtonSize * 2) - 2.dp),
//		) {
//			Text(
//				text = "Everywhere",
//				style = MaterialTheme.typography.bodyMedium,
//				color = MaterialTheme.colorScheme.onBackground,
//				modifier = Modifier.padding(start = 12.dp),
//			)
//		}
//
//		Spacer(modifier = Modifier.width(12.dp))
//
//		GenericButton(
//			icon = R.drawable.ic_vault,
//			tooltip = "Vault",
//			checked = isAuthenticated,
//			colors = GenericButtonDefaults.genericButtonColorsOnSurface(),
//		) { onAuthenticationAction(AuthenticationState.Authenticate) }
//
//		Spacer(modifier = Modifier.width(12.dp))
//	}
}
