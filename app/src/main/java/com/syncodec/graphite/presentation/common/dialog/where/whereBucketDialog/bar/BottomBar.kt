package com.syncodec.graphite.presentation.common.dialog.where.whereBucketDialog.bar

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.bar.GenericBottomBar
import com.syncodec.graphite.presentation.common.button.VaultButton


@Preview
@Composable
fun BottomBar(
	onClickSelect: () -> Unit = {},
) {
	GenericBottomBar {
		OutlinedButton(
			onClick = onClickSelect,
			shape = MaterialTheme.shapes.medium,
			colors = ButtonDefaults.outlinedButtonColors(
				containerColor = MaterialTheme.colorScheme.background,
				contentColor = MaterialTheme.colorScheme.onBackground
			),
			modifier = Modifier.weight(1f)
		) {
			Text(text = stringResource(id = R.string.select_bucket))
		}
		Spacer(modifier = Modifier.width(12.dp))
		VaultButton()
	}
}
