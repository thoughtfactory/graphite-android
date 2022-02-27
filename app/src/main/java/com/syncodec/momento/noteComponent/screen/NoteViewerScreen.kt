package com.syncodec.momento.noteComponent.screen

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.syncodec.momento.custom.LoadingView
import com.syncodec.momento.custom.richText.rememberRichTextEditorWithLifecycle
import com.syncodec.momento.database.diary.Note
import com.syncodec.momento.konstant.Status
import com.syncodec.momento.noteComponent.NoteViewModel
import com.syncodec.momento.noteComponent.miscellaneous.ViewerComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalPagerApi::class, kotlinx.coroutines.InternalCoroutinesApi::class)
@Composable
fun NoteViewerScreen() {
	val scope = rememberCoroutineScope()
	val noteViewModel: NoteViewModel = viewModel()

	val diaryKeyList by noteViewModel.diaryRepository.diaryDbEntryKeyListLiveData.observeAsState()
	var isScrolledToPosition by remember { mutableStateOf(false) }

	val pagerState = rememberPagerState()

	SideEffect {
		if (!isScrolledToPosition) {
			scope.launch {
				val index = diaryKeyList?.indexOf(noteViewModel.viewerDiaryKey)
				if (index != null && index != -1) {
					pagerState.scrollToPage(page = index)
				}
			}
		}
	}

	HorizontalPager(
		state = pagerState,
		count = diaryKeyList?.size ?: 0,
		reverseLayout = true,
		itemSpacing = 2.dp,
		userScrollEnabled = false,
		verticalAlignment = Alignment.Bottom,
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background),
	) { page ->
		Viewer(
			primaryKey = diaryKeyList!![page],
			onPrevious = {
				scope.launch {
					if (page != diaryKeyList!!.size - 1) {
						pagerState.animateScrollToPage(page = page + 1, 0f)
					}
				}
			},
			onNext = {
				scope.launch {
					if (page != 0) {
						pagerState.animateScrollToPage(page = page - 1, 0f)
					}
				}
			}
		)
	}
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Viewer(
	primaryKey: String,
	onPrevious: () -> Unit,
	onNext: () -> Unit
) {
	val noteViewModel: NoteViewModel = viewModel()
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp

	val scope = rememberCoroutineScope()

	val richTextViewer = rememberRichTextEditorWithLifecycle()

	var status by noteViewModel.status
	val isReady by richTextViewer.isReady

	var diary by remember { mutableStateOf<Note?>(null) }

	LaunchedEffect(key1 = isReady) {
		withContext(Dispatchers.IO) {
			scope.launch {
				status = if (isReady) {
					diary = noteViewModel.diaryRepository.loadDiary(primaryKey = primaryKey)
					richTextViewer.exec("editor.commands.setContent(${diary!!.content});")
					Status.LOADED
				} else {
					Status.LOADING
				}
			}
		}
	}

	Crossfade(
		targetState = status,
		animationSpec = tween(
			durationMillis = 400
		)
	) {
		when (it) {
			Status.INIT -> LoadingView()
			Status.LOADING -> LoadingView()
			Status.LOADED -> {
				BottomSheetScaffold(
					sheetContent = {
						Box(
							modifier = Modifier
								.fillMaxWidth()
								.height(144.dp)
								.background(MaterialTheme.colorScheme.secondaryContainer)
						) {

						}
					},
					sheetPeekHeight = 96.dp,
					sheetElevation = 8.dp
				) {
					if (diary != null) {
						diary!!.content?.let { it1 -> ViewerComponent(noteData = it1) }
					}
				}
			}
			Status.SAVING -> {
			}
			Status.SAVED -> {
			}
			Status.SUCCESS -> {
			}
			Status.ERROR -> {
			}
		}
	}
}


