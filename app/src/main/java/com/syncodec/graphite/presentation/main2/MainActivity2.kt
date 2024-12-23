package com.syncodec.graphite.presentation.main2

import android.os.Bundle
import androidx.activity.compose.setContent
import com.syncodec.graphite.presentation.main2.composable.screen.MainScreen
import androidx.activity.ComponentActivity
import com.syncodec.graphite.presentation.ui.BaseContent
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue


class MainActivity2 : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel2>()


    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            BaseContent {
                MainScreen()
            }

        }
    }
}
