package com.syncodec.graphite.presentation.main

import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.main.composable.LocalCompositionCloseBottomSheet
import com.syncodec.graphite.presentation.main.composable.LocalCompositionCloseDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsBucketResreshing
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsNoteResreshing
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsNotebookResreshing
import com.syncodec.graphite.presentation.main.composable.LocalCompositionIsSelected
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnDelete
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnExit
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnRefresh
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOnSelected
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOpenBottomSheet
import com.syncodec.graphite.presentation.main.composable.LocalCompositionOpenDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionSelectedObjectIdList
import com.syncodec.graphite.presentation.main.composable.dialog.MainDialogType
import com.syncodec.graphite.presentation.main.composable.screen.FirstTimeScreen
import com.syncodec.graphite.presentation.main.composable.screen.MainScreen
import com.syncodec.graphite.presentation.main.composable.LocalCompositionShowDeleteDialog
import com.syncodec.graphite.presentation.main.composable.LocalCompositionShowExitDialog
import com.syncodec.graphite.presentation.main.composable.bar.BottomNavigationItem
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.DataStoreInstance
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	private val viewModel by viewModels<MainViewModel>()

	private lateinit var auth : FirebaseAuth
	private lateinit var oneTapClient : SignInClient
	private lateinit var signInRequest : BeginSignInRequest

	@OptIn(ExperimentalAnimationApi::class)
	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		auth = Firebase.auth
		oneTapClient = Identity.getSignInClient(this)
		signInRequest = BeginSignInRequest.builder()
			.setPasswordRequestOptions(
				BeginSignInRequest.PasswordRequestOptions.builder()
					.setSupported(true)
					.build()
			)
			.setGoogleIdTokenRequestOptions(
				BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
					.setSupported(true)
					.setServerClientId(BuildConfig.CLIENT_KEY)
					.setFilterByAuthorizedAccounts(false)
					.build()
			)
			.setAutoSelectEnabled(false)
			.build()

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val dataStoreInstance = DataStoreInstance(this)
				val isFirstTime by dataStoreInstance.getIsFirstTime.collectAsState(initial = null)

				val repositoryState by viewModel.repositoryState.collectAsState(initial = null)

				LaunchedEffect(key1 = repositoryState) {
					Toast.makeText(this@MainActivity, "repositoryState: $repositoryState", Toast.LENGTH_SHORT).show()
				}

				val isNoteRefreshing by viewModel.isNoteRefreshing
				val isBucketRefreshing by viewModel.isBucketRefreshing
				val isNotebookRefreshing by viewModel.isNotebookRefreshing

				var isSelected by viewModel.isSelected
				val selectedObjectIdList = viewModel.selectedObjectIdList

				var showDeleteDialog by viewModel.showDeleteDialog
				var showExitDialog by viewModel.showExitDialog

				val navController = rememberAnimatedNavController()
				val navBackStackEntry by navController.currentBackStackEntryAsState()
				val currentRoute = navBackStackEntry?.destination?.route

				fun openDialog(_mainDialogType : MainDialogType) {
					when (_mainDialogType) {
						MainDialogType.DELETE -> showDeleteDialog = true
						MainDialogType.EXIT -> showExitDialog = true
						else -> null
					}
				}

				fun closeDialog(_mainDialogType : MainDialogType) {
					when (_mainDialogType) {
						MainDialogType.DELETE -> showDeleteDialog = false
						MainDialogType.EXIT -> showExitDialog = false
						else -> null
					}
				}

				onBackPressedDispatcher.addCallback(
					this, object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							if (isSelected) {
								selectedObjectIdList.clear()
								isSelected = false
							} else {
								if (currentRoute == BottomNavigationItem.Home.route) {
									openDialog(MainDialogType.EXIT)
								} else {
									navController.popBackStack(route = BottomNavigationItem.Home.route, inclusive =  false, saveState = true)
								}
							}
						}
					}
				)

				CompositionLocalProvider(
					LocalCompositionIsNoteResreshing provides isNoteRefreshing,
					LocalCompositionIsBucketResreshing provides isBucketRefreshing,
					LocalCompositionIsNotebookResreshing provides isNotebookRefreshing,
					LocalCompositionOnRefresh provides { viewModel.refresher.value = viewModel.refresher.value + 1 },
					LocalCompositionIsSelected provides isSelected,
					LocalCompositionOnSelected provides { isSelected = it },
					LocalCompositionSelectedObjectIdList provides selectedObjectIdList,
					LocalCompositionOpenDialog provides ::openDialog,
					LocalCompositionCloseDialog provides ::closeDialog,
					LocalCompositionShowDeleteDialog provides showDeleteDialog,
					LocalCompositionShowExitDialog provides showExitDialog,
					LocalCompositionOnDelete provides { viewModel.delete() },
					LocalCompositionOnExit provides { finishAndRemoveTask() },
				) {
					AnimatedContent(targetState = isFirstTime) {
						when (it) {
							true -> FirstTimeScreen(onClickLogin = this@MainActivity::signIn)
							false -> MainScreen(
								viewModel = viewModel,
								currentRoute = currentRoute,
								navController = navController,
							)
							else -> LoadingView()
						}
					}
				}
			}
		}
	}

	override fun onResume() {
		super.onResume()

		viewModel.refresher.value = viewModel.refresher.value + 1
	}

	private val signInIntentResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
		if (result.data != null) {
			try {

				val googleCredential = oneTapClient.getSignInCredentialFromIntent(result.data)
				val displayName = googleCredential.displayName
				val username = googleCredential.id
				val password = googleCredential.password
				val idToken = googleCredential.googleIdToken
				val profilePictureUri = googleCredential.profilePictureUri

				if (idToken == null) {
					Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
				} else {
					val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
					auth.signInWithCredential(firebaseCredential)
						.addOnCompleteListener(this) { task ->
							if (task.isSuccessful) {
								Log.d("npr71", "signInWithCredential:success")
								val user = auth.currentUser
								updateUI(user)
							} else {
								Log.w("npr71", "signInWithCredential:failure", task.exception)
								updateUI(null)
							}
						}
				}
			} catch (e : ApiException) {
//					e.printStackTrace()
				Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun signIn() {
		oneTapClient.beginSignIn(signInRequest)
			.addOnSuccessListener(this) { result ->
				try {
					IntentSenderRequest.Builder(result.pendingIntent.intentSender).build().let {
						signInIntentResultLauncher.launch(it)
					}
				} catch (e : IntentSender.SendIntentException) {
					Log.e("npr71", "Couldn't start One Tap UI: ${e.localizedMessage}")
				}
			}
			.addOnFailureListener(this) { e ->
				Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
			}
	}

	private fun updateUI(user : FirebaseUser?) {
		if (user != null) {
			val dataStoreInstance = DataStoreInstance(this)
			dataStoreInstance.putIsFirstTime(false)

			Toast.makeText(this, "Hi ${user.displayName}", Toast.LENGTH_SHORT).show()
		} else {
			Toast.makeText(this, "Error signing in. Please try again later.", Toast.LENGTH_SHORT).show()
		}
	}
}
