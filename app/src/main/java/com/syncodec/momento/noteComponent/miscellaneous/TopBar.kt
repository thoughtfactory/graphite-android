package com.syncodec.momento.noteComponent.miscellaneous

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.syncodec.momento.R
import com.syncodec.momento.noteComponent.NoteActivity
import compose.icons.TablerIcons
import compose.icons.tablericons.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopBar(
	isViewer: Boolean,
	isSaving: Boolean,
	onClick: (NoteActivity.Click) -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(64.dp)
			.background(MaterialTheme.colorScheme.secondaryContainer)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.padding(0.dp, 8.dp)
		) {
			Spacer(modifier = Modifier.width(8.dp))
			IconButton(
				onClick = { onClick(NoteActivity.Click.FINISH) },
			) {
				Crossfade(targetState = isViewer) {
					if (it) {
						Icon(
							imageVector = TablerIcons.ArrowLeft,
							contentDescription = "Back",
							tint = MaterialTheme.colorScheme.onSecondaryContainer,
						)
					} else {
						Icon(
							imageVector = TablerIcons.X,
							contentDescription = "Discard",
							tint = MaterialTheme.colorScheme.onSecondaryContainer,
						)
					}
				}
			}

			Spacer(modifier = Modifier.weight(1f))

			Crossfade(targetState = isViewer) {
				if (it) {
					IconButton(
						onClick = { onClick(NoteActivity.Click.TOP_BAR_QUATERNARY) },
					) {
						Icon(
							imageVector = TablerIcons.Pencil,
							contentDescription = "Edit",
							tint = MaterialTheme.colorScheme.onSecondaryContainer,
						)
					}
				}
			}

			AnimatedVisibility(visible = !isViewer) {
				Button(
					onClick = { onClick(NoteActivity.Click.SAVE) },
					colors = ButtonDefaults.textButtonColors(
						containerColor = MaterialTheme.colorScheme.primaryContainer,
						contentColor = MaterialTheme.colorScheme.onPrimaryContainer
					),
				) {
					Crossfade(targetState = isSaving) {
						if (it) {
							val lottieComposition by rememberLottieComposition(
								LottieCompositionSpec.RawRes(R.raw.lottie_loading)
							)

							LottieAnimation(
								composition = lottieComposition,
								iterations = LottieConstants.IterateForever,
								modifier = Modifier.requiredSize(24.dp)
							)
						} else {
							Text(
								text = "Save",
								style = MaterialTheme.typography.bodyMedium,
								fontWeight = FontWeight.Bold
							)
						}
					}
				}
			}
			Spacer(modifier = Modifier.width(12.dp))
		}
	}
}
