package com.syncodec.momento.diaryComponent.miscellaneous

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.CancellationTokenSource
import com.syncodec.momento.BuildConfig
import com.syncodec.momento.database.diary.WeatherData
import com.syncodec.momento.diaryComponent.DiaryViewModel
import com.syncodec.momento.konstant.Secret
import compose.icons.TablerIcons
import compose.icons.tablericons.MapPin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.util.*


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddressCard() {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val viewModel: DiaryViewModel = viewModel()

	var isLocationRequested: Boolean by remember { mutableStateOf(false) }

	var fusedLocationClient: FusedLocationProviderClient = FusedLocationProviderClient(context)
	var cancellationToken = CancellationTokenSource().token

	val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

	var showAddressCard by remember { mutableStateOf(false) }
	var instantUpdate by remember { mutableStateOf(true) }
	var cardSize by remember { mutableStateOf(IntSize.Zero) }

	val translateY by animateFloatAsState(
		targetValue = if (showAddressCard) 0f else cardSize.height.toFloat(),
		animationSpec = tween(
			durationMillis = if (instantUpdate) 0 else 400
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
				this.translationY = -translateY
			}
	) {
		when {
			locationPermissionState.hasPermission -> {
				LaunchedEffect(key1 = true) {
					delay(1200)
					if (!isLocationRequested) {
						isLocationRequested = true

						fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationToken)
						fusedLocationClient.lastLocation
							.addOnSuccessListener { location: Location? ->
								viewModel.location = location

								if (location != null) {
									scope.launch {
										withContext(Dispatchers.IO) {
											val weatherRequestUrl =
												"https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.latitude}&appid=${Secret.OPEN_WEATHER_KEY}"
											val weatherRequestQueue = Volley.newRequestQueue(context)
											val stringRequest = StringRequest(
												Request.Method.GET,
												weatherRequestUrl,
												{ requestResult ->
													val jsonObject = JSONObject(requestResult)
													val weatherList = jsonObject.getJSONArray("weather")
													if (weatherList.length() > 0) {
														val weather = JSONObject(weatherList.get(0).toString())
														val main = jsonObject.getJSONObject("main")
														scope.launch {
															withContext(Dispatchers.Main) {
																WeatherData(
																	icon = weather.getString("icon"),
																	description = weather.getString("description"),
																	temperature = main.getDouble("temp")
																).apply { viewModel.weatherData = this }
															}
														}
													}
												},
												{
												}
											)
											weatherRequestQueue.add(stringRequest)
										}
									}
								}
								if (location!=null) {
									scope.launch {
										withContext(Dispatchers.IO) {
											try {
												val geocoder = Geocoder(context, Locale.getDefault())
												val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
												if (addressList.isNotEmpty()) {
													val address = addressList.first()
													withContext(Dispatchers.Main) {
														viewModel.address =
															"${address.featureName} ${address.thoroughfare}, ${address.locality}, ${address.subAdminArea}, ${address.adminArea} ${address.postalCode}, ${address.countryName}"

														instantUpdate = false
														showAddressCard = true
													}
												}
											} catch (exception: IOException) {

											} catch (exception: Exception) {

											}
										}
									}
								}
							}
							.addOnFailureListener {
							}
					}
				}
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.background)
						.onGloballyPositioned { cardSize = it.size }
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.padding(16.dp, 12.dp),
					) {
						Text(
							text = if (viewModel.location == null) "Waiting for location..." else {
								if (viewModel.address == null)
									"Address unavailable : ${viewModel.location!!.latitude}, ${viewModel.location!!.latitude}"
								else viewModel.address!!
							},
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground,
							modifier = Modifier
								.fillMaxWidth(0.88f)
						)

						Spacer(modifier = Modifier.weight(1f))

						Icon(
							imageVector = TablerIcons.MapPin,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onBackground,
							modifier = Modifier
								.requiredSize(20.dp)
						)
					}
				}
			}
			locationPermissionState.shouldShowRationale -> {
				LaunchedEffect(true) {
					delay(3200)
					instantUpdate = false
					showAddressCard = true
				}
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.background)
						.onGloballyPositioned { cardSize = it.size }
						.clickable(showAddressCard) { locationPermissionState.launchPermissionRequest() }
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.padding(16.dp, 12.dp),
					) {
						Text(
							text = "Want to save location for this note? Click to provide permission.",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground,
							modifier = Modifier
								.fillMaxWidth(0.9f)
						)
					}
				}
			}
			else -> {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.background)
						.onGloballyPositioned { cardSize = it.size }
						.clickable(showAddressCard) {
							val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
							val uri: Uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
							intent.data = uri
							context.startActivity(intent)
						}
				) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Center,
						modifier = Modifier
							.padding(16.dp, 12.dp),
					) {
						Text(
							text = "Location permission unavailable",
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onBackground,
							modifier = Modifier
								.fillMaxWidth(0.8f)
						)
					}
				}
			}
		}
	}
}
