package com.syncodec.graphite.presentation.settings.composable.bottomSheet

import androidx.compose.runtime.Composable
import com.google.firebase.auth.FirebaseUser


enum class SettingsBottomSheetType {
	Profile
}

@Composable
fun SheetLayout(
	bottomSheetType: SettingsBottomSheetType = SettingsBottomSheetType.Profile,
	firebaseUser : FirebaseUser? = null,
	signOut : () -> Unit = {},
	closeSheet: () -> Unit = {},
) {
	when (bottomSheetType) {
		SettingsBottomSheetType.Profile -> ProfileBottomSheet(
			firebaseUser = firebaseUser,
			signOut = signOut,
			closeSheet = closeSheet
		)
	}
}
