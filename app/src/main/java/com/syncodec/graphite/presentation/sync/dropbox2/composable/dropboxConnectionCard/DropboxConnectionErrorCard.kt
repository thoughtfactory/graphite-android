package com.syncodec.graphite.presentation.sync.dropbox2.composable.dropboxConnectionCard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.info.InfoCardType


@Preview
@Composable
fun DropboxConnectionErrorCard(
	modifier: Modifier = Modifier,
) {
	InfoCardType.Error(
		modifier = modifier,
		title = stringResource(R.string.id),
		description = stringResource(R.string.id),
		onClick = {},
	)
}
