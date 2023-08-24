package com.syncodec.graphite.presentation.note.screen.viewerScreen.composable

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.button.GenericButton
import com.syncodec.graphite.presentation.common.text.marqueeText.MarqueeText
import com.syncodec.graphite.utils.FilePreview.Companion.preview
import com.syncodec.graphite.utils.UriPreview.Companion.preview
import com.syncodec.graphite.utils.share
import com.syncodec.graphite.utils.viewExternally
import io.github.esentsov.PackagePrivate
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalFoundationApi::class)
@PackagePrivate
@Composable
fun AttachmentView(
	noteId : RealmUUID,
	attachmentList : List<File>,
) {
//	val context = LocalContext.current
//	val scope = rememberCoroutineScope()
//	val configuration = LocalConfiguration.current
//	val screenHeight = configuration.screenHeightDp.dp
//
//	var isVisible by remember { mutableStateOf(false) }
//	LaunchedEffect(key1 = attachmentList) {
//		if (attachmentList.isNotEmpty()) scope.launch { delay(300); isVisible = true }
//	}
//
//	AnimatedVisibility(
//		visible = isVisible,
//		enter = expandVertically(tween(300)),
//		exit = shrinkVertically(tween(300)),
//	) {
//		val pagerState = rememberPagerState()
//
//		Column(
//			modifier = Modifier
//		) {
//			Box(
//				modifier = Modifier
//					.fillMaxWidth()
//					.height(screenHeight * 0.37f)
//			) {
//
//				var previewHeight by remember { mutableStateOf(null as Int?) }
//
//				HorizontalPager(
//					pageCount = attachmentList.size,
//					state = pagerState,
//					pageSpacing = 4.dp,
//					modifier = Modifier.fillMaxSize()
//				) { page ->
//					val file = attachmentList.getOrNull(page) ?: return@HorizontalPager
//					Column(
//						modifier = Modifier.fillMaxSize()
//					) {
//						Box(
//							modifier = Modifier.weight(1f)
//						) {
//							AttachmentPreview(file = file) { height -> previewHeight = height }
//						}
//
//						AttachmentNamePlate(file = file)
//					}
//				}
//
//				if (attachmentList.size > 1 ) {
//					AttachmentActionButtons(
//						pagerState = pagerState,
//						height = previewHeight,
//					) {
//						Intent(context, AttachmentActivity::class.java).apply {
//							putExtra(Extra.Companion.Extra.NoteId.name, noteId.bytes)
//							context.startActivity(this)
//						}
//					}
//				}
//			}
//
//			Spacer(modifier = Modifier.height(8.dp))
//		}
//	}
}

@Composable
fun AttachmentPreview(
	file : File,
	onGetHeight : (Int) -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	var isPreviewAvailable by remember { mutableStateOf(null as Boolean?) }

	var imageBitmap by remember { mutableStateOf(null as Bitmap?) }
	var imageOverlay by remember { mutableStateOf(null as Int?) }
	LaunchedEffect(key1 = file) {
		scope.launch(Dispatchers.IO) {
			file.preview(context = context).let {
				imageBitmap = it.first
				imageOverlay = it.second
			}
			isPreviewAvailable = imageBitmap != null
		}
	}

	DisposableEffect(key1 = file) { onDispose { scope.cancel(); imageBitmap?.recycle() } }

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxSize()
			.background(
				MaterialTheme.colorScheme
					.surfaceColorAtElevation(8.dp)
					.copy(alpha = 0.13f)
			)
			.clickable { file.viewExternally(context = context) }
			.onGloballyPositioned { coordinates -> onGetHeight(coordinates.size.height) },
	) {
		when (isPreviewAvailable) {
			true -> {
				imageBitmap?.let {
					AsyncImage(
						model = ImageRequest.Builder(context)
							.data(it)
							.crossfade(300)
							.build(),
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = Modifier
							.fillMaxSize()
							.blur(32.dp)
					)
					Box(
						modifier = Modifier
							.fillMaxSize()
							.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f))
					)
					AsyncImage(
						model = ImageRequest.Builder(context)
							.data(it)
							.crossfade(300)
							.build(),
						contentDescription = null,
						contentScale = ContentScale.Fit,
						modifier = Modifier
					)
					if (imageBitmap != null) {
						imageOverlay?.let {
							Icon(
								painter = painterResource(id = it),
								contentDescription = null,
								tint = MaterialTheme.colorScheme.background,
								modifier = Modifier.requiredSize(64.dp)
							)
						}
					}
				}
			}

			false -> {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier,
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_file),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
						modifier = Modifier.requiredSize(64.dp)
					)
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						text = "Preview unavailable",
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
					)
				}
			}

			null -> LoadingView()
		}
	}
}

