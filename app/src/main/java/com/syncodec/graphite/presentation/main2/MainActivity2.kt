package com.syncodec.graphite.presentation.main2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.syncodec.graphite.presentation.main2.composable.screen.MainScreen
import com.syncodec.graphite.presentation.ui.BaseComposable2
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity2 : ComponentActivity() {

    private val viewModel by viewModel<MainViewModel2>()


    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            BaseComposable2 {

                MainScreen()
            }

        }
    }
}
