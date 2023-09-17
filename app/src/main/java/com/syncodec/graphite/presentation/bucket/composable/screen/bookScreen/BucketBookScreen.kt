package com.syncodec.graphite.presentation.bucket.composable.screen.bookScreen

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.AddBookBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreenCommonViewModel
import com.syncodec.graphite.presentation.bucketItem.activity.BookBucketItemActivity
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.base.LocalAppDataStore
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.presentation.base.secureComposable.LocalIsRepoUnlocked
import com.syncodec.graphite.utils.ViewType
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@Preview
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BucketBookScreen(
	viewModel: BucketScreenCommonViewModel = koinViewModel(),
	pagerState: PagerState = rememberPagerState(
		initialPage = 0,
		initialPageOffsetFraction = 0f,
		pageCount = { 0 }
	),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID, Set<RealmUUID>) -> Unit = { _, _ -> },
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val isAuthenticated = LocalIsRepoUnlocked.current

	val appDataStore = LocalAppDataStore.current
	val viewType by appDataStore.getViewType.collectAsState(initial = null)

	val bucketId by viewModel.id.collectAsState()
	val bucketItemList by viewModel.orderedBucketItemList.collectAsState()

	val bottomSheetState = rememberModalBottomSheetState()
	var isAddBookBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	GenericScaffold2(
		floatingActionButton = {
			AnimatedVisibility(
				visible = !isSelecting,
				enter = scaleIn(tween(470)),
				exit = scaleOut(tween(470)),
				label = "addTodoFab_animation"
			) {
				ExtendedFloatingActionButton(
					text = { Text(text = stringResource(id = R.string.add_book)) },
					icon = {
						Icon(
							painter = painterResource(id = R.drawable.ic_fa_plus),
							contentDescription = stringResource(id = R.string.add_book),
							modifier = Modifier.requiredSize(16.dp)
						)
					},
					onClick = { isAddBookBottomSheetVisible = true }
				)
			}
		}
	) {
		HorizontalPager(
			state = pagerState,
			userScrollEnabled = !isSelecting,
		) { page ->
			val filteredBucketItemList = when (page) {
				0 -> bucketItemList
				1 -> bucketItemList.filter { it.state == BucketItemState.ALPHA.name }
				2 -> bucketItemList.filter { it.state == BucketItemState.BETA.name }
				3 -> bucketItemList.filter { it.state == BucketItemState.GAMMA.name }
				else -> bucketItemList
			}.filter { !it.isLocked || isAuthenticated }
			AnimatedContent(
				targetState = viewType,
				transitionSpec = { fadeIn(tween(470)) + scaleIn(tween(470), 0.71f) togetherWith fadeOut(tween(470)) + scaleOut(tween(470), 0.71f) },
				label = "viewType_animation"
			) {
				when (it) {
					ViewType.List -> BucketBookListScreen(
						bucketId = bucketId,
						bucketItemList = filteredBucketItemList,
						isSelecting = isSelecting,
						selectedIdList = selectedIdList,
						onClickBucketItem = {
							Intent(context, BookBucketItemActivity::class.java).apply {
								putExtra(Extra.Companion.Extra.IsNew.name, false)
								putExtra(Extra.Companion.Extra.BUCKET_ID.name, bucketId?.bytes)
								putExtra(Extra.Companion.Extra.BUCKET_TYPE.name, BucketType.BOOK.name)
								putExtra(Extra.Companion.Extra.BUCKET_ITEM_ID.name, it.bytes)

								context.startActivity(this)
							}
						},
						onSelect = { onSelect(it, filteredBucketItemList.map { it.id }.toSet()) },
					)

					ViewType.Grid -> BucketBookGridScreen(
						bucketId = bucketId,
						bucketItemList = filteredBucketItemList,
						isSelecting = isSelecting,
						selectedIdList = selectedIdList,
						onSelect = { onSelect(it, filteredBucketItemList.map { it.id }.toSet()) },
					)

					null -> LoadingView()
				}
			}
		}
	}

	AddBookBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isAddBookBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isAddBookBottomSheetVisible = false } },
		parentId = bucketId,
	)
}
