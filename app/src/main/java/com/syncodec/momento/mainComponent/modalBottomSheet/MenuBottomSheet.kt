package com.syncodec.momento.mainComponent.modalBottomSheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.syncodec.momento.MainActivity
import com.syncodec.momento.R
import com.syncodec.momento.custom.bottomSheet.BottomSheetHeader
import com.syncodec.momento.custom.bottomSheet.BottomSheetStrip
import com.syncodec.momento.custom.button.MenuBottomSheetButton
import com.syncodec.momento.custom.button.MenuBottomSheetButtonData


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun MenuBottomSheet(
	isLoggedIn: Boolean,
	email: String?,
	onAction: (MainActivity.Action) -> Unit
) {
	val buttonDataList: List<MenuBottomSheetButtonData?> = listOf(
		MenuBottomSheetButtonData(title = "Attachment", icon = R.drawable.ic_attachment) {
			onAction(MainActivity.Action.ATTACHMENT)
		},
		MenuBottomSheetButtonData(title = "Tags", icon = R.drawable.ic_hashtag) {
			onAction(MainActivity.Action.TAGS)
		},
		MenuBottomSheetButtonData(title = "Vault", icon = R.drawable.ic_vault) {
			onAction(MainActivity.Action.VAULT)
		},
		MenuBottomSheetButtonData(title = "Settings", icon = R.drawable.ic_settings) {
			onAction(MainActivity.Action.SETTINGS)
		},

		MenuBottomSheetButtonData(title = "Favourite", icon = R.drawable.ic_favourite) {
			onAction(MainActivity.Action.TOGGLE_FAVOURITE)
		},
		MenuBottomSheetButtonData(title = "Archived", icon = R.drawable.ic_archive) {
			onAction(MainActivity.Action.TOGGLE_ARCHIVED)
		},
	)

	Surface(
		shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp),
		color= MaterialTheme.colorScheme.surface,
		modifier = Modifier.heightIn(360.dp),
	) {
		Column(
			modifier = Modifier,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			BottomSheetStrip()

			BottomSheetHeader(
				title = "Menu",
				icon = R.drawable.ic_menu
			)

			LazyVerticalGrid(
				columns = GridCells.Fixed(4),
				modifier = Modifier.padding(24.dp, 0.dp),
			) { buttonDataList.forEach { item { MenuBottomSheetButton(it) } } }

			Spacer(modifier = Modifier.height(16.dp))

			UserCard(
				isLoggedIn = isLoggedIn,
				email = email,
			) { onAction(it) }

			Spacer(modifier = Modifier.height(24.dp))
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserCard(
	isLoggedIn: Boolean,
	email: String?,
	onAction: (MainActivity.Action) -> Unit
) {
	Surface(
		shape = RoundedCornerShape(24.dp),
		color = MaterialTheme.colorScheme.primary,
		border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp),
	) {
		if (isLoggedIn) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 12.dp),
				contentAlignment = Alignment.CenterStart
			) {
				Text(
					text = email ?: "",
					style = MaterialTheme.typography.titleSmall,
					modifier = Modifier,
				)
			}
		} else {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(24.dp, 12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = "Login to unlock formatting options",
					style = MaterialTheme.typography.titleSmall,
					modifier = Modifier.weight(1f),
				)

				Spacer(modifier = Modifier.width(16.dp))

				Button(
					onClick = { onAction(MainActivity.Action.OPEN_LOGIN_SCREEN) },
					modifier = Modifier,
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.onPrimary,
						contentColor = MaterialTheme.colorScheme.primary
					)
				) {
					Text(
						text = "Login",
						modifier = Modifier,
						style = MaterialTheme.typography.bodyMedium,
					)
				}
			}
		}
	}
}
