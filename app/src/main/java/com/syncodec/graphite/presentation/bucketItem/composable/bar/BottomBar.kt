package com.syncodec.graphite.presentation.bucketItem.composable.bar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.presentation.common.bar.GenericBottomBar
import com.syncodec.graphite.presentation.common.button.MetadataButton
import com.syncodec.graphite.presentation.common.button.ShareButton
import com.syncodec.graphite.presentation.common.button.VaultButton


@Preview
@Composable
fun BottomBar(
	onClickMetadata : () -> Unit = {},
	onClickShare : () -> Unit = {},
) {
	GenericBottomBar {
		MetadataButton(onClick = onClickMetadata)

		ShareButton(onClick = onClickShare)

		Spacer(modifier = Modifier.weight(1f))

		Spacer(modifier = Modifier.width(16.dp))

		VaultButton()
	}
}
