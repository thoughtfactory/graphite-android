package com.syncodec.graphite.presentation.bucket.composable.screen.todoScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.BucketItemState
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.AddTodoBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.EditTodoBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreenCommonViewModel
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalIsAuthenticated
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BucketTodoScreen(
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
	val scope = rememberCoroutineScope()

	val isAuthenticated = LocalIsAuthenticated.current
	val onAuthenticationAction = LocalAuthenticatorAction.current

	val bucketItemList by viewModel.orderedBucketItemList.collectAsState()
	val previewBucketItemObject by viewModel.previewBucketItemObject.collectAsState()

	val bottomSheetState = rememberModalBottomSheetState()
	var isAddTodoSheetVisible by rememberSaveable { mutableStateOf(false) }
	var isEditTodoSheetVisible by remember { mutableStateOf(false) }

	GenericScaffold2(
		floatingActionButton = {
			AnimatedVisibility(
				visible = !isSelecting,
				enter = scaleIn(tween(470)),
				exit = scaleOut(tween(470)),
				label = "addTodoFab_animation"
			) {
				ExtendedFloatingActionButton(
					text = { Text(text = "Add Todo") },
					icon = {
						Icon(
							painter = painterResource(id = R.drawable.ic_fa_plus),
							contentDescription = "Add todo",
							modifier = Modifier.requiredSize(16.dp)
						)
					},
					onClick = { isAddTodoSheetVisible = true }
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
			BucketTodoListScreen(
				bucketItemList = filteredBucketItemList,
				isSelecting = isSelecting,
				selectedIdList = selectedIdList,
				onSelect = { onSelect(it, filteredBucketItemList.map { it.id }.toSet()) },
				onClickBucketItem = {
					viewModel.selectBucketItemObject(it.id)
					isEditTodoSheetVisible = true
				},
				onClickFavourite = viewModel::toggleFavourite,
				onClickLock = { if (isAuthenticated) viewModel.toggleLock(it) else onAuthenticationAction(AuthenticatorScreen.Authenticate) },
				onCheckedChange = viewModel::toggleBucketItemState,
				onReorderBucketItemList = viewModel::onReorderBucketItem
			)
		}
	}

	AddTodoBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isAddTodoSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isAddTodoSheetVisible = false } },
		onAddTodo = { todoTitle, state ->
			viewModel.putTodo(realmUUID = null, title = todoTitle, state = BucketItemState.values().getOrElse(state) { BucketItemState.ALPHA })
		}
	)

	EditTodoBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isEditTodoSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isEditTodoSheetVisible = false } },
		bucketItemObject = previewBucketItemObject,
		onUpdateTitle = { bucketItemObject, title -> viewModel.updateBucketItemTitle(bucketItemObject, title) },
		onUpdateState = { bucketItemObject, state -> viewModel.updateBucketItemState(bucketItemObject, state) },
		onToggleFavourite = { viewModel.toggleFavourite(it) },
		onToggleLock = { viewModel.toggleLock(it) },
	)
}
