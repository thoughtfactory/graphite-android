package com.syncodec.graphite.presentation.bucketItem2.composable.screen.bookScreen

import android.util.Log
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted


@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BookScreenExpanded(
    bucketItemBox: BucketItemBoxDecrypted? = null
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val bucketItemData by remember(key1 = bucketItemBox?.bucketItemData) { derivedStateOf { bucketItemBox?.bucketItemData } }

    Log.d("npr71", "${windowSizeClass.widthSizeClass}")
    Log.d("npr71", "${windowSizeClass.heightSizeClass}")



}