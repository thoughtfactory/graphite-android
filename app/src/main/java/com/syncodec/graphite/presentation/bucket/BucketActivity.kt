package com.syncodec.graphite.presentation.bucket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kedia.ogparser.OpenGraphResult
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionBucketItemObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionBucketObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnAddLink
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnBackPressed
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnClickBucketItemDelete
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnClickBucketItemFavourite
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnClickBucketItemLock
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnClickFavourite
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnClickLock
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnRefresh
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOnSelected
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionOpenGraphResult
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionSetBucketItemObject
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionSetOpenGraphResult
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.bucket.composable.LocalCompositionShowLinkDialog
import com.syncodec.graphite.presentation.bucket.composable.dialog.BucketDialogType
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.Extra
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalVaultIsOpened
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.types.ObjectId


@AndroidEntryPoint
class BucketActivity : ComponentActivity() {

	private val viewModel by viewModels<BucketViewModel>()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		val hasBucketId = intent.hasExtra(Extra.Companion.Constant.BUCKET_ID.name)
		if (hasBucketId) {
			val bucketId = intent.getStringExtra(Extra.Companion.Constant.BUCKET_ID.name)?.let { ObjectId.from(it) }
			if (bucketId != null) {
				viewModel.loadAndViewData(bucketId)
			} else {
				finish()
			}
		} else {
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val bucketObject by viewModel.bucketObject

				var isSelected by viewModel.isSelected
				val selectedObjectIdList = viewModel.selectedObjectIdList

				var showDeleteDialog by remember { mutableStateOf(false) }
				var showLinkDialog by remember { mutableStateOf(false) }

				var openGraphResult by remember { mutableStateOf<OpenGraphResult?>(null) }

				var bucketItemObject by viewModel.bucketItemObject

				val isVaultOpened = LocalVaultIsOpened.current
				val onAuthenticatorAction = LocalAuthenticatorAction.current

				fun openDialog(_bucketDialogType : BucketDialogType) {
					when (_bucketDialogType) {
						BucketDialogType.DELETE -> showDeleteDialog = true
						else -> null
					}
				}

				fun closeDialog(_bucketDialogType : BucketDialogType) {
					when (_bucketDialogType) {
						BucketDialogType.DELETE -> showDeleteDialog = false
						else -> null
					}
				}

				onBackPressedDispatcher.addCallback(
					this, object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							if (showDeleteDialog) {
								closeDialog(BucketDialogType.DELETE)
							} else {
								if (isSelected) {
									selectedObjectIdList.clear()
									isSelected = false
								} else {
									finish()
								}
							}
						}
					}
				)

				CompositionLocalProvider(
					LocalCompositionBucketObject provides bucketObject,
					LocalCompositionOnRefresh provides viewModel::refresh,
					LocalCompositionOnClickLock provides {
						if (isVaultOpened) viewModel.toggleLock()
						else onAuthenticatorAction(Authenticator.AUTHENTICATE)
					},
					LocalCompositionOnClickFavourite provides viewModel::toggleFavourite,
					LocalCompositionSetBucketItemObject provides { viewModel.getBucketItem(it.id) },
					LocalCompositionBucketItemObject provides bucketItemObject,
					LocalCompositionOnClickBucketItemLock provides {
						if (isVaultOpened) viewModel.toggleBucketItemLock(it)
						else onAuthenticatorAction(Authenticator.AUTHENTICATE)
					},
					LocalCompositionOnClickBucketItemFavourite provides viewModel::toggleBucketItemFavourite,
					LocalCompositionOnClickBucketItemDelete provides viewModel::deleteBucketItem,
					LocalCompositionOnAddLink provides viewModel::putLink,
					LocalCompositionIsSelected provides isSelected,
					LocalCompositionSelectedObjectIdList provides selectedObjectIdList,
					LocalCompositionOnSelected provides { isSelected = it },
					LocalCompositionOpenDialog provides ::openDialog,
					LocalCompositionCloseDialog provides ::closeDialog,
					LocalCompositionSetOpenGraphResult provides { openGraphResult = it },
					LocalCompositionOpenGraphResult provides openGraphResult,
					LocalCompositionShowLinkDialog provides showLinkDialog,
					LocalCompositionShowDeleteDialog provides showDeleteDialog,
					LocalCompositionOnDelete provides viewModel::deleteBucketItem,
					LocalCompositionOnBackPressed provides { this.onBackPressedDispatcher.onBackPressed() }
				) {
					BucketScreen()
				}
			}
		}
	}

	override fun onStart() {
		super.onStart()

		viewModel.refresh()
	}
}
