package com.syncodec.momento.vaultComponent

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.syncodec.momento.R
import com.syncodec.momento.miscellaneous.DataStore
import compose.icons.TablerIcons
import compose.icons.tablericons.*

private data class VaultButton(
	val imageVector: ImageVector,
	val onClick: () -> Unit
)

enum class EvokeReason {
	UNLOCK_VAULT,
	NEW_PASSCODE,
	CONFIRM_PASSCODE,
	REMOVE_PASSCODE,
	CHANGE_PASSCODE,
	ADD_BIOMETRIC_UNLOCK,
}

private enum class Click {
	_0,
	_1,
	_2,
	_3,
	_4,
	_5,
	_6,
	_7,
	_8,
	_9,
	BACKSPACE,
	CHECK
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
	evokeReason: EvokeReason,
	onSuccess: () -> Unit,
	onFailed: () -> Unit
) {
	val context = LocalContext.current
	val dataStore = DataStore(context = context)
	val passcode = dataStore.getPasscode.collectAsState(initial = null)

	var code by remember { mutableStateOf("") }
	var newPasscode by remember { mutableStateOf("") }

	var isPasswordIncorrect by remember { mutableStateOf(false) }
	var showPasswordIncorrectMessage by remember { mutableStateOf(false) }

	var reason by remember { mutableStateOf(evokeReason) }

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		PasscodeImage()

		Column(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			VaultMessageHeader(evokeReason = reason)
			Spacer(modifier = Modifier.height(24.dp))

			PasscodeProgressIndicator(code = code)
			Spacer(modifier = Modifier.height(12.dp))

			PasscodeIncorrectMessage(showPasswordIncorrectMessage = showPasswordIncorrectMessage)
		}

		PasscodeKeyboard {
			when (it) {
				Click._0 -> code += "0"
				Click._1 -> code += "1"
				Click._2 -> code += "2"
				Click._3 -> code += "3"
				Click._4 -> code += "4"
				Click._5 -> code += "5"
				Click._6 -> code += "6"
				Click._7 -> code += "7"
				Click._8 -> code += "8"
				Click._9 -> code += "9"
				Click.BACKSPACE -> code = code.dropLast(1)
				Click.CHECK -> {
					isPasswordIncorrect = code != passcode.value

					when (reason) {
						EvokeReason.UNLOCK_VAULT -> {
							if (!isPasswordIncorrect) {
								Toast.makeText(context, "Vault unlocked", Toast.LENGTH_SHORT).show()
								onSuccess()
							} else showPasswordIncorrectMessage = true
						}
						EvokeReason.NEW_PASSCODE -> {
							newPasscode = code
							reason = EvokeReason.CONFIRM_PASSCODE
						}
						EvokeReason.CONFIRM_PASSCODE -> {
							if (newPasscode == code) {
								dataStore.putPasscode(code)
								Toast.makeText(context, "Passcode updated", Toast.LENGTH_SHORT).show()
								onSuccess()
							} else {
								Toast.makeText(context, "Passcode don't match. Please try again.", Toast.LENGTH_SHORT).show()
								reason = EvokeReason.NEW_PASSCODE
							}
						}
						EvokeReason.REMOVE_PASSCODE -> {
							if (!isPasswordIncorrect) {
								dataStore.putPasscode("")
								Toast.makeText(context, "Passcode removed. But locked items will be not be visible in home screen.", Toast.LENGTH_LONG).show()
								onSuccess()
							} else {
								Toast.makeText(context, "Wrong passcode. Please try again.", Toast.LENGTH_SHORT).show()
								showPasswordIncorrectMessage = true
							}
						}
						EvokeReason.CHANGE_PASSCODE -> {
							if (!isPasswordIncorrect) {
								reason = EvokeReason.NEW_PASSCODE
							} else {
								Toast.makeText(context, "Wrong passcode. Please try again.", Toast.LENGTH_SHORT).show()
								showPasswordIncorrectMessage = true
							}
						}
						EvokeReason.ADD_BIOMETRIC_UNLOCK -> if (!isPasswordIncorrect) {
							Toast.makeText(context, "Vault unlocked", Toast.LENGTH_SHORT).show()
							onSuccess()
						}
					}
					code = ""
				}
			}
			if (code.length > 4) code = code.substring(0, 4)
		}

		Spacer(modifier = Modifier.height(24.dp))
	}

}

