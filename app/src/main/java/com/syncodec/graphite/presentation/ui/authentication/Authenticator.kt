package com.syncodec.graphite.presentation.ui.authentication

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.ui.authentication.buildingBlock.PasscodeNumPad


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatorScreen(
	onAuthentication : (String) -> Unit,
	onClose : () -> Unit
) {

	val context = LocalContext.current

	var noTry by remember { mutableStateOf(0) }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		TopAppBar(
			modifier = Modifier.fillMaxWidth(),
			navigationIcon = {
				MenuButton(
					icon = R.drawable.ic_close,
					tint = MaterialTheme.colorScheme.onBackground,
					onClick = onClose
				)
			},
			title = {},
			colors = TopAppBarDefaults.smallTopAppBarColors(
				containerColor = MaterialTheme.colorScheme.background,
				navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
				titleContentColor = MaterialTheme.colorScheme.onBackground,
			)
		)

		Spacer(modifier = Modifier.weight(1f))

		PasscodeNumPad (onEnter = onAuthentication)

		Spacer(modifier = Modifier.height(24.dp))
	}
}
