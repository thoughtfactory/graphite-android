package com.syncodec.momento.noteComponent.miscellaneous

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.syncodec.momento.R
import com.syncodec.momento.database.note.LocationData
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
	locationData: LocationData?,
	onClick: (NoteActivity.Click) -> Unit
) {
	val scope = rememberCoroutineScope()

	val showCardPreference = true

	val animateAlpha by animateFloatAsState(
		targetValue = if (showAddressCard && showCardPreference) 1f else 0f,
		animationSpec = tween(durationMillis = 600),
		finishedListener = {
			scope.launch {
				delay(6400)
				onClick(NoteActivity.Click.HIDE_ADDRESS)
			}
		}
	)

	Box(
		modifier = Modifier
			.fillMaxSize()
			.graphicsLayer {
				this.alpha = animateAlpha
			},
		contentAlignment = Alignment.BottomCenter
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
						.background(MaterialTheme.colorScheme.background)
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.padding(16.dp, 12.dp),
					) {
						Text(
							text = "Want to geotag your entry?\nKeep your memory connected with location",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground,
							fontWeight = FontWeight.Bold,
							modifier = Modifier
								.weight(1f)
						)

						Button(
							onClick = { onClick(NoteActivity.Click.REQUEST_LOCATION_PERMISSION) },
							colors = ButtonDefaults.outlinedButtonColors(
								containerColor = MaterialTheme.colorScheme.primaryContainer
							),
							border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
							modifier = Modifier
								.wrapContentWidth(),
						) {
							Row(
								modifier = Modifier,
								verticalAlignment = Alignment.CenterVertically
							) {
								Icon(
									painter = painterResource(id = R.drawable.ic_map_marker),
									contentDescription = null,
									tint = MaterialTheme.colorScheme.onPrimaryContainer,
									modifier = Modifier
										.requiredSize(20.dp)
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
					}
				}
			}
			NoteActivity.AddressState.SHOW_RATIONALE -> {
			}
			NoteActivity.AddressState.REQUESTED -> {
			}
			NoteActivity.AddressState.LOCATION -> {
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
							text = "Address unavailable\nLat : ${locationData?.latitude}, Lng : ${locationData?.longitude}",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground,
							fontWeight = FontWeight.Bold,
							modifier = Modifier
								.weight(1f)
						)
					}
				}
			}
			NoteActivity.AddressState.SUCCESS -> {
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
							text = address ?: "",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground,
							fontWeight = FontWeight.Bold,
							modifier = Modifier
								.weight(1f)
						)
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
						onClick(NoteActivity.Click.SHOW_ADDRESS)
					}
				}
			}
			NoteActivity.AddressState.SHOW_RATIONALE -> {}
			NoteActivity.AddressState.REQUESTED -> {}
			NoteActivity.AddressState.LOCATION -> {
				LaunchedEffect(key1 = Unit) {
					scope.launch {
						delay(1200)
						onClick(NoteActivity.Click.SHOW_ADDRESS)
					}
				}
			}
			NoteActivity.AddressState.SUCCESS -> {
				LaunchedEffect(key1 = Unit) {
					scope.launch {
						delay(1200)
						onClick(NoteActivity.Click.SHOW_ADDRESS)
					}
				}
			}
			NoteActivity.AddressState.ERROR -> {}
			NoteActivity.AddressState.REMOVED -> {
				LaunchedEffect(key1 = Unit) {
					scope.launch {
						delay(0)
						onClick(NoteActivity.Click.SHOW_ADDRESS)
					}
				}
			}
		}
	}
}