@Composable
private fun PasscodeImage() {
	Box(
		modifier = Modifier
			.fillMaxWidth(0.71f)
			.height(256.dp)
			.padding(32.dp),
		contentAlignment = Alignment.Center
	) {
		Image(
			painter = rememberImagePainter(R.drawable.il_vault),
			contentDescription = null,
			contentScale = ContentScale.Fit,
			modifier = Modifier.fillMaxSize()
		)
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun VaultMessageHeader(evokeReason: EvokeReason) {
	AnimatedContent(
		targetState = evokeReason
	) {
		when (it) {
			EvokeReason.UNLOCK_VAULT -> VaultMessage(message = "Enter passcode")
			EvokeReason.NEW_PASSCODE -> VaultMessage(message = "Enter new passcode")
			EvokeReason.CONFIRM_PASSCODE -> VaultMessage(message = "Confirm passcode")
			EvokeReason.REMOVE_PASSCODE -> VaultMessage(message = "Enter current passcode")
			EvokeReason.CHANGE_PASSCODE -> VaultMessage(message = "Enter current passcode")
			EvokeReason.ADD_BIOMETRIC_UNLOCK -> VaultMessage(message = "Add biometric unlock")
		}
	}
}

@Composable
private fun VaultMessage(message: String) {
	Text(
		text = message,
		style = MaterialTheme.typography.bodyMedium,
		color = MaterialTheme.colorScheme.onBackground,
		fontWeight = FontWeight.Bold
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasscodeProgressIndicator(code: String) {
	Row(
		modifier = Modifier
	) {
		for (i in 0 until 4) {
			val tint by animateColorAsState(
				targetValue = if (code.length > i) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
				animationSpec = tween(
					durationMillis = 400
				)
			)
			Card(
				modifier = Modifier
					.requiredSize(40.dp)
					.padding(8.dp),
				shape = CircleShape,
				border = BorderStroke(3.dp, MaterialTheme.colorScheme.primaryContainer),
				containerColor = tint
			) {

			}
		}
	}
}

@Composable
private fun PasscodeIncorrectMessage(showPasswordIncorrectMessage: Boolean) {
	AnimatedVisibility(
		visible = showPasswordIncorrectMessage,
		enter = fadeIn(),
		exit = fadeOut()
	) {
		Text(
			text = "Passcode incorrect. Please try again.",
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground,
			maxLines = 2,
			textAlign = TextAlign.Center,
			modifier = Modifier
				.fillMaxWidth(0.88f)
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PasscodeKeyboard(
	onClick: (Click) -> Unit
) {
	val vaultButtonList: List<VaultButton> = listOf(
		VaultButton(TablerIcons.Number1) { onClick(Click._1) },
		VaultButton(TablerIcons.Number2) { onClick(Click._2) },
		VaultButton(TablerIcons.Number3) { onClick(Click._3) },
		VaultButton(TablerIcons.Number4) { onClick(Click._4) },
		VaultButton(TablerIcons.Number5) { onClick(Click._5) },
		VaultButton(TablerIcons.Number6) { onClick(Click._6) },
		VaultButton(TablerIcons.Number7) { onClick(Click._7) },
		VaultButton(TablerIcons.Number8) { onClick(Click._8) },
		VaultButton(TablerIcons.Number9) { onClick(Click._9) },
		VaultButton(TablerIcons.Check) { onClick(Click.CHECK) },
		VaultButton(TablerIcons.Number0) { onClick(Click._0) },
		VaultButton(TablerIcons.Backspace) { onClick(Click.BACKSPACE) },
	)

	LazyVerticalGrid(
		columns = GridCells.Fixed(3),
		modifier = Modifier
			.fillMaxWidth(),
	) {
		vaultButtonList.forEach { vaultButton ->
			item {
				IconButton(
					onClick = {
						vaultButton.onClick()
					},
					modifier = Modifier
						.fillMaxSize()
						.aspectRatio(1.5f)
				) {
					Icon(
						imageVector = vaultButton.imageVector,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier
							.requiredSize(24.dp)
					)
				}
			}
		}
	}
}
