package com.syncodec.graphite.presentation.common.bar

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.button.BackButton
import com.syncodec.graphite.presentation.common.button.GenericButton


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun GenericTopBar(
	title : String = "Generic Top Bar",
	navigationIcon : Int = R.drawable.ic_flat_back,
	onNavigationIconClick : (() -> Unit)? = null,
) {

}
