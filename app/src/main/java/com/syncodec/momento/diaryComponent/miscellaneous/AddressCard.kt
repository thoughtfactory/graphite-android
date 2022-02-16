package com.syncodec.momento.diaryComponent.miscellaneous

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.syncodec.momento.R
import com.syncodec.momento.diaryComponent.DiaryActivity
import com.syncodec.momento.diaryComponent.DiaryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddressCard() {
	val context = LocalContext.current
	val viewModel: DiaryViewModel = viewModel()

	val scope = rememberCoroutineScope()
	var isLoadedOnce by remember { mutableStateOf(false) }

	val addressState by viewModel.diaryActivityState.addressState
	var showAddressCard by viewModel.diaryActivityState.showAddressCard

	val animateAlpha by animateFloatAsState(
		targetValue = if (showAddressCard) 1f else 0f,
		animationSpec = tween(
			durationMillis = 400
		),
		finishedListener = {
			scope.launch {
				delay(6400)
				showAddressCard = false
			}
		}
	)

	Box(
		modifier = Modifier
			.graphicsLayer {
				this.alpha = animateAlpha
			}
	) {
		when (addressState) {
			DiaryActivity.AddressState.OFF -> {
				Log.i("npr71", "AddressState : OFF")
			}
			DiaryActivity.AddressState.INIT -> {
				Log.i("npr71", "AddressState : INIT")
			}
			DiaryActivity.AddressState.NO_PERMISSION -> {
				Log.i("npr71", "AddressState : NO_PERMISSION")
			}
			DiaryActivity.AddressState.REQUEST_PERMISSION -> {
				Log.i("npr71", "AddressState : REQUEST_PERMISSION")
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
							onClick = { viewModel.diaryActivityState.locationPermissionState.launchPermissionRequest() },
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
			DiaryActivity.AddressState.SHOW_RATIONALE -> {
				Log.i("npr71", "AddressState : SHOW_RATIONALE")
			}
			DiaryActivity.AddressState.REQUESTED -> {
				Log.i("npr71", "AddressState : REQUESTED")
			}
			DiaryActivity.AddressState.LOCATION -> {
				Log.i("npr71", "AddressState : LOCATION")
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
							text = "Address unavailable\nLat : ${viewModel.location!!.latitude}, Lng : ${viewModel.location!!.longitude}",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground,
							fontWeight = FontWeight.Bold,
							modifier = Modifier
								.weight(1f)
						)
					}
				}
			}
			DiaryActivity.AddressState.SUCCESS -> {
				Log.i("npr71", "AddressState : SUCCESS")
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
							text = viewModel.address!!,
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground,
							fontWeight = FontWeight.Bold,
							modifier = Modifier
								.weight(1f)
						)
					}
				}
			}
			DiaryActivity.AddressState.ERROR -> {
				Log.i("npr71", "AddressState : ERROR")
			}
			DiaryActivity.AddressState.REMOVED -> {
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
			DiaryActivity.AddressState.OFF -> {}
			DiaryActivity.AddressState.INIT -> {}
			DiaryActivity.AddressState.NO_PERMISSION -> {}
			DiaryActivity.AddressState.REQUEST_PERMISSION -> {
				LaunchedEffect(key1 = true) {
					scope.launch {
						delay(1200)
						viewModel.diaryActivityState.showAddressCard.value = true
					}
				}
			}
			DiaryActivity.AddressState.SHOW_RATIONALE -> {}
			DiaryActivity.AddressState.REQUESTED -> {}
			DiaryActivity.AddressState.LOCATION -> {
				LaunchedEffect(key1 = true) {
					scope.launch {
						delay(1200)
						viewModel.diaryActivityState.showAddressCard.value = true
					}
				}
			}
			DiaryActivity.AddressState.SUCCESS -> {
				LaunchedEffect(key1 = true) {
					scope.launch {
						delay(1200)
						viewModel.diaryActivityState.showAddressCard.value = true
					}
				}
			}
			DiaryActivity.AddressState.ERROR -> {}
			DiaryActivity.AddressState.REMOVED -> {
				LaunchedEffect(key1 = true) {
					scope.launch {
						delay(0)
						viewModel.diaryActivityState.showAddressCard.value = true
					}
				}
			}
		}
	}
}