@Composable
fun AttachmentPreview(
	uri : Uri,
	onGetHeight : (Int) -> Unit = {},
	onClick : () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	var isPreviewAvailable by remember { mutableStateOf(null as Boolean?) }

	var imageBitmap by remember { mutableStateOf(null as Bitmap?) }
	var imageOverlay by remember { mutableStateOf(null as Int?) }
	LaunchedEffect(key1 = uri) {
		scope.launch(Dispatchers.IO) {
			uri.preview(context = context).let {
				imageBitmap = it.first
				imageOverlay = it.second
			}
			isPreviewAvailable = imageBitmap != null
		}
	}

	DisposableEffect(key1 = uri) { onDispose { scope.cancel(); imageBitmap?.recycle() } }

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.fillMaxWidth()
			.background(
				MaterialTheme.colorScheme
					.surfaceColorAtElevation(8.dp)
					.copy(alpha = 0.13f)
			)
			.clickable { onClick() }
			.onGloballyPositioned { coordinates -> onGetHeight(coordinates.size.height) },
	) {
		when (isPreviewAvailable) {
			true -> {
				imageBitmap?.let {
					AsyncImage(
						model = ImageRequest.Builder(context)
							.data(it)
							.crossfade(300)
							.build(),
						contentDescription = null,
						contentScale = ContentScale.Crop,
						modifier = Modifier
							.fillMaxSize()
							.blur(32.dp)
					)
					Box(
						modifier = Modifier
							.fillMaxSize()
							.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f))
					)
					AsyncImage(
						model = ImageRequest.Builder(context)
							.data(it)
							.crossfade(300)
							.build(),
						contentDescription = null,
						contentScale = ContentScale.Fit,
						modifier = Modifier
					)
					if (imageBitmap != null) {
						imageOverlay?.let {
							Icon(
								painter = painterResource(id = it),
								contentDescription = null,
								tint = MaterialTheme.colorScheme.background,
								modifier = Modifier.requiredSize(64.dp)
							)
						}
					}
				}
			}

			false -> {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					modifier = Modifier,
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_file),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
						modifier = Modifier.requiredSize(64.dp)
					)
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						text = "Preview unavailable",
						style = MaterialTheme.typography.bodyLarge,
						color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.71f),
					)
				}
			}

			null -> LoadingView()
		}
	}
}

@Preview
@Composable
private fun AttachmentNamePlate(
	file : File = File("npr.71"),
) {
	val context = LocalContext.current

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(
				MaterialTheme.colorScheme
					.surfaceColorAtElevation(8.dp)
					.copy(alpha = 0.47f)
			)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(16.dp, 8.dp)
		) {
			Spacer(modifier = Modifier.width(4.dp))
			MarqueeText(
				text = file.name,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.weight(1f)
			)
			Spacer(modifier = Modifier.width(8.dp))
			GenericButton(
				icon = R.drawable.ic_share,
			) { file.share(context = context) }
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun AttachmentActionButtons(
	pagerState : PagerState,
	height : Int? = null,
	onClickAttachmentButton : () -> Unit = {},
) {
	val scope = rememberCoroutineScope()

	height?.let {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.height(with(LocalDensity.current) { it.toDp() })
				.padding(10.dp)
		) {
			GenericButton(
				icon = R.drawable.ic_file,
				onClick = onClickAttachmentButton,
			)

			Spacer(modifier = Modifier.weight(1f))
			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				AnimatedVisibility(
					visible = pagerState.canScrollBackward,
					enter = scaleIn(tween(300), 0.71f) + fadeIn(tween(300)),
					exit = scaleOut(tween(300), 0.71f) + fadeOut(tween(300)),
				) {
					GenericButton(
						icon = R.drawable.ic_caret,
						onClick = { if (pagerState.canScrollBackward) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
						modifier = Modifier.graphicsLayer { rotationZ = - 90f }
					)
				}
				Spacer(modifier = Modifier.weight(1f))
				AnimatedVisibility(
					visible = pagerState.canScrollForward,
					enter = scaleIn(tween(300), 0.71f) + fadeIn(tween(300)),
					exit = scaleOut(tween(300), 0.71f) + fadeOut(tween(300)),
				) {
					GenericButton(
						icon = R.drawable.ic_caret,
						onClick = { if (pagerState.canScrollForward) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
						modifier = Modifier.graphicsLayer { rotationZ = 90f }
					)
				}
			}
			Spacer(modifier = Modifier.weight(1f))
			Spacer(modifier = Modifier.height(44.dp))
		}
	}
}
