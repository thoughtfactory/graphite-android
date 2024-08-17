package com.syncodec.graphite.presentation.main2.model

data class MainScreenState(
    val isFirstTime: Boolean? = null,
    val appState: AppState = AppState.LockedRepo
)

enum class AppState {
    LockedRepo,
    UnlockedRepo,
    RepoError
}