//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//private fun Viewer(
//	primaryKey: String,
//	onPrevious: () -> Unit,
//	onNext: () -> Unit
//) {
//	val viewModel: ViewModel = viewModel()
//	val context = LocalContext.current
//	val configuration = LocalConfiguration.current
//	val screenHeight = configuration.screenHeightDp.dp
//
//	val scope = rememberCoroutineScope()
//
//	val richTextViewer = rememberRichTextEditorWithLifecycle()
//
//	var status by viewModel.status
//	val isReady by richTextViewer.isReady
//
//	LaunchedEffect(key1 = isReady) {
//		withContext(Dispatchers.IO) {
//			scope.launch {
//				status = if (isReady) {
//					val diary: Note = viewModel.diaryRepository.loadDiary(primaryKey = primaryKey)
//					richTextViewer.exec("editor.commands.setContent(${diary.content});")
//					Status.LOADED
//				} else {
//					Status.LOADING
//				}
//			}
//		}
//	}
//
//	Crossfade(
//		targetState = status,
//		animationSpec = tween(
//			durationMillis = 400
//		)
//	) {
//		when (it) {
//			Status.INIT -> LoadingView()
//			Status.LOADING -> LoadingView()
//			Status.LOADED -> {
//				BottomSheetScaffold(
//					sheetContent = {
//						Box(
//							modifier = Modifier
//								.fillMaxWidth()
//								.height(144.dp)
//								.background(MaterialTheme.colorScheme.secondaryContainer)
//						) {
//
//						}
//					},
//					sheetPeekHeight = 96.dp,
//					sheetElevation = 8.dp
//				) {
//					AndroidView(
//						factory = { richTextViewer },
//						update = { webView ->
//						},
//						modifier = Modifier
//							.fillMaxSize()
//							.background(MaterialTheme.colorScheme.background)
//					)
//				}
//			}
//			Status.SAVING -> {
//			}
//			Status.SAVED -> {
//			}
//			Status.SUCCESS -> {
//			}
//			Status.ERROR -> {
//			}
//		}
//	}
//}


//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//private fun Viewer(
//	primaryKey: String,
//	onPrevious: () -> Unit,
//	onNext: () -> Unit
//) {
//	val viewModel: ViewModel = viewModel()
//	val context = LocalContext.current
//	val configuration = LocalConfiguration.current
//	val screenHeight = configuration.screenHeightDp.dp
//
//	val scope = rememberCoroutineScope()
//
//	val richTextViewer = rememberRichTextEditorWithLifecycle()
//	val collapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState()
//	val scrollState = rememberScrollState()
//
//
//	var status by viewModel.status
//	val isReady by richTextViewer.isReady
//
//	var offset by remember { mutableStateOf(0f) }
//	var webViewScrollOffset by remember { mutableStateOf(0f) }
//
//	LaunchedEffect(key1 = isReady) {
//		withContext(Dispatchers.IO) {
//			scope.launch {
//				status = if (isReady) {
//					val diary: Note = viewModel.diaryRepository.loadDiary(primaryKey = primaryKey)
//					richTextViewer.exec("editor.commands.setContent(${diary.content});")
//					Status.LOADED
//				} else {
//					Status.LOADING
//				}
//			}
//		}
//	}
//
//	Crossfade(
//		targetState = status,
//		animationSpec = tween(
//			durationMillis = 400
//		)
//	) {
//		when (it) {
//			Status.INIT -> LoadingView()
//			Status.LOADING -> LoadingView()
//			Status.LOADED -> {
//				BottomSheetScaffold(
//					sheetContent = {
//						Box(
//							modifier = Modifier
//								.fillMaxWidth()
//								.height(144.dp)
//								.background(MaterialTheme.colorScheme.secondaryContainer)
//						) {
//
//						}
//					},
//					sheetPeekHeight = 96.dp,
//					sheetElevation = 8.dp
//				) {
//					Box(
//						modifier = Modifier
//							.fillMaxSize()
//					) {
//						AndroidView(
//							factory = { richTextViewer },
//							update = { webView ->
//								webView.setOnTouchListener { p0, p1 -> (p1?.action == MotionEvent.ACTION_MOVE) }
//								webView.scrollY = webViewScrollOffset.toInt()
//							},
//							modifier = Modifier
//								.fillMaxWidth()
//								.background(MaterialTheme.colorScheme.background)
//								.graphicsLayer {
//									this.translationY = max(offset + 194.dp.toPx(), 0.dp.toPx())
//									webViewScrollOffset = max(-256.dp.toPx(), -offset - 256.dp.toPx())
//									Log.i("npr71", "scroll : $webViewScrollOffset : $ ")
//								}
//						)
//
//						Box(
//							modifier = Modifier
//								.fillMaxWidth()
//								.height(194.dp)
//								.padding(16.dp)
//								.graphicsLayer {
//									this.translationY = offset
//								},
//							contentAlignment = Alignment.Center
//						) {
//							Image(
//								painter = painterResource(id = com.syncodec.momento.R.drawable.background),
//								contentDescription = null,
//								contentScale = ContentScale.Crop,
//								modifier = Modifier
//									.fillMaxSize()
//									.clip(RoundedCornerShape(12.dp))
//							)
//						}
//
//						Box(
//							modifier = Modifier
//								.fillMaxSize()
//								.background(Color.Transparent)
//								.scrollable(
//									orientation = Orientation.Vertical,
//									state = rememberScrollableState { delta ->
//										offset += delta
//										offset = min(0f, offset)
//										0f
//									}
//								)
//						)
//					}
//				}
//			}
//			Status.SAVING -> {
//			}
//			Status.SAVED -> {
//			}
//			Status.SUCCESS -> {
//			}
//			Status.ERROR -> {
//			}
//		}
//	}
//}


