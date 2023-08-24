package com.syncodec.graphite.presentation.ui.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.ui.authentication.buildingBlock.PasscodeNumPad


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemovePasscode(
	onAuthenticate : (String) -> Unit,
	onClose : () -> Unit
) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		TopAppBar(
			modifier = Modifier.fillMaxWidth(),
			navigationIcon = {
				GenericButton(
					icon = R.drawable.ic_close,
					onClick = onClose
				)
			},
			title = {},
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.background,
				navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
				titleContentColor = MaterialTheme.colorScheme.onBackground,
			)
		)

		Image(
			painter = painterResource(id = R.drawable.il_open_vault),
			contentDescription = "Remove Passcode",
			modifier = Modifier.weight(1f)
		)

		Text(
			text = "Remove Vault Passcode",
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(24.dp)
		)

		Spacer(modifier = Modifier.height(24.dp))

		Text(
			text = "Enter passcode",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onBackground,
		)

		Spacer(modifier = Modifier.height(16.dp))

		PasscodeNumPad(onEnter = onAuthenticate)
	}
}
