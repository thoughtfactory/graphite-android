package com.syncodec.graphite.presentation.main2

import android.os.Bundle
import androidx.activity.compose.setContent
import com.syncodec.graphite.presentation.main2.composable.screen.MainScreen
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.accompanist.systemuicontroller.rememberSystemUiController
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
