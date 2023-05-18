package com.syncodec.graphite.presentation.main.composable.screen

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.util.TypedValue
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.text.revealTextView.RevealText
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Preview
@Composable
fun FirstTimeScreen(
	onClickLogin: () -> Unit = { },
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp
	val screenHeight = configuration.screenHeightDp.dp

	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val textColor = MaterialTheme.colorScheme.onSurface

	var showContent by remember { mutableStateOf(true) }

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))
	) {
		Column(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Spacer(modifier = Modifier.height(screenHeight / 16))

			Image(
				painter = painterResource(id = R.drawable.il_loader_illustration),
				contentDescription = null,
				modifier = Modifier.requiredSize(screenWidth * 3 / 4)
			)

			AndroidView(
				factory = {
					RevealText(it).apply {
						this.setText("GRAPHITE")
						this.setTextColor(textColor.toArgb())
						this.setBackgroundColor(0)
						this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 47f)
						this.setTypeface(ResourcesCompat.getFont(context, R.font.graduate_regular), Typeface.BOLD)
						this.letterSpacing = 0.1f
						this.setDuration(2400)
					}
				}
			) { it.show() }

			Spacer(modifier = Modifier.height(12.dp))

			AndroidView(
				factory = {
					RevealText(it).apply {
						this.setText("A LOCAL FIRST")
						this.setTextColor(textColor.toArgb())
						this.setBackgroundColor(0)
						this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f)
						this.setTypeface(
							ResourcesCompat.getFont(context, R.font.graduate_regular),
							Typeface.BOLD
						)
						this.letterSpacing = 0.1f
						this.setDuration(2400)
					}
				}
			) { it.show() }

			Spacer(modifier = Modifier.height(24.dp))

			ContentCard(showContent = showContent)

			LoginCard(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
					.padding(32.dp, 0.dp),
				onClickLogin = onClickLogin,
				onClickTryFirst = {
					scope.launch {
						showContent = false
						delay(1200)
						dataStoreInstance.putIsFirstTime(false)
					}
				}
			)

			Spacer(modifier = Modifier.height(12.dp))

			Spacer(modifier = Modifier.height(screenHeight / 8))
		}
	}
}

@Composable
private fun ContentCard(
	showContent: Boolean
) {
	val scope = rememberCoroutineScope()
	var showJournal by remember { mutableStateOf(false) }
	var showNotebook by remember { mutableStateOf(false) }
	var showBucketList by remember { mutableStateOf(false) }

	LaunchedEffect(key1 = showContent) {
		scope.launch {
			showJournal = showContent
			delay(200)
			showNotebook = showContent
			delay(200)
			showBucketList = showContent
		}
	}

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(32.dp, 0.dp, 0.dp, 0.dp),
	) {
		AnimatedVisibility(
			visible = showJournal,
			enter = slideInHorizontally(tween(durationMillis = 1600)) + fadeIn(tween(durationMillis = 1600)),
			exit = slideOutHorizontally(tween(durationMillis = 1600)) { it / 2 } + fadeOut(tween(durationMillis = 1600)),
		) {
			Row(
				modifier = Modifier,
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_pencil),
					contentDescription = "Journal",
					tint = Color(0xFF5EAAA8),
					modifier = Modifier.requiredSize(28.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "JOURNAL",
					modifier = Modifier,
					fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
					fontWeight = FontWeight.Bold,
					fontSize = 20.sp,
					lineHeight = 24.sp,
					letterSpacing = 2.sp,
					color = MaterialTheme.colorScheme.onSurface
				)
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		AnimatedVisibility(
			visible = showNotebook,
			enter = slideInHorizontally(tween(durationMillis = 1600)) + fadeIn(tween(durationMillis = 1600)),
			exit = slideOutHorizontally(tween(durationMillis = 1600)) { it / 2 } + fadeOut(tween(durationMillis = 1600)),
		) {
			Row(
				modifier = Modifier,
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_notebook),
					contentDescription = "Notebook",
					tint = Color(0xFFFF6464),
					modifier = Modifier.requiredSize(28.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "NOTEBOOK",
					modifier = Modifier,
					fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
					fontWeight = FontWeight.Bold,
					fontSize = 20.sp,
					lineHeight = 24.sp,
					letterSpacing = 2.sp,
					color = MaterialTheme.colorScheme.onSurface
				)
			}
		}

		Spacer(modifier = Modifier.height(12.dp))

		AnimatedVisibility(
			visible = showBucketList,
			enter = slideInHorizontally(tween(durationMillis = 1600)) + fadeIn(tween(durationMillis = 1600)),
			exit = slideOutHorizontally(tween(durationMillis = 1600)) { it / 2 } + fadeOut(tween(durationMillis = 1600)),
		) {
			Row(
				modifier = Modifier,
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_bucket),
					contentDescription = "Bucket List",
					tint = Color(0xFF6C63FF),
					modifier = Modifier.requiredSize(28.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "BUCKET LIST",
					modifier = Modifier,
					fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
					fontWeight = FontWeight.Bold,
					fontSize = 20.sp,
					lineHeight = 24.sp,
					letterSpacing = 2.sp,
					color = MaterialTheme.colorScheme.onSurface
				)
			}
		}
	}
}

