package com.syncodec.graphite.presentation.bucket

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreenCommonViewModel
import com.syncodec.graphite.presentation.bucket.composable.screen.bookScreen.BucketBookScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.linkScreen.BucketLinkScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.showScreen.BucketShowScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.todoScreen.BucketTodoScreen
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.xor
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class BucketActivity : ComponentActivity() {

	private val viewModel by viewModel<BucketScreenCommonViewModel>()

	@OptIn(ExperimentalFoundationApi::class)
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val hasBucketId = intent.hasExtra(Extra.Companion.Extra.BUCKET_ID.name)
		if (hasBucketId) {
			val bucketId = intent.getByteArrayExtra(Extra.Companion.Extra.BUCKET_ID.name)?.let { RealmUUID.from(it) }
			bucketId?.let { realmUUID ->
				viewModel.initBucket(realmUUID)
			} ?: run {
				Toast.makeText(this, "Error loading bucket. No id specified.", Toast.LENGTH_SHORT).show()
				finish()
			}
		} else {
			Toast.makeText(this, "Error loading bucket. No id specified.", Toast.LENGTH_SHORT).show()
			finish()
		}

		setContent {
			BaseComposable {

				val bucketObject by viewModel.bucketObject.collectAsState()
				val bucketItemListMap by viewModel.filteredBucketItemListMap.collectAsState()
				val previewBucketItemObject by viewModel.previewBucketItemObject.collectAsState()
				val searchQueryList by viewModel.filterQueryList.collectAsState()

				val pagerState = rememberPagerState(
					initialPage = 0,
					initialPageOffsetFraction = 0f,
					pageCount = { 4 }
				)

				var isSelecting by remember { mutableStateOf(false) }
				var selectedIdList: Set<RealmUUID> by remember { mutableStateOf(setOf()) }
				var toSelectIdList: Set<RealmUUID> by remember { mutableStateOf(setOf()) }
				fun onSelect(id: RealmUUID, idList: Set<RealmUUID>) {
					if (!isSelecting) isSelecting = true
					selectedIdList.toMutableSet().apply {
						xor(id)
						selectedIdList = toSet()
					}
					toSelectIdList = idList
				}

				BackHandler(enabled = isSelecting) { isSelecting = false; selectedIdList = setOf() }

				BucketScreen(
					pagerState = pagerState,
					bucketObject = bucketObject,
					bucketItemListMap = bucketItemListMap,
					searchQueryList = searchQueryList,
					isSelecting = isSelecting,
					selectedIdList = selectedIdList,
					toggleFavourite = viewModel::toggleFavourite,
					toggleLock = viewModel::toggleLock,
					filterBySearchAdd = viewModel::filterBySearchAdd,
					filterBySearchRemove = viewModel::filterBySearchRemove,
					updateTitleDescription = viewModel::updateTitleDescription,
					moveBucketItem = viewModel::moveBucketItem,
					toggleMultiFavorite = viewModel::toggleMultiFavourite,
					toggleMultiLock = viewModel::toggleMultiLock,
					updateMultiState = viewModel::updateBucketItemState,
					deleteMulti = viewModel::delete,
					clearSearchFilter = viewModel::clearSearchFilter,
					onSelectAll = { selectedIdList.toMutableSet().apply { addAll(toSelectIdList); selectedIdList = toSet() } },
					onUnselectAll = { selectedIdList = setOf() },
					shareBucketItems = { viewModel.shareBucketItems(idList = it, callback = ::share) }
				) {
					when (bucketObject?.bucketType) {
						BucketType.TODO.name -> BucketTodoScreen(
							pagerState = pagerState,
							bucketItemListMap = bucketItemListMap,
							previewBucketItemObject = previewBucketItemObject,
							isSelecting = isSelecting,
							selectedIdList = selectedIdList,
							onSelect = ::onSelect,
							selectBucketItemObjectForPreview = viewModel::selectBucketItemObject,
							onUpdateTitle = { bucketItemObject, title -> viewModel.updateBucketItemTitle(bucketItemObject.id, title) },
							onUpdateState = { bucketItemObject, state -> viewModel.updateBucketItemState(bucketItemObject.id, state) },
							toggleFavourite = viewModel::toggleFavourite,
							toggleLock = viewModel::toggleLock,
							toggleBucketItemState = viewModel::toggleBucketItemState,
							onReorderBucketItemList = viewModel::onReorderBucketItem,
							updateBucketItemTitle = viewModel::updateBucketItemTitle,
							updateBucketItemState = { bucketItemObject, state -> viewModel.updateBucketItemState(bucketItemObject, state) },
							putTodo = viewModel::putTodo,
							addTodoDebugData = viewModel::addTodoDebugData
						)

						BucketType.BOOK.name -> BucketBookScreen(
							pagerState = pagerState,
							isSelecting = isSelecting,
							selectedIdList = selectedIdList,
							onSelect = ::onSelect
						)

						BucketType.SHOW.name -> BucketShowScreen(
							pagerState = pagerState,
							isSelecting = isSelecting,
							selectedIdList = selectedIdList,
							onSelect = ::onSelect
						)

						BucketType.LINK.name -> BucketLinkScreen(
//					        viewModel = viewModel,
							isSelecting = isSelecting,
							selectedIdList = selectedIdList,
							onSelect = ::onSelect
						)

						else -> Unit
					}
				}
			}
		}
	}

	suspend fun share(shareText: String) {
		withContext(Dispatchers.Main) {
			Intent(Intent.ACTION_SEND).apply {
				type = "text/html"
				putExtra(Intent.EXTRA_SUBJECT, "My book list")
				putExtra(Intent.EXTRA_TEXT, shareText)
				addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

				if (resolveActivity(packageManager) != null) startActivity(Intent.createChooser(this, "Share using"))
				else Toast.makeText(this@BucketActivity, "No app found on your device which can perform this action", Toast.LENGTH_SHORT).show()
			}
		}
	}


	private fun onShare(bucketItemObjectList: List<BucketItemObject>, shareAll: Boolean) {

//		val baseUrl = when (bucketObject.bucketType) {
//			BucketType.TODO.name -> ""
//			BucketType.BOOK.name -> " - https://openlibrary.org"
//			BucketType.SHOW.name -> " - https://www.themoviedb.org/"
//			BucketType.LINK.name -> ""
//			BucketType.UNKNOWN.name -> ""
//			else -> ""
//		}
//
//		var shareText = ""
//		bucketItemObjectList.filter { if (shareAll) true else it.id in selectedRealmUUIDList }.forEach {
//			val connector = when (it.getShowData()?.type) {
//				ShowType.TV -> "tv/"
//				ShowType.MOVIE -> "movie/"
//				else -> ""
//			}
//			shareText += "${it.title}$baseUrl$connector${if (bucketObject.bucketType == BucketType.TODO.name) "" else it.key}\n"
//		}
//
//		Intent(Intent.ACTION_SEND).apply {
//			type = "text/html"
//			putExtra(Intent.EXTRA_SUBJECT, bucketObject.title ?: bucketObject.bucketType)
////			putExtra(Intent.EXTRA_TEXT, Html.fromHtml(shareText, Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST))
//			putExtra(Intent.EXTRA_TEXT, shareText)
//
//			if (resolveActivity(this@BucketActivity.packageManager) != null) startActivity(Intent.createChooser(this, "Share using"))
//			else Toast.makeText(this@BucketActivity, "No app found on your device which can perform this action", Toast.LENGTH_SHORT).show()
//		}
	}
}
