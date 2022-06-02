package com.syncodec.graphite.vaultComponent

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.syncodec.graphite.R
import com.syncodec.graphite.miscellaneous.DataStore

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

@Composable
fun VaultScreen(
	evokeReason: EvokeReason,
	onSuccess: () -> Unit,
	onFailed: () -> Unit
) {
	val context = LocalContext.current
	val dataStore = remember { DataStore(context = context) }
	val passcode by dataStore.getPasscode.collectAsState(initial = null)

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
					isPasswordIncorrect = code != passcode

					when (reason) {
						EvokeReason.UNLOCK_VAULT -> {
							code = ""
							if (!isPasswordIncorrect) {
								Toast.makeText(context, "Vault unlocked", Toast.LENGTH_SHORT).show()
								onSuccess()
							} else showPasswordIncorrectMessage = true
						}
						EvokeReason.NEW_PASSCODE -> {
							if (code.length == 4) {
								newPasscode = code
								reason = EvokeReason.CONFIRM_PASSCODE
								code = ""
							}
						}
						EvokeReason.CONFIRM_PASSCODE -> {
							if (newPasscode == code) {
								dataStore.putPasscode(code)
								code = ""
								Toast.makeText(context, "Passcode updated", Toast.LENGTH_SHORT)
									.show()
								onSuccess()
							} else {
								code = ""
								Toast.makeText(
									context,
									"Passcode don't match. Please try again.",
									Toast.LENGTH_SHORT
								).show()
								reason = EvokeReason.NEW_PASSCODE
							}
						}
						EvokeReason.REMOVE_PASSCODE -> {
							if (!isPasswordIncorrect) {
								dataStore.putPasscode("")
								code = ""
								Toast.makeText(
									context,
									"Passcode removed. But locked items will be not be visible in home screen.",
									Toast.LENGTH_LONG
								).show()
								onSuccess()
							} else {
								code = ""
								Toast.makeText(
									context,
									"Wrong passcode. Please try again.",
									Toast.LENGTH_SHORT
								).show()
								showPasswordIncorrectMessage = true
							}
						}
						EvokeReason.CHANGE_PASSCODE -> {
							if (!isPasswordIncorrect) {
								code = ""
								reason = EvokeReason.NEW_PASSCODE
							} else {
								code = ""
								Toast.makeText(
									context,
									"Wrong passcode. Please try again.",
									Toast.LENGTH_SHORT
								).show()
								showPasswordIncorrectMessage = true
							}
						}
						EvokeReason.ADD_BIOMETRIC_UNLOCK -> if (!isPasswordIncorrect) {
							code = ""
							Toast.makeText(context, "Vault unlocked", Toast.LENGTH_SHORT).show()
							onSuccess()
						}
					}
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
		style = MaterialTheme.typography.bodyLarge,
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
			modifier = Modifier.fillMaxWidth(0.88f)
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PasscodeKeyboard(
	onClick: (Click) -> Unit
) {
	LazyVerticalGrid(
		columns = GridCells.Fixed(3),
		modifier = Modifier
			.fillMaxWidth(),
	) {
		item {
			VaultButton(
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(1.5f),
				text = "1"
			) { onClick(Click._1) }
		}
		item {
			VaultButton(
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(1.5f),
				text = "2"
			) { onClick(Click._2) }
		}
		item {
			VaultButton(
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(1.5f),
				text = "3"
			) { onClick(Click._3) }
		}
		item {
			VaultButton(
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(1.5f),
				text = "4"
			) { onClick(Click._4) }
		}
		item {
			VaultButton(
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(1.5f),
				text = "5"
			) { onClick(Click._5) }
		}
		item {
			VaultButton(
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(1.5f),
				text = "6"
			) { onClick(Click._6) }
		}
		item {
			VaultButton(
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(1.5f),
				text = "7"
			) { onClick(Click._7) }
		}
		item {
			VaultButton(
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(1.5f),
				text = "8"
			) { onClick(Click._8) }
		}
		item {
			VaultButton(
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(1.5f),
				text = "9"
			) { onClick(Click._9) }
		}
		item {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(1.5f)
					.clickable { onClick(Click.BACKSPACE) },
				contentAlignment = Alignment.Center
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_backspace),
					contentDescription = "Backspace",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.size(28.dp)
				)
			}
		}
		item {
			VaultButton(
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(1.5f), text = "0"
			) { onClick(Click._0) }
		}
		item {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.aspectRatio(1.5f)
					.clickable { onClick(Click.CHECK) },
				contentAlignment = Alignment.Center
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_done),
					contentDescription = "Done",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.size(28.dp)
				)
			}
		}
	}
}

@Composable
private fun VaultButton(
	modifier: Modifier,
	text: String,
	onClick: () -> Unit
) {
	Box(
		modifier = modifier.clickable { onClick() },
		contentAlignment = Alignment.Center
	) {
		Text(
			text = text,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontSize = 20.sp,
			color = MaterialTheme.colorScheme.onBackground
		)
	}
}
