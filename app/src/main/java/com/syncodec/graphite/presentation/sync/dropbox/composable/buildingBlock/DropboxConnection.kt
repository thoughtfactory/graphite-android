package com.syncodec.graphite.presentation.sync.dropbox.composable.buildingBlock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dropbox.core.v2.users.FullAccount
import com.dropbox.core.v2.users.SpaceUsage
import com.syncodec.graphite.di.sync.dropbox.DBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Preview
@Composable
fun DropboxConnection(
	testConnectionResponse : DBox.Companion.TestConnectionResponse? = null,
) {
	AnimatedVisibility(
		visible = testConnectionResponse is DBox.Companion.TestConnectionResponse.Success,
		enter = expandVertically(tween(300)),
		exit = shrinkVertically(tween(300)),
	) {
		when (testConnectionResponse) {
			is DBox.Companion.TestConnectionResponse.Success -> {
				DropboxProfile(
					email = testConnectionResponse.fullAccount.email,
					name = testConnectionResponse.fullAccount.name.displayName,
					profilePhotoUrl = testConnectionResponse.fullAccount.profilePhotoUrl,
					spaceTotal = testConnectionResponse.spaceUsage.allocation?.individualValue?.allocated,
					spaceUsed = testConnectionResponse.spaceUsage.used,
					modifier = Modifier.padding(12.dp, 4.dp),
				)
			}

			else -> null
		}
	}
}
