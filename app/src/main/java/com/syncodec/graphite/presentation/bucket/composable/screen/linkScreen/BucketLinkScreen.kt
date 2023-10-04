package com.syncodec.graphite.presentation.bucket.composable.screen.linkScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemObject
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.AddLinkBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.PreviewLinkBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreenCommonViewModel
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.base.LocalAppDataStore
import com.syncodec.graphite.presentation.base.secureComposable.LocalIsRepoUnlocked
import com.syncodec.graphite.utils.ViewType
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BucketLinkScreen(
	viewModel: BucketScreenCommonViewModel = koinViewModel(),
	isSelecting: Boolean = false,
	selectedIdList: Set<RealmUUID> = setOf(),
	onSelect: (RealmUUID, Set<RealmUUID>) -> Unit = { _, _ -> },
) {
	val scope = rememberCoroutineScope()

	val isAuthenticated = LocalIsRepoUnlocked.current

	val appDataStore = LocalAppDataStore.current
	val viewType by appDataStore.getViewType.collectAsState(initial = null)

	val bucketItemList= listOf<BucketItemObject>()
//	val bucketItemList by viewModel.filteredBucketItemList.collectAsState()
	var previewBucketItemObject by remember { mutableStateOf<BucketItemObject?>(null) }

	val bottomSheetState = rememberModalBottomSheetState()
	var isAddLinkBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isPreviewLinkBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

	GenericScaffold2(
		floatingActionButton = {
			AnimatedVisibility(
				visible = !isSelecting,
				enter = scaleIn(tween(470)),
				exit = scaleOut(tween(470)),
				label = "addTodoFab_animation"
			) {
				ExtendedFloatingActionButton(
					text = { Text(text = stringResource(id = R.string.add_link)) },
					icon = {
						Icon(
							painter = painterResource(id = R.drawable.ic_fa_plus),
							contentDescription = stringResource(id = R.string.add_link),
							modifier = Modifier.requiredSize(16.dp)
						)
					},
					onClick = { isAddLinkBottomSheetVisible = true }
				)
			}
		}
	) {
		bucketItemList.filter { !it.isLocked || isAuthenticated }.let { filteredBucketItemList ->
			AnimatedContent(
				targetState = viewType,
				transitionSpec = { fadeIn(tween(470)) + scaleIn(tween(470), 0.71f) togetherWith fadeOut(tween(470)) + scaleOut(tween(470), 0.71f) },
				label = "viewType_animation"
			) {
				when (it) {
					ViewType.List -> BucketLinkListScreen(
						bucketItemList = filteredBucketItemList,
						isSelecting = isSelecting,
						selectedIdList = selectedIdList,
						onSelect = { onSelect(it, filteredBucketItemList.map { it.id }.toSet()) },
						onClickBucketItem = {
							viewModel.selectBucketItemObject(it)
							isPreviewLinkBottomSheetVisible = true
						},
						onReorderBucketItemList = viewModel::onReorderBucketItem,
					)

					ViewType.Grid -> BucketLinkGridScreen(
						bucketItemList = filteredBucketItemList,
						isSelecting = isSelecting,
						selectedIdList = selectedIdList,
						onSelect = { onSelect(it, filteredBucketItemList.map { it.id }.toSet()) },
						onClickBucketItem = {
							viewModel.selectBucketItemObject(it)
							isPreviewLinkBottomSheetVisible = true
						},
						onReorderBucketItemList = viewModel::onReorderBucketItem,
					)
					null -> LoadingView()
				}
			}
		}
	}

	AddLinkBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isAddLinkBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isAddLinkBottomSheetVisible = false } }
	) { url, state ->
		viewModel.putLink(url = url, state = BucketItemState.values().getOrElse(state) { BucketItemState.ALPHA })
	}

	PreviewLinkBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isPreviewLinkBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isPreviewLinkBottomSheetVisible = false } },
		bucketItemObject = previewBucketItemObject,
		onToggleFavourite = { viewModel.toggleFavourite(it.id) },
		onToggleLock = { viewModel.toggleLock(it.id) },
	)
}
