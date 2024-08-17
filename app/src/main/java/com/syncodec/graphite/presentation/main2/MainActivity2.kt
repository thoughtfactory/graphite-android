package com.syncodec.graphite.presentation.main2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.presentation.main2.model.MainViewModel2
import com.syncodec.graphite.presentation.main2.screen.RepoStateScreen
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue


class MainActivity2 : ComponentActivity() {

    private val viewModel: MainViewModel2 by viewModel()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BaseComposable {

                val mainScreenState by viewModel.mainState.collectAsState()
                Log.d("mainScreenState", "mainScreenState : ${mainScreenState.appState}")

                RepoStateScreen(
                    isFirstTime = mainScreenState.isFirstTime,
                    appState = mainScreenState.appState,
                    onClickUnlock = {}
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
