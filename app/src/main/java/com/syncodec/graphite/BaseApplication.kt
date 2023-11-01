package com.syncodec.graphite

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.di.cloud.dropbox.DropboxConnector
import com.syncodec.graphite.di.locator.GeoLocator
import com.syncodec.graphite.di.repository.LockableRepo
import com.syncodec.graphite.presentation.attachment.composable.screen.AttachmentScreenViewModel
import com.syncodec.graphite.presentation.base.secureComposable.AuthenticationState
import com.syncodec.graphite.presentation.bucket.composable.screen.BucketScreenCommonViewModel
import com.syncodec.graphite.presentation.bucketItem.viewModel.BookBucketItemViewModel
import com.syncodec.graphite.presentation.bucketItem.viewModel.ShowBucketItemViewModel
import com.syncodec.graphite.presentation.common.dialog.where.whereBucketDialog.WhereBucketDialogViewModel2
import com.syncodec.graphite.presentation.common.dialog.where.whereChapterDialog2.WhereChapterDialogViewModel2
import com.syncodec.graphite.presentation.explorer.atlas.AtlasViewModel
import com.syncodec.graphite.presentation.explorer.calendar.CalendarViewModel
import com.syncodec.graphite.presentation.main.MainViewModel
import com.syncodec.graphite.presentation.main.composable.screen.bucketScreen.BucketScreenViewModel
import com.syncodec.graphite.presentation.main.composable.screen.explorerScreen.ExplorerViewModel
import com.syncodec.graphite.presentation.main.composable.screen.noteScreen.NoteScreenViewModel
import com.syncodec.graphite.presentation.main.composable.screen.notebookScreen.NotebookScreenViewModel
import com.syncodec.graphite.presentation.note.NoteViewModel
import com.syncodec.graphite.presentation.notebook.composable.NotebookScreenViewModel2
import com.syncodec.graphite.presentation.search.SearchViewModel
import com.syncodec.graphite.presentation.settings.composable.viewModel.ClearDataViewModel
import com.syncodec.graphite.presentation.settings.composable.viewModel.ExportDataViewModel
import com.syncodec.graphite.presentation.settings.composable.viewModel.ImportDataViewModel
import com.syncodec.graphite.presentation.settings.composable.viewModel.LocalBackupViewModel
import com.syncodec.graphite.presentation.sync.dropbox2.DropboxViewModel
import com.syncodec.graphite.presentation.tags.TagsViewModel
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import com.syncodec.graphite.utils.dataStore.SyncDataStoreInstance
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File


class BaseApplication : Application() {

	override fun onCreate() {
		super.onCreate()
		FirebaseApp.initializeApp(this)

//		LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)

		initDirectory()

		val dataStoreInstance = DataStoreInstance(context = this)
		val syncDataStoreInstance = SyncDataStoreInstance(context = this)
		val lockableRepo = LockableRepo(context = this.applicationContext, dataStoreInstance = dataStoreInstance)

		val repositoryStatusStateFlow = lockableRepo.repositoryStatusFlow
		val isAuthenticated = lockableRepo.isUnlocked

		val geoLocator = GeoLocator(context = this)

		val dropboxConnector = DropboxConnector(context = this)

		startKoin {
			androidLogger()
			androidContext(this@BaseApplication)
			modules(
				module {
					single { lockableRepo }
					single { repositoryStatusStateFlow }
					single { dataStoreInstance }
					single { syncDataStoreInstance }
					single { isAuthenticated }
					single { geoLocator }
					single { DBox(this@BaseApplication) }
					single { dropboxConnector }

					viewModelOf(::MainViewModel)
					viewModelOf(::NoteScreenViewModel)
					viewModelOf(::BucketScreenViewModel)
					viewModelOf(::NotebookScreenViewModel)
					viewModelOf(::ExplorerViewModel)

					viewModelOf(::NoteViewModel)
//					viewModelOf(::ViewerScreenViewModel)

					viewModelOf(::BucketScreenCommonViewModel)
					viewModelOf(::NotebookScreenViewModel2)
					viewModelOf(::TagsViewModel)
					viewModelOf(::AttachmentScreenViewModel)

					viewModelOf(::BookBucketItemViewModel)
					viewModelOf(::ShowBucketItemViewModel)

					viewModelOf(::ExportDataViewModel)
					viewModelOf(::ImportDataViewModel)
					viewModelOf(::ClearDataViewModel)
					viewModelOf(::LocalBackupViewModel)

//					viewModelOf(::ExportDataViewModel)
//					viewModelOf(::ImportDataGraphiteViewModel)
//					viewModelOf(::ImportDataJourneyViewModel)
//					viewModelOf(::ImportDataGoogleKeepViewModel)
//					viewModelOf(::ClearDataViewModel)
//					viewModelOf(::LocalBackupViewModel)
//					viewModelOf(::LocalBackupViewModel)
					viewModelOf(::WhereChapterDialogViewModel2)
					viewModelOf(::WhereBucketDialogViewModel2)
					viewModelOf(::AtlasViewModel)
					viewModelOf(::CalendarViewModel)
					viewModelOf(::SearchViewModel)

					viewModelOf(::DropboxViewModel)
				}
			)
		}

		Purchases.debugLogsEnabled = false
		val auth = Firebase.auth

//		val purchasesConfiguration = PurchasesConfiguration
//			.Builder(
//				this,
//				Alice.decrypt(BuildConfig.REVENUE_CAT_API_KEY, "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@")
//					?: ""
//			)
//			.appUserID(auth.currentUser?.uid)
//			.build()
//		Purchases.configure(purchasesConfiguration)
//
//		auth.currentUser?.uid?.let { uid ->
//			CoroutineScope(Dispatchers.Default).launch {
//				dataStoreInstance.getSuperExpiryTime.collect { superExpiryTimeString ->
//					try {
//						val currentTimestamp = Instant.now().toEpochMilli()
//						when {
//							superExpiryTimeString == "" -> getRevenueCatInfo(auth)
//							superExpiryTimeString.toLong() > currentTimestamp -> isPro.tryEmit(true)
//							else -> getRevenueCatInfo(auth)
//						}
//					} catch (e: Exception) {
//						getRevenueCatInfo(auth)
//					}
//				}
//			}
//		}
	}

	private fun initDirectory() {
		val filesDir = this.filesDir
		val dataDir = File(filesDir, "data").also { it.mkdirs() }
		val attachmentDir = File(dataDir, "attachment").also { it.mkdirs() }
	}

	private fun getRevenueCatInfo(auth: FirebaseAuth) {
		Purchases
			.sharedInstance
			.apply {
				setAttributes(mapOf("\$email" to auth.currentUser?.email))
				getCustomerInfo(
					fetchPolicy = CacheFetchPolicy.NOT_STALE_CACHED_OR_CURRENT,
					callback = object : ReceiveCustomerInfoCallback {
						override fun onError(error: PurchasesError) {
						}

						override fun onReceived(customerInfo: CustomerInfo) {
							isPro.tryEmit(customerInfo.entitlements["pro"]?.isActive == true)
						}
					}
				)
			}
	}

	companion object {
		val isPro: MutableStateFlow<Boolean> = MutableStateFlow(false)
		val authenticationState: MutableStateFlow<AuthenticationState> = MutableStateFlow(AuthenticationState.None)
	}
}
