package com.syncodec.graphite.presentation.bucket.composable.buildingBlock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ANIMATION_DURATION_MILLIS


@Preview
@Composable
fun TodoFloatingActionButton(
	visible: Boolean = true,
	onClick: () -> Unit = {},
) {
	AnimatedVisibility(
		visible = visible,
		enter = scaleIn(tween(ANIMATION_DURATION_MILLIS)),
		exit = scaleOut(tween(ANIMATION_DURATION_MILLIS)),
		label = "addTodoFab_animation"
	) {
		ExtendedFloatingActionButton(
			text = { Text(text = stringResource(id = R.string.add_todo)) },
			icon = {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_plus),
					contentDescription = stringResource(id = R.string.add_todo),
					modifier = Modifier.requiredSize(16.dp)
				)
			},
			onClick = onClick
		)
	}
}

@Preview
@Composable
fun TodoFloatingActionButtonDebug(
	visible: Boolean = true,
	onClick: () -> Unit = {},
	onClickDebug: () -> Unit = {},
) {
	AnimatedVisibility(
		visible = visible,
		enter = scaleIn(tween(ANIMATION_DURATION_MILLIS)),
		exit = scaleOut(tween(ANIMATION_DURATION_MILLIS)),
		label = "addTodoFab_animation"
	) {
		Column {
			FloatingActionButton(onClick = onClickDebug) {
				Icon(
					painter = painterResource(id = R.drawable.ic_fa_bug),
					contentDescription = "Debug",
					modifier = Modifier.requiredSize(16.dp)
				)
			}
			Spacer(modifier = Modifier.height(8.dp))
			ExtendedFloatingActionButton(
				text = { Text(text = stringResource(id = R.string.add_todo)) },
				icon = {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_plus),
						contentDescription = stringResource(id = R.string.add_todo),
						modifier = Modifier.requiredSize(16.dp)
					)
				},
				onClick = onClick
			)
		}
	}
}

