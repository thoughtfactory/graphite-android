package com.syncodec.graphite.mainComponent.screen

import android.graphics.Typeface
import android.util.TypedValue
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.MainActivity
import com.syncodec.graphite.R
import com.syncodec.graphite.custom.revealTextView.RevealText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
	onAction: (MainActivity.Action) -> Unit
) {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	val systemUiController = rememberSystemUiController()
	systemUiController.setStatusBarColor(MaterialTheme.colorScheme.surface)
	systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.surface)

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface),
	) {
		Spacer(modifier = Modifier.height(16.dp))
		Image(
			painter = painterResource(id = R.drawable.il_login_background),
			contentDescription = null,
			contentScale = ContentScale.Fit,
			modifier = Modifier
				.height(screenHeight / 3)
				.padding(16.dp),
		)

		Spacer(modifier = Modifier.height(12.dp))

		NameCard()

		Spacer(modifier = Modifier.height(24.dp))

		ContentCard()

		LoginCard(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
				.padding(32.dp, 0.dp)
		) { onAction(it) }
	}
}

@Composable
private fun NameCard() {
	val textColor = MaterialTheme.colorScheme.onSurface
	Column(
		modifier = Modifier.padding(24.dp, 0.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		AndroidView(
			factory = {
				RevealText(it).apply {
					this.setText("GRAPHITE")
					this.setTextColor(textColor.toArgb())
					this.setBackgroundColor(0)
					this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
					this.setTypeface(
						ResourcesCompat.getFont(context, R.font.graduate_regular),
						Typeface.BOLD
					)
					this.letterSpacing = 0.1f
					this.setDuration(2400)
				}
			}
		) { it.show() }
		Spacer(modifier = Modifier.height(8.dp))
		AndroidView(
			factory = {
				RevealText(it).apply {
					this.setText("STORE YOUR EVERY MOMENTS")
					this.setTextColor(textColor.toArgb())
					this.setBackgroundColor(0)
					this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
					this.setTypeface(
						ResourcesCompat.getFont(context, R.font.graduate_regular),
						Typeface.NORMAL
					)
					this.letterSpacing = 0.1f
					this.lineHeight = 2
					this.setDuration(2400)
				}
			}
		) { it.show() }
	}
}

@Composable
private fun ContentCard() {
	val scope = rememberCoroutineScope()
	var showJournal by remember { mutableStateOf(false) }
	var showNotebook by remember { mutableStateOf(false) }
	var showBucketList by remember { mutableStateOf(false) }

	LaunchedEffect(key1 = null) {
		scope.launch {
			showJournal = true
			delay(200)
			showNotebook = true
			delay(200)
			showBucketList = true
		}
	}


	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(32.dp, 0.dp, 0.dp, 0.dp),
	) {
		AnimatedVisibility(
			visible = showJournal,
			enter = slideInHorizontally(tween(durationMillis = 800)) + fadeIn(tween(durationMillis = 800))
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
					fontWeight = FontWeight.Normal,
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
			enter = slideInHorizontally(tween(durationMillis = 800)) + fadeIn(tween(durationMillis = 800))
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
					fontWeight = FontWeight.Normal,
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
			enter = slideInHorizontally(tween(durationMillis = 800)) + fadeIn(tween(durationMillis = 800))
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
					fontWeight = FontWeight.Normal,
					fontSize = 20.sp,
					lineHeight = 24.sp,
					letterSpacing = 2.sp,
					color = MaterialTheme.colorScheme.onSurface
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginCard(
	modifier: Modifier,
	onAction: (MainActivity.Action) -> Unit
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
						if (isAgreedToTerms) {
							scope.launch {
								delay(800)
								onAction(MainActivity.Action.LOGIN)
							}
						} else {
							Toast.makeText(
								context,
								"Please agree to Terms of Service and Privacy Policy",
								Toast.LENGTH_SHORT
							).show()
						}
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
						fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
						fontWeight = FontWeight.Normal,
						fontSize = 16.sp,
						lineHeight = 20.sp,
						letterSpacing = 2.sp,
					)
				}

				Spacer(modifier = Modifier.height(4.dp))

				Button(
					onClick = {
						if (isAgreedToTerms) {
							onAction(MainActivity.Action.TRY_FIRST)
						} else {
							Toast.makeText(
								context,
								"Please agree to Terms of Service and Privacy Policy",
								Toast.LENGTH_SHORT
							).show()
						}
					},
					colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
					border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
					shape = RoundedCornerShape(12.dp)
				) {
					Text(
						text = "I WOULD LIKE TO TRY FIRST",
						modifier = Modifier,
						fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
						fontWeight = FontWeight.Normal,
						fontSize = 12.sp,
						lineHeight = 16.sp,
						letterSpacing = 2.sp,
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
						addStyle(
							style = SpanStyle(
								color = MaterialTheme.colorScheme.primary,
								fontSize = 14.sp,
							), start = 0, end = 15
						)
						addStyle(
							style = SpanStyle(
								color = MaterialTheme.colorScheme.onSurface,
								fontSize = 14.sp,
							), start = 15, end = 15 + 16
						)
						addStyle(
							style = SpanStyle(
								color = MaterialTheme.colorScheme.primary,
								fontSize = 14.sp,
							), start = 15 + 16, end = 15 + 16 + 5
						)
						addStyle(
							style = SpanStyle(
								color = MaterialTheme.colorScheme.onSurface,
								fontSize = 14.sp,
							), start = 15 + 16 + 5, end = 15 + 16 + 5 + 14
						)

						addStringAnnotation(
							tag = "Terms of Service",
							annotation = "https://github.com",
							start = 15,
							end = 15 + 16
						)
						addStringAnnotation(
							tag = "Privacy Policy",
							annotation = "https://github.com",
							start = 15 + 16 + 5,
							end = 15 + 16 + 5 + 14
						)
					}

					val uriHandler = LocalUriHandler.current
					Column(modifier = Modifier) {
						Spacer(modifier = Modifier.height(15.dp))
						ClickableText(
							modifier = Modifier,
							text = annotatedLinkString,
							style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp),
							onClick = {
								annotatedLinkString
									.getStringAnnotations("URL", it, it)
									.firstOrNull()?.let { stringAnnotation ->
										uriHandler.openUri(stringAnnotation.item)
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