//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//private fun Viewer(
//	primaryKey: String,
//	onPrevious: () -> Unit,
//	onNext: () -> Unit
//) {
//	val viewModel: ViewModel = viewModel()
//	val context = LocalContext.current
//	val configuration = LocalConfiguration.current
//	val screenHeight = configuration.screenHeightDp.dp
//
//	val scope = rememberCoroutineScope()
//
//	val richTextViewer = rememberRichTextEditorWithLifecycle()
//	val collapsingToolbarScaffoldState = rememberCollapsingToolbarScaffoldState()
//	val scrollState = rememberScrollState()
//
//
//	var status by viewModel.status
//	val isReady by richTextViewer.isReady
//
//	var offset by remember { mutableStateOf(0f) }
//	var webViewScrollOffset by remember { mutableStateOf(0f) }
//
//	LaunchedEffect(key1 = isReady) {
//		withContext(Dispatchers.IO) {
//			scope.launch {
//				status = if (isReady) {
//					val diary: Note = viewModel.diaryRepository.loadDiary(primaryKey = primaryKey)
//					richTextViewer.exec("editor.commands.setContent(${diary.content});")
//					Status.LOADED
//				} else {
//					Status.LOADING
//				}
//			}
//		}
//	}
//
//	Crossfade(
//		targetState = status,
//		animationSpec = tween(
//			durationMillis = 400
//		)
//	) {
//		when (it) {
//			Status.INIT -> LoadingView()
//			Status.LOADING -> LoadingView()
//			Status.LOADED -> {
//				BottomSheetScaffold(
//					sheetContent = {
//						Box(
//							modifier = Modifier
//								.fillMaxWidth()
//								.height(144.dp)
//								.background(MaterialTheme.colorScheme.secondaryContainer)
//						) {
//
//						}
//					},
//					sheetPeekHeight = 96.dp,
//					sheetElevation = 8.dp
//				) {
//					Box(
//						modifier = Modifier
//							.fillMaxSize()
//					) {
//						Box(
//							modifier = Modifier
//								.fillMaxWidth()
//								.height(256.dp)
//								.padding(16.dp),
//							contentAlignment = Alignment.Center
//						) {
//							Image(
//								painter = painterResource(id = com.syncodec.momento.R.drawable.background),
//								contentDescription = null,
//								modifier = Modifier
//									.fillMaxSize()
//									.clip(RoundedCornerShape(12.dp))
//							)
//						}
//
//						AndroidView(
//							factory = { richTextViewer },
//							update = { webView ->
//								webView.setOnTouchListener { p0, p1 -> (p1?.action == MotionEvent.ACTION_MOVE) }
//								webView.scrollY = webViewScrollOffset.toInt()
//
//							},
//							modifier = Modifier
//								.fillMaxWidth()
//								.background(MaterialTheme.colorScheme.background)
//								.graphicsLayer {
//									this.translationY = max(offset + 256.dp.toPx(), -108.dp.toPx())
//									webViewScrollOffset = max(-256.dp.toPx(), -offset - 256.dp.toPx())
//									Log.i("npr71", "scroll : $webViewScrollOffset : $ ")
//								}
//						)
//
//						Box(
//							modifier = Modifier
//								.fillMaxSize()
//								.background(Color.Transparent)
//								.scrollable(
//									orientation = Orientation.Vertical,
//									state = rememberScrollableState { delta ->
//										offset += delta
//										offset = min(0f, offset)
//										0f
//									}
//								)
//						)
//					}
//				}
//			}
//			Status.SAVING -> {
//			}
//			Status.SAVED -> {
//			}
//			Status.SUCCESS -> {
//			}
//			Status.ERROR -> {
//			}
//		}
//	}
//}


