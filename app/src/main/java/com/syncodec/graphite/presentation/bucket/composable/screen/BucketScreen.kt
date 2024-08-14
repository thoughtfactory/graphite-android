package com.syncodec.graphite.presentation.bucket.composable.screen

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.BucketViewModel
import com.syncodec.graphite.presentation.bucket.composable.bar.BottomBar
import com.syncodec.graphite.presentation.bucket.composable.bar.TopBar
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.SheetLayout
import com.syncodec.graphite.presentation.bucket.composable.dialog.BucketDialog
import com.syncodec.graphite.presentation.bucket.composable.dialog.BucketDialogType
import com.syncodec.graphite.presentation.bucket.composable.screen.bookScreen.BucketBookScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.linkScreen.BucketLinkScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.showScreen.BucketShowScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.todoScreen.BucketTodoScreen
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericButton
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated
import com.syncodec.graphite.utils.enumValueOf
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel


@OptIn(
	ExperimentalMaterialApi::class,
	ExperimentalFoundationApi::class
)
@Composable
fun BucketScreen(
	afterDeleteBucket : () -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	val viewModel : BucketViewModel = koinViewModel()

	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	val pagerState = rememberPagerState(initialPage = 0){ 4 }
	var currentPage = pagerState.currentPage
	LaunchedEffect(key1 = pagerState.currentPage) {
		currentPage = pagerState.currentPage
	}

	val modalBottomSheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
	var sheetType by remember { mutableStateOf(BucketBottomSheetType.MENU) }

	fun openSheet(type : BucketBottomSheetType) {
		scope.launch { sheetType = type; modalBottomSheetState.show() }
	}

	fun closeSheet() {
		scope.launch { modalBottomSheetState.hide() }
	}

	var isSelecting by remember { mutableStateOf(false) }
	var selectedIdList : List<RealmUUID> by remember { mutableStateOf(listOf()) }
	fun onSelect(id : RealmUUID?) {
		if (! isSelecting) isSelecting = true
		selectedIdList.toMutableList().apply {
			if (id in selectedIdList) remove(id) else id?.let { add(it) }
			selectedIdList = this
		}
	}

	var showEditBucketDialog by remember { mutableStateOf(false) }
	var showDeleteBucketItemsDialog by remember { mutableStateOf(false) }
	var showDeleteBucketDialog by remember { mutableStateOf(false) }
	fun openDialog(dialogType : BucketDialogType) = when (dialogType) {
		BucketDialogType.Edit -> showEditBucketDialog = true
		BucketDialogType.DeleteBucketItems -> showDeleteBucketItemsDialog = true
		BucketDialogType.DeleteBucket -> showDeleteBucketDialog = true
	}

	fun closeDialog(dialogType : BucketDialogType) = when (dialogType) {
		BucketDialogType.Edit -> showEditBucketDialog = false
		BucketDialogType.DeleteBucketItems -> showDeleteBucketItemsDialog = false
		BucketDialogType.DeleteBucket -> showDeleteBucketDialog = false
	}

	val bucketObject by viewModel.bucketObject.collectAsState()
	val title by viewModel.title.collectAsState()
	val bucketType by viewModel.bucketType.collectAsState()
	var previewBucketItemObjectId by remember { mutableStateOf(null as RealmUUID?) }

	BackHandler(enabled = isSelecting) { isSelecting = false; selectedIdList = listOf() }

	fun share(shareAll : Boolean = false) {
		viewModel.shareBucketItems(shareAll = shareAll, realmUUIDList = selectedIdList.toList()) {
			withContext(Dispatchers.Main) {
				Intent(Intent.ACTION_SEND).apply {
					type = "text/html"
					putExtra(Intent.EXTRA_SUBJECT, title ?: bucketType)
					putExtra(Intent.EXTRA_TEXT, it)

					if (resolveActivity(context.packageManager) != null) context.startActivity(Intent.createChooser(this, "Share using"))
					else Toast.makeText(context, "No app found on your device which can perform this action", Toast.LENGTH_SHORT).show()
				}
			}
		}
		isSelecting = false
		selectedIdList = listOf()
	}

	bucketObject?.let { bucketObject1 ->
		GenericScaffold(
			topBar = {
				TopBar(
					title = bucketObject1.title,
					isLocked = bucketObject1.isLocked,
					isFavourite = bucketObject1.isFavourite,
					bucketType = enumValueOf(bucketObject1.bucketType, BucketType.UNKNOWN),
					viewState = currentPage,
					isSelecting = isSelecting,
					selectedItemSize = selectedIdList.size,
					onCancelSelection = { isSelecting = false; selectedIdList = listOf() },
					onClickFavourite = viewModel::toggleFavourite,
					onClickLock = {
						if (isAuthenticated) viewModel.toggleLock()
						else onAuthenticationAction(AuthenticatorScreen.Authenticate)
					},
					onStateChange = { scope.launch { pagerState.animateScrollToPage(it) } },
					onShare = { share(false) },
					onDelete = {
						closeSheet()
						openDialog(BucketDialogType.DeleteBucketItems)
					},
				)
			},
			bottomBar = { BottomBar(onClickMenu = { openSheet(BucketBottomSheetType.MENU) }) },
			isBottomBarVisible = ! isSelecting,
			modalBottomSheetState = modalBottomSheetState,
			sheetContent = {
				SheetLayout(
					sheetType = sheetType,
					previewBucketItemObjectId = previewBucketItemObjectId,
					id = bucketObject1.id,
					description = bucketObject1.description,
					createdTimestamp = bucketObject1.createdTimestamp,
					modifiedTimestamp = bucketObject1.modifiedTimestamp,
					onClickEdit = { openDialog(BucketDialogType.Edit) },
					onClickShare = {
						closeSheet()
						share(true)
					},
					onClickDelete = {
						closeSheet()
						openDialog(BucketDialogType.DeleteBucket)
					},
					closeSheet = { closeSheet() },
				)
			},
			dialogContent = {
				BucketDialog(
					bucketObject = bucketObject1,
					showEditBucketDialog = showEditBucketDialog,
					showDeleteBucketItemsDialog = showDeleteBucketItemsDialog,
					showDeleteBucketDialog = showDeleteBucketDialog,
					onUpdateBucket = viewModel::updateBucket,
					onDelete = {
						viewModel.deleteBucketItem(selectedIdList.toList())
						isSelecting = false
						selectedIdList = listOf()
					},
					onDeleteBucket = {
						viewModel.deleteBucket(bucketObject1.id) {
							closeDialog(BucketDialogType.DeleteBucket)
							withContext(Dispatchers.Main) { Toast.makeText(context, "Bucket Deleted", Toast.LENGTH_SHORT).show() }
							afterDeleteBucket()
						}
					},
					closeDialog = ::closeDialog,
				)
			},
			primaryButton = GenericButton(
				text = when (bucketObject1.bucketType) {
					BucketType.TODO.name -> "Add Todo"
					BucketType.BOOK.name -> "Add Book"
					BucketType.SHOW.name -> "Add Show"
					BucketType.LINK.name -> "Add Link"
					BucketType.UNKNOWN.name -> "ERROR"
					else -> "ERROR"
				},
				icon = when (bucketObject1.bucketType) {
					BucketType.TODO.name -> R.drawable.ic_todo
					BucketType.BOOK.name -> R.drawable.ic_book_shelf
					BucketType.SHOW.name -> R.drawable.ic_show
					BucketType.LINK.name -> R.drawable.ic_link
					BucketType.UNKNOWN.name -> R.drawable.ic_warning
					else -> R.drawable.ic_warning
				},
				onClick = {
					when (bucketObject1.bucketType) {
						BucketType.TODO.name -> BucketBottomSheetType.AddTodo
						BucketType.BOOK.name -> BucketBottomSheetType.AddBook
						BucketType.SHOW.name -> BucketBottomSheetType.AddShow
						BucketType.LINK.name -> BucketBottomSheetType.AddLink
						else -> BucketBottomSheetType.MENU
					}.let {
//						if (it == BucketBottomSheetType.ADD_TODO) setBucketItemObject(null)
						openSheet(it)
					}
				}
			),
			isButtonVisible = ! isSelecting
		) {
			when (bucketObject1.bucketType) {
				BucketType.TODO.name -> BucketTodoScreen(
					pagerState = pagerState,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = ::onSelect,
					onClickBucketItem = {
						openSheet(BucketBottomSheetType.PreviewTodo)
						previewBucketItemObjectId = it
					},
				)

				BucketType.BOOK.name -> BucketBookScreen(
					pagerState = pagerState,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = ::onSelect,
				)

				BucketType.SHOW.name -> BucketShowScreen(
					pagerState = pagerState,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = ::onSelect,
				)

				BucketType.LINK.name -> BucketLinkScreen(
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					onSelect = ::onSelect,
					onClickBucketItem = {
						openSheet(BucketBottomSheetType.PreviewLink)
						previewBucketItemObjectId = it
					},
				)

				BucketType.UNKNOWN.name -> null
				else -> null
			}
		}
	} ?: LoadingView()
}
