package com.syncodec.graphite.todayComponent

import android.graphics.Typeface
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.Graphite
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.LoadingView
import com.syncodec.graphite.custom.revealTextView.RevealText
import com.syncodec.graphite.database.UserDatabase
import com.syncodec.graphite.konstant.Konstant
import com.syncodec.graphite.ui.theme.GraphiteBase
import org.joda.time.DateTime
import org.joda.time.Days
import java.util.*


class TodayActivity : ComponentActivity() {
	val quoteTableDao = UserDatabase.getInstance(this).quoteTableDao

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
		val startDate = remember { DateTime(2022, 5, 4, 0, 0, 0) }
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

			val d = pageDate.dayOfMonth.toString().padStart(2, '0')
			val m = pageDate.monthOfYear.toString().padStart(2, '0')
			val y = pageDate.year.toString().padStart(2, '0')

			Background(
				dmy = "${d}_${m}_${y}",
			)

			QuoteCard(
				d = d,
				m = m,
				y = y,
				loadPage = currentPage == page,
			)
		}
	}

	@Composable
	private fun Background(
		dmy: String,
	) {
		Image(
			painter = rememberImagePainter(
				data = (application as Graphite).getQuoteBg(date = dmy),
				builder = { crossfade(600) }
			),
			contentDescription = null,
			contentScale = ContentScale.Crop,
			colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.31f), BlendMode.SrcOver),
			modifier = Modifier.fillMaxSize(),
		)
	}

	@Composable
	private fun QuoteCard(
		d: String,
		m: String,
		y: String,
		loadPage: Boolean
	) {
		val uriHandler = LocalUriHandler.current

		var isLoaded by remember { mutableStateOf(false) }
		val quote by quoteTableDao.getAsFlow("${d}_${m}_${y}").collectAsState(initial = null)

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
							.requiredSize(32.dp)
							.padding(4.dp)
					)
				}
			}

			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End
				) {
					if (isLoaded && quote != null && quote!!.bgCred != null) {
						AndroidView(
							factory = { context ->
								RevealText(context).apply {
									this.setText("background image")
									this.setTextColor(android.graphics.Color.WHITE)
									this.setBackgroundColor(0)
									this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
									this.setTypeface(
										ResourcesCompat.getFont(context, R.font.overlock_regular),
										Typeface.BOLD_ITALIC
									)
									this.letterSpacing = 0.1f
								}
							},
							update = { view -> view.show() },
							modifier = Modifier
								.alpha(0.47f)
								.clickable(enabled = quote!!.bgLink != null) {
									try {
										uriHandler.openUri(quote!!.bgLink!!)
									} catch (exception: Exception) {
									}
								}
						)
						AndroidView(
							factory = { context ->
								RevealText(context).apply {
									this.setText(" by ")
									this.setTextColor(android.graphics.Color.WHITE)
									this.setBackgroundColor(0)
									this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
									this.setTypeface(
										ResourcesCompat.getFont(context, R.font.overlock_regular),
										Typeface.BOLD_ITALIC
									)
									this.letterSpacing = 0.1f
								}
							},
							update = { view -> view.show() },
							modifier = Modifier.alpha(0.47f)
						)
						AndroidView(
							factory = { context ->
								RevealText(context).apply {
									this.setText("${quote!!.bgCred}")
									this.setTextColor(android.graphics.Color.WHITE)
									this.setBackgroundColor(0)
									this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
									this.setTypeface(
										ResourcesCompat.getFont(context, R.font.overlock_regular),
										Typeface.BOLD_ITALIC
									)
									this.letterSpacing = 0.1f
								}
							},
							update = { view -> view.show() },
							modifier = Modifier
								.alpha(0.47f)
								.clickable(enabled = quote!!.bgCredLink != null) {
									try {
										uriHandler.openUri(quote!!.bgCredLink!!)
									} catch (exception: Exception) {
									}
								}
						)
						AndroidView(
							factory = { context ->
								RevealText(context).apply {
									this.setText(" on ")
									this.setTextColor(android.graphics.Color.WHITE)
									this.setBackgroundColor(0)
									this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
									this.setTypeface(
										ResourcesCompat.getFont(context, R.font.overlock_regular),
										Typeface.BOLD_ITALIC
									)
									this.letterSpacing = 0.1f
								}
							},
							update = { view -> view.show() },
							modifier = Modifier.alpha(0.47f)
						)
						AndroidView(
							factory = { context ->
								RevealText(context).apply {
									this.setText("Pexels")
									this.setTextColor(android.graphics.Color.WHITE)
									this.setBackgroundColor(0)
									this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
									this.setTypeface(
										ResourcesCompat.getFont(context, R.font.overlock_regular),
										Typeface.BOLD_ITALIC
									)
									this.letterSpacing = 0.1f
								}
							},
							update = { view -> view.show() },
							modifier = Modifier
								.alpha(0.47f)
								.clickable(enabled = quote!!.bgCredLink != null) {
									try {
										uriHandler.openUri("https://www.pexels.com/")
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