@Composable
private fun LoginCard(
	modifier: Modifier,
	onClickLogin: () -> Unit,
	onClickTryFirst: () -> Unit
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var isVisible by remember { mutableStateOf(false) }
	var isAgreedToTerms by remember { mutableStateOf(false) }

	LaunchedEffect(key1 = null) { isVisible = true }

	Box(
		modifier = Modifier.fillMaxSize(),
		contentAlignment = Alignment.BottomCenter
	) {
		AnimatedVisibility(
			visible = isVisible,
			enter = fadeIn(tween(2400))
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Bottom,
				modifier = modifier
			) {
				Button(
					onClick = {
						if (isAgreedToTerms) onClickLogin()
						else Toast.makeText(context, "Please agree to Terms of Service and Privacy Policy", Toast.LENGTH_SHORT).show()
					},
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.primary,
						contentColor = MaterialTheme.colorScheme.onPrimary
					),
					shape = RoundedCornerShape(12.dp)
				) {
					Text(
						text = "LOGIN",
						modifier = Modifier,
						fontFamily = FontFamily(Font(R.font.graduate_regular)),
					)
				}

				Spacer(modifier = Modifier.height(2.dp))

				Button(
					onClick = {
						if (isAgreedToTerms) onClickTryFirst()
						else Toast.makeText(context, "Please agree to Terms of Service and Privacy Policy", Toast.LENGTH_SHORT).show()
					},
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.background,
						contentColor = MaterialTheme.colorScheme.onBackground
					),
					shape = RoundedCornerShape(12.dp)
				) {
					Text(
						text = "I WOULD LIKE TO TRY FIRST",
						modifier = Modifier,
						fontFamily = FontFamily(Font(R.font.graduate_regular)),
					)
				}

				Spacer(modifier = Modifier.height(24.dp))

				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.Top
				) {
					Checkbox(
						checked = isAgreedToTerms,
						onCheckedChange = { isAgreedToTerms = !isAgreedToTerms },
						colors = CheckboxDefaults.colors(
							checkedColor = MaterialTheme.colorScheme.primary,
							uncheckedColor = MaterialTheme.colorScheme.primary,
						)
					)

					val annotatedLinkString: AnnotatedString = buildAnnotatedString {
						val str = "I agree to the Terms of Service and Privacy Policy"
						append(str)
//						** I agree to the
						addStyle(
							style = SpanStyle(
								color = MaterialTheme.colorScheme.primary,
								fontSize = 14.sp,
							), start = 0, end = 15
						)
//						** Terms of Service
						addStyle(
							style = SpanStyle(
								color = MaterialTheme.colorScheme.onSurface,
								fontSize = 14.sp,
								textDecoration = TextDecoration.Underline
							), start = 15, end = 15 + 16
						)
//						** and
						addStyle(
							style = SpanStyle(
								color = MaterialTheme.colorScheme.primary,
								fontSize = 14.sp,
							), start = 15 + 16, end = 15 + 16 + 5
						)
//						** Privacy Policy
						addStyle(
							style = SpanStyle(
								color = MaterialTheme.colorScheme.onSurface,
								fontSize = 14.sp,
								textDecoration = TextDecoration.Underline
							), start = 15 + 16 + 5, end = 15 + 16 + 5 + 14
						)

						addStringAnnotation(
							tag = "url",
							annotation = "https://graphite.syncodec.com/terms.html",
							start = 15,
							end = 15 + 16
						)
						addStringAnnotation(
							tag = "url",
							annotation = "https://graphite.syncodec.com/policy.html",
							start = 15 + 16 + 5,
							end = 15 + 16 + 5 + 14
						)
					}

					Column(modifier = Modifier) {
						Spacer(modifier = Modifier.height(15.dp))
						ClickableText(
							modifier = Modifier,
							text = annotatedLinkString,
							style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
							onClick = {
								annotatedLinkString
									.getStringAnnotations("url", it, it)
									.firstOrNull()?.let { (item, start, end, tag) ->
										context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item)))
									}
							}
						)
					}
				}

				Spacer(modifier = Modifier.height(32.dp))
			}
		}
	}
}
