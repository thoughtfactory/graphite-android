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
import com.syncodec.graphite.di.cloud.dropbox.DBox
import com.syncodec.graphite.di.cloud.googleDrive.GDrive
import com.syncodec.graphite.di.locator.GeoLocator
import com.syncodec.graphite.di.repository.Repository
import com.syncodec.graphite.presentation.attachment.composable.screen.AttachmentScreenViewModel
import com.syncodec.graphite.presentation.bucket.BucketViewModel
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
import com.syncodec.graphite.presentation.note2.NoteViewModel2
import com.syncodec.graphite.presentation.notebook.composable.NotebookScreenViewModel2
import com.syncodec.graphite.presentation.search.SearchViewModel
import com.syncodec.graphite.presentation.settings.composable.screen.ImportDataViewModel
import com.syncodec.graphite.presentation.sync.dropbox.DropboxSyncViewModel
import com.syncodec.graphite.presentation.sync.googleDrive.GoogleDriveSyncViewModel
import com.syncodec.graphite.presentation.tags.TagsViewModel
import com.syncodec.graphite.presentation.base.secureComposable.AuthenticationState
import com.syncodec.graphite.utils.alice.Alice
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File
import java.time.Instant


class BaseApplication : Application() {

	override fun onCreate() {
		super.onCreate()
		FirebaseApp.initializeApp(this)

//		LeakCanary.config = LeakCanary.config.copy(dumpHeap = false)

		initDirectory()

		val dataStoreInstance = DataStoreInstance(context = this)
		val sortByFlow = dataStoreInstance.getSortBy
		val sortOnFlow = dataStoreInstance.getSortOn

		val repositoryStatusStateFlow: MutableStateFlow<Repository.Companion.RepositoryStatus> = MutableStateFlow(Repository.Companion.RepositoryStatus.Init)

		val repository = Repository()
//		repository.initRepository(context = this, dataStoreInstance = dataStoreInstance)
		repositoryStatusStateFlow.tryEmit(Repository.Companion.RepositoryStatus.Success(repository = repository))

		val isAuthenticated = repository.isUnlocked

		val geoLocator = GeoLocator(this)

		startKoin {
			androidLogger()
			androidContext(this@BaseApplication)
			modules(
				module {
					single { repository }
					single { repositoryStatusStateFlow }
					single { dataStoreInstance }
					single { isAuthenticated }
					single { sortByFlow }
					single { sortOnFlow }
					single { geoLocator }
					single { DBox(this@BaseApplication) }
					single { GDrive(this@BaseApplication) }

					viewModelOf(::MainViewModel)
					viewModelOf(::NoteScreenViewModel)
					viewModelOf(::BucketScreenViewModel)
					viewModelOf(::NotebookScreenViewModel)
					viewModelOf(::ExplorerViewModel)

					viewModelOf(::NoteViewModel2)
//					viewModelOf(::ViewerScreenViewModel)

					viewModelOf(::BucketViewModel)
					viewModelOf(::BucketScreenCommonViewModel)
					viewModelOf(::NotebookScreenViewModel2)
					viewModelOf(::TagsViewModel)
					viewModelOf(::AttachmentScreenViewModel)
					viewModelOf(::ImportDataViewModel)

					viewModelOf(::BookBucketItemViewModel)
					viewModelOf(::ShowBucketItemViewModel)

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
					viewModelOf(::DropboxSyncViewModel)
					viewModelOf(::GoogleDriveSyncViewModel)
				}
			)
		}

		Purchases.debugLogsEnabled = false
		val auth = Firebase.auth

		val purchasesConfiguration = PurchasesConfiguration
			.Builder(
				this,
				Alice.decrypt(BuildConfig.REVENUE_CAT_API_KEY, "lt3(3x4R7M^107!&4E74Z%*o8cp2i7y@")
					?: ""
			)
			.appUserID(auth.currentUser?.uid)
			.build()
		Purchases.configure(purchasesConfiguration)

		auth.currentUser?.uid?.let { uid ->
			CoroutineScope(Dispatchers.Default).launch {
				dataStoreInstance.getSuperExpiryTime.collect { superExpiryTimeString ->
					try {
						val currentTimestamp = Instant.now().toEpochMilli()
						when {
							superExpiryTimeString == "" -> getRevenueCatInfo(auth)
							superExpiryTimeString.toLong() > currentTimestamp -> isPro.tryEmit(true)
							else -> getRevenueCatInfo(auth)
						}
					} catch (e: Exception) {
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
		val isPro: MutableStateFlow<Boolean> = MutableStateFlow(true)
		val authenticationState: MutableStateFlow<AuthenticationState> = MutableStateFlow(AuthenticationState.None)
	}
}
