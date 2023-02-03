package com.syncodec.graphite.presentation.bucket

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.di.model.BucketObject
import com.syncodec.graphite.di.model.BucketType
import com.syncodec.graphite.di.network.ShowType
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnShare
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnUpdateBucket
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionSetBucketItemObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionShowEditBucketDialog
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetType
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetViewModel
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreen
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreenCommonViewModel
import com.syncodec.graphite.presentation.common.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.common.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.common.LocalCompositionOnSelect
import com.syncodec.graphite.presentation.common.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.common.LocalCompositionSelectedRealmUUIDIdList
import com.syncodec.graphite.presentation.common.dialog.DialogType
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class BucketActivity : ComponentActivity() {

	private val viewModel by viewModel<BucketViewModel>()
	private val bucketScreenCommonViewModel by viewModel<BucketScreenCommonViewModel>()
	private val bucketBottomSheetViewModel by viewModel<BucketBottomSheetViewModel>()

	@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		val hasBucketId = intent.hasExtra(Extra.Companion.Extra.BUCKET_ID.name)
		if (hasBucketId) {
			val bucketId = intent.getByteArrayExtra(Extra.Companion.Extra.BUCKET_ID.name)?.let { RealmUUID.from(it) }
			bucketId?.let { realmUUID ->
				viewModel.loadAndViewData(realmUUID)
				bucketScreenCommonViewModel.initBucket(realmUUID)
				bucketBottomSheetViewModel.initBucket(realmUUID)
			} ?: run {
				Toast.makeText(this, "Error loading bucket. No id specified.", Toast.LENGTH_SHORT).show()
				finish()
			}
		} else {
			Toast.makeText(this, "Error loading bucket. No id specified.", Toast.LENGTH_SHORT).show()
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val scope = rememberCoroutineScope()

				val keyboardController = LocalSoftwareKeyboardController.current
				val focusManager = LocalFocusManager.current

				val bucketObject by viewModel.bucketObject

				var isSelected by remember { mutableStateOf(false) }
				val selectedRealmUUIDList = remember { mutableStateListOf<RealmUUID>() }

				var showDeleteDialog by remember { mutableStateOf(false) }
				var showEditBucketDialog by remember { mutableStateOf(false) }

				var openGraphResult by remember { mutableStateOf<OpenGraphResult?>(null) }

				val bucketItemObject by viewModel.bucketItemObject

				var bottomSheetType : BucketBottomSheetType by rememberSaveable { mutableStateOf(BucketBottomSheetType.MENU) }
				val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

				fun openSheet(_bottomSheetType : BucketBottomSheetType) {
					scope.launch { bottomSheetType = _bottomSheetType; modalBottomSheetState.show() }
				}

				fun closeSheet() {
					scope.launch { modalBottomSheetState.hide() }
				}

				LaunchedEffect(key1 = modalBottomSheetState.currentValue) {
					if (modalBottomSheetState.currentValue == ModalBottomSheetValue.Hidden) {
						try {
							keyboardController?.hide()
							focusManager.clearFocus()
						} catch (_ : Exception) {
						}
					}
				}

				fun openDialog(_bucketDialogType : DialogType) {
					when (_bucketDialogType) {
						DialogType.DELETE -> showDeleteDialog = true
						DialogType.EDIT -> showEditBucketDialog = true
						else -> null
					}
				}

				fun closeDialog(_bucketDialogType : DialogType) {
					when (_bucketDialogType) {
						DialogType.DELETE -> showDeleteDialog = false
						DialogType.EDIT -> showEditBucketDialog = false
						else -> null
					}
				}

				onBackPressedDispatcher.addCallback(
					this, object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							if (showDeleteDialog || showEditBucketDialog) {
								closeDialog(DialogType.DELETE)
								closeDialog(DialogType.EDIT)
							} else if (modalBottomSheetState.isVisible) {
								closeSheet()
							} else if (isSelected) {
								selectedRealmUUIDList.clear()
								isSelected = false
							} else {
								finish()
							}
						}
					}
				)

				CompositionLocalProvider(
					LocalCompositionSetBucketItemObject provides bucketBottomSheetViewModel::setBucketItemObject,
					LocalCompositionIsSelected provides isSelected,
					LocalCompositionSelectedRealmUUIDIdList provides selectedRealmUUIDList,
					LocalCompositionOnSelect provides { isSelected = it },
					LocalCompositionOpenBottomSheet provides ::openSheet,
					LocalCompositionCloseBottomSheet provides ::closeSheet,
					LocalCompositionOpenDialog provides ::openDialog,
					LocalCompositionCloseDialog provides ::closeDialog,
					LocalCompositionShowEditBucketDialog provides showEditBucketDialog,
					LocalCompositionShowDeleteDialog provides showDeleteDialog,
					LocalCompositionOnDelete provides {
						if (selectedRealmUUIDList.isEmpty()) {
							Intent().apply {
								putExtra(Extra.Companion.Extra.INTENT_ACTION.name, Extra.Companion.IntentAction.DELETE.name)
								putExtra(Extra.Companion.Extra.OBJECT_ID.name, bucketObject?.id?.bytes)
								setResult(Activity.RESULT_OK, this)
								this@BucketActivity.finish()
							}
						} else {
							viewModel.deleteBucketItem(selectedRealmUUIDList.toList())
							selectedRealmUUIDList.clear()
							isSelected = false
						}
					},
					LocalCompositionOnUpdateBucket provides viewModel::updateBucket,
					LocalCompositionOnShare provides { shareAll ->
						bucketObject?.let { onShare(bucketObject = it, selectedRealmUUIDList = selectedRealmUUIDList, shareAll = shareAll) } ?: Toast.makeText(
							this@BucketActivity,
							"Error sharing items",
							Toast.LENGTH_SHORT
						).show()
					},
				) {
					BucketScreen(
						bucketObject = bucketObject,
						modalBottomSheetState = modalBottomSheetState,
						bottomSheetType = bottomSheetType,
						onClickBack = { this.onBackPressedDispatcher.onBackPressed() }
					)
				}
			}
		}
	}

	private fun onShare(bucketObject : BucketObject, selectedRealmUUIDList : List<RealmUUID>, shareAll : Boolean) {

		val bucketItemObjectList = bucketObject.bucketItemList

		val baseUrl = when (bucketObject.bucketType) {
			BucketType.TODO.name -> ""
			BucketType.BOOK.name -> " - https://openlibrary.org"
			BucketType.SHOW.name -> " - https://www.themoviedb.org/"
			BucketType.LINK.name -> ""
			BucketType.UNKNOWN.name -> ""
			else -> ""
		}

		var shareText = ""
		bucketItemObjectList.filter { if (shareAll) true else it.id in selectedRealmUUIDList }.forEach {
			val connector = when (it.getShowData()?.type) {
				ShowType.TV -> "tv/"
				ShowType.MOVIE -> "movie/"
				else -> ""
			}
			shareText += "${it.title}$baseUrl$connector${if (bucketObject.bucketType == BucketType.TODO.name) "" else it.key}\n"
		}

		Intent(Intent.ACTION_SEND).apply {
			type = "text/html"
			putExtra(Intent.EXTRA_SUBJECT, bucketObject.title ?: bucketObject.bucketType)
//			putExtra(Intent.EXTRA_TEXT, Html.fromHtml(shareText, Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST))
			putExtra(Intent.EXTRA_TEXT, shareText)

			if (resolveActivity(this@BucketActivity.packageManager) != null) startActivity(Intent.createChooser(this, "Share using"))
			else Toast.makeText(this@BucketActivity, "No app found on your device which can perform this action", Toast.LENGTH_SHORT).show()
		}
	}
}
