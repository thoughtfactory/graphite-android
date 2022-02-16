package com.syncodec.momento.vaultComponent

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
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
import compose.icons.TablerIcons
import compose.icons.tablericons.*


private data class VaultButton(
	val imageVector: ImageVector,
	val onClick: () -> Unit
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VaultOpenerScreen(
	onSuccess: () -> Unit,
	onFailed: () -> Unit
) {
	val context = LocalContext.current
	var passcode by remember { mutableStateOf("") }

	val code = "1371"
	var isPasswordIncorrect by remember { mutableStateOf(false) }

	val vaultButtonList: List<VaultButton> = listOf(
		VaultButton(TablerIcons.Number1) { passcode += "1" },
		VaultButton(TablerIcons.Number2) { passcode += "2" },
		VaultButton(TablerIcons.Number3) { passcode += "3" },
		VaultButton(TablerIcons.Number4) { passcode += "4" },
		VaultButton(TablerIcons.Number5) { passcode += "5" },
		VaultButton(TablerIcons.Number6) { passcode += "6" },
		VaultButton(TablerIcons.Number7) { passcode += "7" },
		VaultButton(TablerIcons.Number8) { passcode += "8" },
		VaultButton(TablerIcons.Number9) { passcode += "9" },
		VaultButton(TablerIcons.Check) {
			isPasswordIncorrect = code != passcode
			if (isPasswordIncorrect) {
				passcode = ""
			} else {
				Toast.makeText(context, "Vault unlocked", Toast.LENGTH_SHORT).show()
				onSuccess()
			}
		},
		VaultButton(TablerIcons.Number0) { passcode += "0" },
		VaultButton(TablerIcons.Backspace) { passcode = passcode.dropLast(1) },
	)

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {

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
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f),
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center
			) {
				Text(
					text = "ENTER PASSCODE",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onBackground
				)

				Spacer(modifier = Modifier.height(24.dp))

				Row(
					modifier = Modifier
				) {
					for (i in 0 until 4) {
						val tint by animateColorAsState(
							targetValue = if (passcode.length > i) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
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

				AnimatedVisibility(
					visible = isPasswordIncorrect,
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

			LazyVerticalGrid(
				cells = GridCells.Fixed(3),
				modifier = Modifier
					.fillMaxWidth()
			) {
				vaultButtonList.forEach { vaultButton ->
					item {
						IconButton(
							onClick = {
								vaultButton.onClick()
								if (passcode.length > 4) {
									passcode = passcode.substring(0, 4)
								}
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
			Spacer(modifier = Modifier.height(24.dp))
		}
	}

}
