package com.syncodec.graphite.presentation.common.dialog.where.whereBucketDialog

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.component.bucket.BucketCard
import com.syncodec.graphite.presentation.common.dialog.where.whereBucketDialog.bar.BottomBar
import com.syncodec.graphite.presentation.common.dialog.where.whereBucketDialog.bar.TopBar
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.main.composable.bottomSheet.BucketBottomSheet
import com.syncodec.graphite.presentation.main.composable.buildingBlock.EmptyView
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun WhereBucketDialog2(
	isDialogVisible: Boolean = true,
	onDismissRequest: () -> Unit = {},
	currentSelectedBucket: RealmUUID? = null,
	currentBucketType: String? = null,
	onSelectBucket: (RealmUUID) -> Unit = {}
) {
	val context = LocalContext.current

	val scope = rememberCoroutineScope()
	val viewModel: WhereBucketDialogViewModel2 = koinViewModel()

	val bucketList by viewModel.bucketList.collectAsState()

	val gridState = rememberLazyGridState()

	val bottomSheetState = rememberModalBottomSheetState()
	var isBucketBottomSheetVisible by remember { mutableStateOf(false) }

	var selectedBucketObjectId by remember { mutableStateOf<RealmUUID?>(null) }

	LaunchedEffect(key1 = isDialogVisible) {
		selectedBucketObjectId = null
	}

	BackHandler(enabled = isDialogVisible) { onDismissRequest() }
	BackHandler(enabled = selectedBucketObjectId != null) { selectedBucketObjectId = null }

	AnimatedVisibility(
		visible = isDialogVisible,
		enter = slideInVertically(tween(470)) { it / 2 } + fadeIn(tween(470)),
		exit = slideOutVertically(tween(470)) { it / 2 } + fadeOut(tween(470))
	) {
		GenericScaffold2(
			topBar = { TopBar(onClickCancel = onDismissRequest) },
			bottomBar = {
				BottomBar(
					onClickSelect = {
						selectedBucketObjectId?.let(onSelectBucket) ?: Toast.makeText(context, context.getText(R.string.toast_no_bucket_selected), Toast.LENGTH_SHORT).show()
						onDismissRequest()
					},
				)
			},
			floatingActionButton = {
				FloatingActionButton(
					onClick = { isBucketBottomSheetVisible = true }
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_fa_bucket_list),
						contentDescription = stringResource(id = R.string.new_bucket),
						modifier = Modifier.requiredSize(16.dp)
					)
				}
			}
		) {
			if (bucketList.isEmpty()) EmptyView(
				image = R.drawable.il_empty_chapter,
				title = "No buckets found",
				modifier = Modifier.fillMaxSize()
			)
			else LazyVerticalGrid(
				columns = GridCells.Adaptive(144.dp),
				state = gridState,
				contentPadding = PaddingValues(horizontal = 10.dp),
				verticalArrangement = Arrangement.spacedBy(4.dp),
				horizontalArrangement = Arrangement.spacedBy(4.dp),
				modifier = Modifier.fillMaxSize()
			) {
				items(
					items = bucketList.filter { it.bucketType.name == currentBucketType },
					key = { it.id.toString() }
				) { bucketObject ->
					BucketCard(
						title = bucketObject.title,
						bucketSize = bucketObject.bucketItemCount,
						bucketType = bucketObject.bucketType,
						isLocked = bucketObject.isLocked,
						isFavourite = bucketObject.isFavourite,
						selected = bucketObject.id == selectedBucketObjectId,
						onClick = { selectedBucketObjectId = bucketObject.id },
					)
				}
			}
		}
	}

	BucketBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isBucketBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isBucketBottomSheetVisible = false } },
//		putBucket =
	)
}

