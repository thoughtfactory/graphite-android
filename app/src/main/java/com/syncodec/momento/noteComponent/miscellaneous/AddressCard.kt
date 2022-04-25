package com.syncodec.momento.noteComponent.miscellaneous

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.maps.model.LatLng
import com.syncodec.momento.R
import com.syncodec.momento.miscellaneous.logger
import com.syncodec.momento.noteComponent.NoteActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddressCard(
	addressState: NoteActivity.AddressState,
	showAddressCard: Boolean,
	address: String?,
	latLng: LatLng?,
	onAction: (NoteActivity.Action, Any?) -> Unit
) {
	val scope = rememberCoroutineScope()

	val showCardPreference = true

	val animateAlpha by animateFloatAsState(
		targetValue = if (showAddressCard && showCardPreference) 1f else 0f,
		animationSpec = tween(durationMillis = 600),
		finishedListener = {
			scope.launch {
				delay(6400)
				onAction(NoteActivity.Action.HIDE_ADDRESS, null)
			}
		}
	)

	Box(
		contentAlignment = Alignment.BottomCenter,
		modifier = Modifier
			.clickable { onAction(NoteActivity.Action.HIDE_ADDRESS, null) }
			.graphicsLayer { this.alpha = animateAlpha },
	) {
		when (addressState) {
			NoteActivity.AddressState.OFF -> {
			}
			NoteActivity.AddressState.INIT -> {
			}
			NoteActivity.AddressState.NO_PERMISSION -> {
			}
			NoteActivity.AddressState.REQUEST_PERMISSION -> {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.surface)
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier.padding(0.dp, 12.dp),
					) {
						Spacer(modifier = Modifier.width(16.dp))
						Text(
							text = "Want to geotag your entry?\nKeep your memory connected with location",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurface,
							fontWeight = FontWeight.Bold,
							modifier = Modifier.weight(1f)
						)

						Spacer(modifier = Modifier.width(16.dp))

						Button(
							onClick = {
								onAction(NoteActivity.Action.REQUEST_LOCATION_PERMISSION, null)
							},
							colors = ButtonDefaults.outlinedButtonColors(
								containerColor = MaterialTheme.colorScheme.primaryContainer
							),
							modifier = Modifier.wrapContentWidth(),
						) {
							Row(
								modifier = Modifier,
								verticalAlignment = Alignment.CenterVertically
							) {
								Icon(
									painter = painterResource(id = R.drawable.ic_map_marker),
									contentDescription = null,
									tint = MaterialTheme.colorScheme.onPrimaryContainer,
									modifier = Modifier.requiredSize(20.dp)
								)
								Spacer(modifier = Modifier.width(6.dp))
								Text(
									text = "Yep!!",
									style = MaterialTheme.typography.bodyMedium,
									fontWeight = FontWeight.Bold,
									color = MaterialTheme.colorScheme.onPrimaryContainer
								)
							}
						}
						Spacer(modifier = Modifier.width(16.dp))
					}
				}
			}
			NoteActivity.AddressState.SHOW_RATIONALE -> {
			}
			NoteActivity.AddressState.PERMISSION_REQUESTED -> {
			}
			NoteActivity.AddressState.LOCATION -> {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.surface)
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier.padding(16.dp, 12.dp),
					) {
						Text(
							text = "Address unavailable\nLat : ${latLng?.latitude}, Lng : ${latLng?.longitude}",
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurface,
							fontWeight = FontWeight.Bold,
							modifier = Modifier
						)
					}
				}
			}
			NoteActivity.AddressState.SUCCESS -> {
				if (address != null) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.background(MaterialTheme.colorScheme.surface)
					) {
						Row(
							verticalAlignment = Alignment.CenterVertically,
							horizontalArrangement = Arrangement.Center,
							modifier = Modifier
								.padding(16.dp, 12.dp),
						) {
							Text(
								text = address,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurface,
								fontWeight = FontWeight.Bold,
								modifier = Modifier
							)
						}
					}
				}
			}
			NoteActivity.AddressState.ERROR -> {
			}
			NoteActivity.AddressState.REMOVED -> {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.background)
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.padding(16.dp, 12.dp),
					) {
						Text(
							text = "Geo tag removed...",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground,
							fontWeight = FontWeight.Bold,
							modifier = Modifier
								.weight(1f)
						)
					}
				}
			}
		}
		when (addressState) {
			NoteActivity.AddressState.OFF -> {}
			NoteActivity.AddressState.INIT -> {}
			NoteActivity.AddressState.NO_PERMISSION -> {}
			NoteActivity.AddressState.REQUEST_PERMISSION -> {
				LaunchedEffect(key1 = Unit) {
					scope.launch {
						delay(1200)
						onAction(NoteActivity.Action.SHOW_ADDRESS, null)
					}
				}
			}
			NoteActivity.AddressState.SHOW_RATIONALE -> {}
			NoteActivity.AddressState.PERMISSION_REQUESTED -> {}
			NoteActivity.AddressState.LOCATION -> {
				LaunchedEffect(key1 = Unit) {
					scope.launch {
						delay(1200)
						onAction(NoteActivity.Action.SHOW_ADDRESS, null)
					}
				}
			}
			NoteActivity.AddressState.SUCCESS -> {
				LaunchedEffect(key1 = Unit) {
					scope.launch {
						delay(1200)
						onAction(NoteActivity.Action.SHOW_ADDRESS, null)
					}
				}
			}
			NoteActivity.AddressState.ERROR -> {}
			NoteActivity.AddressState.REMOVED -> {
				LaunchedEffect(key1 = Unit) {
					scope.launch {
						delay(0)
						onAction(NoteActivity.Action.SHOW_ADDRESS, null)
					}
				}
			}
		}
	}
}
