package com.syncodec.graphite.todayComponent

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.custom.revealTextView.RevealText
import com.syncodec.graphite.database.UserDatabase
import com.syncodec.graphite.konstant.Konstant
import com.syncodec.graphite.ui.theme.GraphiteBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Days
import java.util.*


class TodayActivity : ComponentActivity() {
	private val quoteTableDao = UserDatabase.getInstance(this).quoteTableDao

	val storage = Firebase.storage("gs://graphite-diary.appspot.com")
	val storageRef = storage.reference

	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)

		setContent {
			val systemUiController = rememberSystemUiController()
			systemUiController.setSystemBarsColor(color = Color.Transparent)

			val keyList by quoteTableDao.getAllKeysAsFlow().collectAsState(listOf())
			GraphiteBase {
				if (keyList.isEmpty()) {
					LoadingView()
				} else {
					TodayScreen()
				}
			}
		}
	}

	@ExperimentalPagerApi
	@ExperimentalMaterialApi
	@ExperimentalFoundationApi
	@ExperimentalMaterial3Api
	@Composable
	fun TodayScreen() {
		val startDate = remember { DateTime(2022, 6, 4, 0, 0, 0) }
		val endDate = remember { DateTime.now() }
		val dayCount = remember { Days.daysBetween(startDate, endDate).days + 1 }

		val pagerState = rememberPagerState(dayCount - 1)
		var currentPage by remember { mutableStateOf(dayCount - 1) }

		LaunchedEffect(key1 = pagerState.currentPage) {
			snapshotFlow { pagerState.currentPage }.collect {
				currentPage = it
			}
		}

		HorizontalPager(
			state = pagerState,
			count = dayCount,
			verticalAlignment = Alignment.Bottom,
			itemSpacing = 2.dp,
			modifier = Modifier
				.fillMaxSize()
				.background(Color.Black),
		) { page ->
			val pageDate = startDate.plusDays(page)

			val y = pageDate.year.toString().padStart(2, '0')
			val m = pageDate.monthOfYear.toString().padStart(2, '0')
			val d = pageDate.dayOfMonth.toString().padStart(2, '0')

			Background(
				ymd = "${y}_${m}_${d}",
			)

			QuoteCard(
				y = y,
				m = m,
				d = d,
				loadPage = currentPage == page,
			)
		}
	}

	@Composable
	private fun Background(
		ymd: String,
	) {

		var drawable by remember { mutableStateOf<Drawable?>(null) }

		LaunchedEffect(key1 = null) {
			CoroutineScope(Dispatchers.IO).launch {
				(application as Graphite).loadQuoteBg(ymd = ymd).apply {
					if (this == null) {
						val quoteBgRef = storageRef.child("server/enQuote/$ymd/$ymd.jpg")

						quoteBgRef.downloadUrl.addOnSuccessListener {
							val loader = ImageLoader(this@TodayActivity)
							val request = ImageRequest.Builder(this@TodayActivity)
								.data(it)
								.allowHardware(false)
								.build()

							CoroutineScope(Dispatchers.IO).launch {
								drawable = loader.execute(request).drawable
								drawable?.let { it1 ->
									(application as Graphite).saveQuoteBd(
										ymd = ymd,
										drawable = it1
									)
								}
							}
						}
					} else {
						drawable = this
					}
				}
			}
		}

		AsyncImage(
			model = ImageRequest.Builder(this)
				.data(drawable)
				.crossfade(300)
				.build(),
			placeholder = null,
			contentDescription = null,
			contentScale = ContentScale.Crop,
			colorFilter = ColorFilter.tint(
				Color.Black.copy(alpha = 0.47f),
				BlendMode.SrcOver
			),
			modifier = Modifier.fillMaxSize(),
		)
	}

	@Composable
	private fun QuoteCard(
		y: String,
		m: String,
		d: String,
		loadPage: Boolean
	) {
		val uriHandler = LocalUriHandler.current

		var isLoaded by remember { mutableStateOf(false) }
		val quote by quoteTableDao.getAsFlow("${y}_${m}_${d}").collectAsState(initial = null)

		LaunchedEffect(key1 = loadPage) {
			if (loadPage) isLoaded = true
		}

		val spacerColor by animateColorAsState(
			targetValue = if (isLoaded) Color.White.copy(0.47f) else Color.Transparent,
			animationSpec = tween(600)
		)

		Column(
			modifier = Modifier.fillMaxSize()
		) {
			Spacer(modifier = Modifier.height(40.dp))
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.fillMaxWidth()
					.height(32.dp),
			) {
				IconButton(onClick = { finish() }) {
					Icon(
						painter = painterResource(id = R.drawable.ic_back),
						contentDescription = "Back",
						tint = Color.White,
						modifier = Modifier
					)
				}
			}

			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			) {

				FlowRow(
					crossAxisAlignment = FlowCrossAxisAlignment.Center,
					mainAxisAlignment = MainAxisAlignment.End,
					modifier = Modifier.fillMaxWidth(),
				) {
					quote?.bgLink?.let {
						IconButton(
							onClick = {
								try {
									uriHandler.openUri(it)
								} catch (exception: Exception) {
								}
							}
						) {
							Icon(
								painter = painterResource(id = R.drawable.ic_gallery),
								contentDescription = "Background image",
								tint = Color.White.copy(0.71f),
								modifier = Modifier
							)
						}
					}

					quote?.bgCred?.let {
						Text(
							text = "by $it ",
							style = MaterialTheme.typography.bodyMedium,
							color = Color.White.copy(alpha = 0.47f),
							modifier = Modifier.clickable {
								try {
									quote?.bgCredLink?.let { it1 -> uriHandler.openUri(it1) }
								} catch (exception: Exception) {
								}
							}
						)
						Spacer(modifier = Modifier.height(4.dp))
					}

					quote?.bgProvider?.let {
						Text(
							text = "from $it",
							style = MaterialTheme.typography.bodyMedium,
							color = Color.White.copy(alpha = 0.47f),
							modifier = Modifier.clickable {
								try {
									quote?.bgProviderLink?.let { it1 -> uriHandler.openUri(it1) }
								} catch (exception: Exception) {
								}
							}
						)
					}
				}

				Spacer(modifier = Modifier.weight(1f))

				if (isLoaded) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.Start
					) {
						Column(
							horizontalAlignment = Alignment.Start,
							modifier = Modifier,
						) {
							AndroidView(
								factory = { context ->
									RevealText(context).apply {
										this.setText(d)
										this.setTextColor(android.graphics.Color.WHITE)
										this.setBackgroundColor(0)
										this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 64f)
										this.setTypeface(
											ResourcesCompat.getFont(
												context,
												R.font.graduate_regular
											),
											Typeface.BOLD
										)
										this.letterSpacing = 0.1f
									}
								},
								update = { view -> view.show() }
							)

							Row(
								modifier = Modifier.fillMaxWidth(),
								verticalAlignment = Alignment.CenterVertically
							) {
								AndroidView(
									factory = { context ->
										RevealText(context).apply {
											try {
												val month =
													Konstant.monthNameShort[m.toInt() - 1].uppercase(
														Locale.getDefault()
													)
												this.setText("$month $y")
												this.setTextColor(android.graphics.Color.WHITE)
												this.setBackgroundColor(0)
												this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
												this.setTypeface(
													ResourcesCompat.getFont(
														context,
														R.font.graduate_regular
													),
													Typeface.BOLD
												)
												this.letterSpacing = 0.1f
											} catch (exception: Exception) {
											}
										}
									},
									update = { view -> view.show() }
								)

								Spacer(modifier = Modifier.weight(1f))

								if (quote?.special != "null") {
									Box(
										modifier = Modifier
											.clip(RoundedCornerShape(12.dp))
											.background(Color.Black.copy(alpha = 0.31f))
									) {
										AndroidView(
											factory = { context ->
												RevealText(context).apply {
													try {
														this.setText(quote?.special)
														this.setTextColor(android.graphics.Color.WHITE)
														this.setBackgroundColor(0)
														this.setTextSize(
															TypedValue.COMPLEX_UNIT_SP,
															12f
														)
														this.setTypeface(
															ResourcesCompat.getFont(
																context,
																R.font.graduate_regular
															),
															Typeface.BOLD
														)
														this.letterSpacing = 0.1f
													} catch (exception: Exception) {
													}
												}
											},
											modifier = Modifier.padding(12.dp, 8.dp),
											update = { view -> view.show() }
										)
									}
								}
							}
						}
					}
				}

				Spacer(modifier = Modifier.height(24.dp))

				if (quote != null && isLoaded) {
					AndroidView(
						factory = { context ->
							RevealText(context).apply {
								this.setText(quote?.quote)
								this.setTextColor(android.graphics.Color.WHITE)
								this.setBackgroundColor(0)
								this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
								this.setTypeface(
									ResourcesCompat.getFont(context, R.font.overlock_regular),
									Typeface.NORMAL
								)
								this.letterSpacing = 0.1f
							}
						},
						update = { view -> view.show() },
						modifier = Modifier.fillMaxWidth()
					)
				}

				Spacer(modifier = Modifier.height(24.dp))

				AnimatedVisibility(
					visible = isLoaded,
					enter = fadeIn(tween(600)),
					exit = fadeOut(tween(600))
				) {
					Box(
						modifier = Modifier
							.fillMaxWidth(0.71f)
							.height(1.dp)
							.padding(16.dp, 0.dp, 0.dp, 0.dp)
							.clip(RoundedCornerShape(50))
							.background(spacerColor)
					)
				}

				Spacer(modifier = Modifier.height(16.dp))

				if (quote != null && isLoaded) {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier
							.fillMaxWidth()
							.padding(0.dp, 0.dp, 16.dp, 0.dp),
					) {
						Spacer(modifier = Modifier.weight(1f))

						AndroidView(
							factory = { context ->
								RevealText(context).apply {
									this.setText("~ ${quote?.author}")
									this.setTextColor(android.graphics.Color.WHITE)
									this.setBackgroundColor(0)
									this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
									this.setTypeface(
										ResourcesCompat.getFont(context, R.font.overlock_bold),
										Typeface.BOLD
									)
									this.gravity = Gravity.END
									this.letterSpacing = 0.1f
								}
							},
							update = { view -> view.show() },
							modifier = Modifier
								.alpha(0.47f)
								.clickable(enabled = quote != null && quote!!.authorLink != null) {
									try {
										uriHandler.openUri(quote!!.authorLink!!)
									} catch (exception: Exception) {
									}
								}
						)
					}
				}

				Spacer(modifier = Modifier.height(16.dp))
			}
		}
	}
}
