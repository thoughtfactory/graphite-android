package com.syncodec.graphite.presentation.common.info

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R


@Preview
@Composable
fun PlainTextWarning(
	modifier : Modifier = Modifier,
	isVisible : Boolean = true,
	onClickButton : () -> Unit = {},
) {
	AnimatedVisibility(
		visible = isVisible,
		enter = expandVertically(tween(300)),
		exit = shrinkVertically(tween(300)),
	){
		InfoCard(
			title = "Unencrypted Data",
			description = "Snapshot files are not encrypted and can be accessed by anyone with authorization.\n" +
					"Encrypted snapshots are coming soon.",
			icon = R.drawable.ic_warning,
			colors = InfoCardDefaults.errorCardColors(),
			buttonText = "Dismiss",
			onClickButton = onClickButton,
			modifier = modifier,
		)
	}
}