//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//private fun Viewer(
//	primaryKey: String,
//	onPrevious: () -> Unit,
//	onNext: () -> Unit
//) {
//	val viewModel: ViewModel = viewModel()
//	val richTextViewer = rememberRichTextEditorWithLifecycle()
//
//	val diary: Note = viewModel.diaryRepository.loadDiary(primaryKey = primaryKey)
//
//	var status by viewModel.status
//	val isReady by richTextViewer.isReady
//
//	LaunchedEffect(key1 = isReady) {
//		status = if (isReady) {
//			richTextViewer.exec("editor.commands.setContent(${diary.content});")
//			Status.LOADED
//		} else {
//			Status.LOADING
//		}
//	}
//
//	Crossfade(
//		targetState = status,
//		animationSpec = tween(
//			durationMillis = 400
//		)
//	) {
//		when (it) {
//			Status.INIT -> LoadingView()
//			Status.LOADING -> LoadingView()
//			Status.LOADED -> {
//				BottomSheetScaffold(
//					sheetContent = {
//						Box(
//							modifier = Modifier
//								.fillMaxWidth()
//								.wrapContentHeight()
//								.background(MaterialTheme.colorScheme.secondaryContainer)
//						) {
//							AndroidView(
//								factory = { richTextViewer },
//								update = { viewer ->
//								},
//								modifier = Modifier
//									.fillMaxWidth()
//									.wrapContentHeight()
//							)
//
//						}
//					},
//					sheetPeekHeight = 96.dp,
//					sheetElevation = 8.dp
//				) {
//					Column(
//						modifier = Modifier.fillMaxSize()
//					) {
//						Row(
//							modifier = Modifier
//								.fillMaxWidth()
//								.height(48.dp)
//								.padding(8.dp, 0.dp),
//							horizontalArrangement = Arrangement.SpaceBetween,
//							verticalAlignment = Alignment.CenterVertically
//						) {
//							IconButton(
//								onClick = { onPrevious() },
//							) {
//								Icon(
//									imageVector = TablerIcons.ChevronLeft,
//									contentDescription = "Previous diary",
//									tint = MaterialTheme.colorScheme.onBackground,
//									modifier = Modifier
//										.requiredSize(24.dp)
//								)
//							}
//
//							IconButton(
//								onClick = { onNext() },
//							) {
//								Icon(
//									imageVector = TablerIcons.ChevronRight,
//									contentDescription = "Next diary",
//									tint = MaterialTheme.colorScheme.onBackground,
//									modifier = Modifier
//										.requiredSize(24.dp)
//								)
//							}
//						}
//						Spacer(modifier = Modifier.height(96.dp))
//					}
//				}
//			}
//			Status.SAVING -> {
//			}
//			Status.SAVED -> {
//			}
//			Status.SUCCESS -> {
//			}
//			Status.ERROR -> {
//			}
//		}
//	}
//}
