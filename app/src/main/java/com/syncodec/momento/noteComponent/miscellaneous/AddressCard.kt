package com.syncodec.momento.noteComponent.miscellaneous

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.syncodec.momento.R
import com.syncodec.momento.noteComponent.NoteActivity
import com.syncodec.momento.noteComponent.NoteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddressCard() {
	val context = LocalContext.current
	val noteViewModel: NoteViewModel = viewModel()

	val scope = rememberCoroutineScope()

	val showCardPreference = true

	val addressState by noteViewModel.activityState.addressState
	var showAddressCard by noteViewModel.activityState.showAddressCard

	val animateAlpha by animateFloatAsState(
		targetValue = if (showAddressCard && showCardPreference != false) 1f else 0f,
		animationSpec = tween(durationMillis = 600),
		finishedListener = {
			scope.launch {
				delay(6400)
				showAddressCard = false
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
							onClick = { noteViewModel.activityState.locationPermissionState.launchPermissionRequest() },
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
							text = "Address unavailable\nLat : ${noteViewModel.location!!.latitude}, Lng : ${noteViewModel.location!!.longitude}",
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
							text = noteViewModel.address!!,
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
						noteViewModel.activityState.showAddressCard.value = true
					}
				}
			}
			NoteActivity.AddressState.SHOW_RATIONALE -> {}
			NoteActivity.AddressState.REQUESTED -> {}
			NoteActivity.AddressState.LOCATION -> {
				LaunchedEffect(key1 = Unit) {
					scope.launch {
						delay(1200)
						noteViewModel.activityState.showAddressCard.value = true
					}
				}
			}
			NoteActivity.AddressState.SUCCESS -> {
				LaunchedEffect(key1 = Unit) {
					scope.launch {
						delay(1200)
						noteViewModel.activityState.showAddressCard.value = true
					}
				}
			}
			NoteActivity.AddressState.ERROR -> {}
			NoteActivity.AddressState.REMOVED -> {
				LaunchedEffect(key1 = Unit) {
					scope.launch {
						delay(0)
						noteViewModel.activityState.showAddressCard.value = true
					}
				}
			}
		}
	}
}
