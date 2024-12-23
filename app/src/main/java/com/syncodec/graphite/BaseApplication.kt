package com.syncodec.graphite

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.syncodec.graphite.di.repository.repository.Repository
import com.syncodec.graphite.di.secureRepository.BoxRepository
import com.syncodec.graphite.di.sync.dropbox.DBox
import com.syncodec.graphite.presentation.attachment.composable.screen.AttachmentScreenViewModel
import com.syncodec.graphite.presentation.bucket.BucketViewModel
import com.syncodec.graphite.presentation.bucket.composable.bottomSheet.BucketBottomSheetViewModel
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreenCommonViewModel
import com.syncodec.graphite.presentation.bucketItem.BucketItemViewModel
import com.syncodec.graphite.presentation.common.dialog.whereDialog.WhereDialogViewModel
import com.syncodec.graphite.presentation.explorer.ExplorerScreenViewModel
import com.syncodec.graphite.presentation.explorer.screen.searchScreen.SearchScreenViewModel
import com.syncodec.graphite.presentation.main.composable.screen.bucketScreen.BucketScreenViewModel
import com.syncodec.graphite.presentation.main.composable.screen.noteScreen.NoteScreenViewModel
import com.syncodec.graphite.presentation.main2.MainViewModel2
import com.syncodec.graphite.presentation.note.screen.editorScreen.EditorScreenViewModel
import com.syncodec.graphite.presentation.note.screen.viewerScreen.ViewerScreenViewModel
import com.syncodec.graphite.presentation.settings.composable.dialog.clearData.ClearDataViewModel
import com.syncodec.graphite.presentation.settings.composable.dialog.exportData.ExportDataViewModel
import com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.googleKeep.ImportDataGoogleKeepViewModel
import com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.graphite.ImportDataGraphiteViewModel
import com.syncodec.graphite.presentation.settings.composable.screen.importDataScreen.dialog.journey.ImportDataJourneyViewModel
import com.syncodec.graphite.presentation.settings.composable.screen.localBackupScreen.LocalBackupViewModel
import com.syncodec.graphite.presentation.sync.dropbox.DropboxSyncViewModel
import com.syncodec.graphite.presentation.tags.TagsViewModel
import com.syncodec.graphite.utils.AuthenticatorScreen
import com.syncodec.graphite.utils.DataStoreInstance
import io.objectbox.android.Admin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.io.File


class BaseApplication : Application() {

	private lateinit var dataStore : DataStoreInstance

	override fun onCreate() {
		super.onCreate()
		FirebaseApp.initializeApp(this)

//		LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)

		initDirectory()

		startKoin {
			androidLogger()
			androidContext(this@BaseApplication)
			modules(
				module {
					single { Repository() }
					single { BoxRepository(context = this@BaseApplication) }
					single { DBox(this@BaseApplication) }

					viewModelOf(::MainViewModel2)

//					viewModelOf(::MainViewModel)
					viewModelOf(::NoteScreenViewModel)
					viewModelOf(::BucketScreenViewModel)
//					viewModelOf(::NotebookScreenViewModel)
					viewModelOf(::EditorScreenViewModel)
					viewModelOf(::ViewerScreenViewModel)
					viewModelOf(::BucketViewModel)
					viewModelOf(::BucketScreenCommonViewModel)
					viewModelOf(::BucketBottomSheetViewModel)
					viewModelOf(::BucketItemViewModel)
//					viewModelOf(::NotebookScreenViewModel2)
					viewModelOf(::SearchScreenViewModel)
					viewModelOf(::TagsViewModel)
					viewModelOf(::AttachmentScreenViewModel)
					viewModelOf(::ExportDataViewModel)
					viewModelOf(::ImportDataGraphiteViewModel)
					viewModelOf(::ImportDataJourneyViewModel)
					viewModelOf(::ImportDataGoogleKeepViewModel)
					viewModelOf(::ClearDataViewModel)
					viewModelOf(::LocalBackupViewModel)
					viewModelOf(::LocalBackupViewModel)
					viewModelOf(::WhereDialogViewModel)
					viewModelOf(::ExplorerScreenViewModel)
					viewModelOf(::DropboxSyncViewModel)
				}
			)
		}

		dataStore = DataStoreInstance(this)
		Purchases.debugLogsEnabled = false
		val auth = Firebase.auth

		val purchasesConfiguration = PurchasesConfiguration
//			.Builder(this, Alice.decrypt(BuildConfig.REVENUE_CAT_API_KEY, "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@") ?: "")
			.Builder(this, "goog_XBisvPpghYpVZbPweBjCOmhLvbI")
			.appUserID(auth.currentUser?.uid)
			.build()
		Purchases.configure(purchasesConfiguration)

		auth.currentUser?.uid?.let { uid ->
			CoroutineScope(Dispatchers.Default).launch {
				dataStore.getSuperExpiryTime.collect { superExpiryTimeString ->
					try {
						val currentTimestamp = System.currentTimeMillis()
						when {
							superExpiryTimeString == "" -> getRevenueCatInfo(auth)
							superExpiryTimeString.toLong() > currentTimestamp -> isPro.tryEmit(true)
							else -> getRevenueCatInfo(auth)
						}
					} catch (e : Exception) {
						getRevenueCatInfo(auth)
					}
				}
			}
		}
	}

	private fun initDirectory() {
		val filesDir = this.filesDir
		val dataDir = File(filesDir, "data").also { it.mkdirs() }
		val attachmentDir = File(dataDir, "attachment").also { it.mkdirs() }
	}

	private fun getRevenueCatInfo(auth : FirebaseAuth) {
		Purchases
			.sharedInstance
			.apply {
				setAttributes(mapOf("\$email" to auth.currentUser?.email))
				getCustomerInfo(
					fetchPolicy = CacheFetchPolicy.NOT_STALE_CACHED_OR_CURRENT,
					callback = object : ReceiveCustomerInfoCallback {
						override fun onError(error : PurchasesError) {
						}

						override fun onReceived(customerInfo : CustomerInfo) {
							isPro.tryEmit(customerInfo.entitlements["pro"]?.isActive == true)
						}
					}
				)
			}
	}

	companion object {
		val isPro : MutableStateFlow<Boolean> = MutableStateFlow(false)

		val isAuthenticated : MutableStateFlow<Boolean> = MutableStateFlow(false)
		val authenticatorScreen : MutableStateFlow<AuthenticatorScreen> = MutableStateFlow(AuthenticatorScreen.None)
	}
}
