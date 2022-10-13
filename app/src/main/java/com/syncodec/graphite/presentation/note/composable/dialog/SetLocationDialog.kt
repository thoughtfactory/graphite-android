package com.syncodec.graphite.presentation.note.composable.dialog

import android.location.Address
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.LatLng
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.utils.locationAddressFilter
import com.syncodec.graphite.utils.roundTo


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SetLocationDialog(
	showDialog: Boolean,
	latLng: LatLng?,
	address: String?,
	reverseGeocode: (LatLng, (Address?) -> Unit) -> Unit,
	onDismiss: () -> Unit,
	onConfirm: (LatLng, String?) -> Unit,
) {
	var _latLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
	var _address by remember { mutableStateOf<String?>(null) }

	val cameraPositionState = rememberCameraPositionState()

	LaunchedEffect(key1 = latLng) {
		_latLng = latLng ?: LatLng(0.0, 0.0)
	}

	LaunchedEffect(key1 = address) {
		_address = address ?: ""
	}

	LaunchedEffect(key1 = cameraPositionState.isMoving) {
		if (!cameraPositionState.isMoving) {
			_latLng = LatLng(cameraPositionState.position.target.latitude, cameraPositionState.position.target.longitude)
			reverseGeocode(_latLng) { address ->
				_address = locationAddressFilter(address = address)
			}
		}
	}

	AnimatedVisibility(
		visible = showDialog,
		enter = fadeIn(tween(300)),
		exit = fadeOut(tween(300)),
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
		) {
			GoogleMap(
				modifier = Modifier.fillMaxSize(),
				cameraPositionState = cameraPositionState,
			)

			Column(
				modifier = Modifier.fillMaxSize()
			) {
				Spacer(modifier = Modifier.height(10.dp))
				Row(
					modifier = Modifier.fillMaxWidth()
				) {
					Spacer(modifier = Modifier.width(4.dp))
					MenuButton(
						icon = R.drawable.ic_close,
						onClick = onDismiss
					)
				}

				Spacer(modifier = Modifier.weight(1f))

				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.Bottom
				) {
					Spacer(modifier = Modifier.width(16.dp))

					Column(
						modifier = Modifier
							.weight(1f)
							.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
							.padding(16.dp)
					) {
						Text(
							text = _address ?: "Address unavailable",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onPrimary,
						)
						Spacer(modifier = Modifier.height(4.dp))
						AnimatedContent(targetState = _latLng) {
							Text(
								text = "${it.latitude?.roundTo(6)}, ${it.longitude?.roundTo(6)}",
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onPrimary,
								fontStyle = FontStyle.Italic
							)
						}
					}

					Spacer(modifier = Modifier.width(12.dp))

					FloatingActionButton(
						onClick = { onConfirm(_latLng, _address) },
						containerColor = MaterialTheme.colorScheme.primary,
						contentColor = MaterialTheme.colorScheme.onPrimary,
					) {
						Icon(
							painter = painterResource(id = R.drawable.ic_check),
							contentDescription = "Set Location",
						)
					}

					Spacer(modifier = Modifier.width(16.dp))
				}

				Spacer(modifier = Modifier.height(12.dp))
			}
		}
	}
}
