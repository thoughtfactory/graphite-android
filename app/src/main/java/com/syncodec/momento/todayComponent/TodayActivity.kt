package com.syncodec.momento.todayComponent

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.momento.R
import com.syncodec.momento.custom.revealTextView.RevealText
import com.syncodec.momento.ui.theme.MomentoTheme

class TodayActivity : ComponentActivity() {
	@OptIn(
		ExperimentalPagerApi::class, ExperimentalMaterialApi::class,
		ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class
	)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)

		setContent {
			MomentoTheme {
				TodayScreen()
			}
		}
	}

	@Preview
	@ExperimentalPagerApi
	@ExperimentalMaterialApi
	@ExperimentalFoundationApi
	@ExperimentalMaterial3Api
	@Composable
	fun TodayScreen() {
		val systemUiController = rememberSystemUiController()
		systemUiController.setSystemBarsColor(
			color = Color.Transparent
		)

		val pagerState = rememberPagerState(9)
		HorizontalPager(
			state = pagerState,
			count = 10,
			verticalAlignment = Alignment.Bottom,
			itemSpacing = 2.dp,	modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight()
				.background(Color.Black),
		) { page ->
			Image(
				painter = painterResource(id = R.drawable.background),
				contentDescription = null,
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight()
			)

			Column(
				modifier = Modifier
					.fillMaxWidth()
					.fillMaxHeight()
					.padding(20.dp, 20.dp, 20.dp, 32.dp),
				verticalArrangement = Arrangement.Bottom
			) {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					AndroidView(
						factory = { context ->
							RevealText(context).apply {
								this.setText("04")
								this.setTextColor(android.graphics.Color.WHITE)
								this.setBackgroundColor(0)
								this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 64f)
								this.setTypeface(ResourcesCompat.getFont(context, R.font.montserrat_light), Typeface.NORMAL)
								this.letterSpacing = 0.1f
							}
						},
						update = { view ->
							view.show()
						}
					)

					AndroidView(
						factory = { context ->
							RevealText(context).apply {
								this.setText("Map 2022")
								this.setTextColor(android.graphics.Color.WHITE)
								this.setBackgroundColor(0)
								this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
								this.setTypeface(ResourcesCompat.getFont(context, R.font.montserrat_medium), Typeface.NORMAL)
								this.letterSpacing = 0.1f
							}
						},
						update = { view ->
							view.show()
						}
					)
				}

				Spacer(modifier = Modifier.height(32.dp))

				AndroidView(
					factory = { context ->
						RevealText(context).apply {
							this.setText("Looking down the misty path to uncertain destinations.")
							this.setTextColor(android.graphics.Color.WHITE)
							this.setBackgroundColor(0)
							this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
							this.setTypeface(ResourcesCompat.getFont(context, R.font.source_sans_pro_semi_bold), Typeface.BOLD)
							this.letterSpacing = 0.1f
						}
					},
					update = { view ->
						view.show()
					}
				)

				Spacer(modifier = Modifier.height(24.dp))

				Spacer(
					modifier = Modifier
						.height(2.dp)
						.width(40.dp)
						.padding(8.dp, 0.dp, 0.dp, 0.dp)
						.background(Color(255, 255, 255, 96), RoundedCornerShape(2.dp))
						.align(Alignment.Start)
				)

				Spacer(modifier = Modifier.height(24.dp))

				Row {
					AndroidView(
						factory = { context ->
							RevealText(context).apply {
								this.setText("429")
								this.setTextColor(android.graphics.Color.WHITE)
								this.setBackgroundColor(0)
								this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
								this.setTypeface(ResourcesCompat.getFont(context, R.font.courgette_regular), Typeface.NORMAL)
								this.letterSpacing = 0.1f
							}
						},
						update = { view ->
							view.show()
						}
					)
				}
			}
		}
	}

}
