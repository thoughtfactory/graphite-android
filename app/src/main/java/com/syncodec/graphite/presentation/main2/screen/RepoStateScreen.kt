package com.syncodec.graphite.presentation.main2.screen

import android.util.Log
import androidx.compose.runtime.Composable
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.main2.model.AppState


@Composable
fun RepoStateScreen(
    isFirstTime: Boolean?,
    appState: AppState,
    onClickUnlock: () -> Unit
) {
    Log.d("npr71", "isFirstTime : $isFirstTime, appState:$appState")
    when (isFirstTime) {
        null -> LoadingView()
        true -> FirstTimeScreen()
        false -> when (appState) {
            AppState.LockedRepo -> LockedRepoScreen(onClickUnlock = onClickUnlock)
            AppState.RepoError -> LockedRepoScreen(onClickUnlock = onClickUnlock)
            AppState.UnlockedRepo -> MainScreen()
        }
    }
}
