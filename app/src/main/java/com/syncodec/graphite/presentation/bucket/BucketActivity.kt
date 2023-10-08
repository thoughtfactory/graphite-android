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
				val bucketItemList by viewModel.filteredBucketItemList.collectAsState()
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
					bucketItemList = bucketItemList,
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
					shareBucketItems = { viewModel.shareBucketItems(idList = it, callback = ::share) },
					shareBucket = { viewModel.shareBucketItems(idList = bucketItemList.map { it.id }.toSet(), callback = ::share) },
					deleteBucket = { bucketObject?.id?.let { viewModel.delete(setOf(it)); finish() } },
				) {
					when (bucketObject?.bucketType) {
						BucketType.TODO.name -> BucketTodoScreen(
							pagerState = pagerState,
							bucketItemList = bucketItemList,
							previewBucketItemObject = previewBucketItemObject,
							isSelecting = isSelecting,
							selectedIdList = selectedIdList,
							onSelect = ::onSelect,
							selectBucketItemObjectForPreview = viewModel::selectBucketItemObject,
							onUpdateTitle = { bucketItemObject, title -> viewModel.updateBucketItemTitle(bucketItemObject.id, title) },
							toggleFavourite = { viewModel.toggleMultiFavourite(idList = setOf(it)) },
							toggleLock = { viewModel.toggleMultiLock(idList = setOf(it)) },
							toggleBucketItemState = viewModel::toggleBucketItemState,
							onReorderBucketItemList = viewModel::onReorderBucketItem,
							updateBucketItemTitle = viewModel::updateBucketItemTitle,
							updateBucketItemState = { bucketItemObject, state -> viewModel.updateBucketItemState(bucketItemObject, state) },
							putTodo = viewModel::putTodo,
							addTodoDebugData = viewModel::addTodoDebugData,
						)

						BucketType.BOOK.name -> BucketBookScreen(
							pagerState = pagerState,
							bucketId = bucketObject?.id,
							bucketItemList = bucketItemList,
							isSelecting = isSelecting,
							selectedIdList = selectedIdList,
							onSelect = ::onSelect,
							onReorderBucketItemList = viewModel::onReorderBucketItem,
						)

						BucketType.SHOW.name -> BucketShowScreen(
							pagerState = pagerState,
							bucketId = bucketObject?.id,
							bucketItemList = bucketItemList,
							isSelecting = isSelecting,
							selectedIdList = selectedIdList,
							onSelect = ::onSelect,
							onReorderBucketItemList = viewModel::onReorderBucketItem,
						)

						BucketType.LINK.name -> BucketLinkScreen(
							bucketItemList = bucketItemList,
							previewBucketItemObject = previewBucketItemObject,
							isSelecting = isSelecting,
							selectedIdList = selectedIdList,
							onSelect = ::onSelect,
							selectBucketItemObjectForPreview = viewModel::selectBucketItemObject,
							toggleFavourite = { viewModel.toggleMultiFavourite(idList = setOf(it)) },
							toggleLock = { viewModel.toggleMultiLock(idList = setOf(it)) },
							onReorderBucketItemList = viewModel::onReorderBucketItem,
							putLink = viewModel::putLink,
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
}
