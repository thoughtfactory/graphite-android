package com.syncodec.graphite.presentation.sync.dropbox.composable.buildingBlock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.di.cloud.dropbox.DBox

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
					email = testConnectionResponse.email,
					name = testConnectionResponse.name,
					profilePhotoUrl = testConnectionResponse.profilePictureUrl,
					spaceTotal = testConnectionResponse.spaceTotal,
					spaceUsed = testConnectionResponse.spaceUsed,
					modifier = Modifier.padding(12.dp, 4.dp),
				)
			}

			else -> null
		}
	}
}
