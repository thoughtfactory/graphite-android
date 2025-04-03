package com.syncodec.graphite.presentation.bucket2.moveBucketItemScaffold.bar

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToAlphaText
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToBetaIcon
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToBetaSelectedIcon
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToBetaText
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToGammaText
import com.syncodec.graphite.presentation.common.animation.AnimatedText
import com.syncodec.graphite.presentation.common.v2.button.GraIconButton
import com.syncodec.graphite.presentation.common.navigationTab.GenericTabRow
import com.syncodec.graphite.presentation.common.navigationTab.TabItem
import com.syncodec.graphite.presentation.common.v2.selectable2.LocalSelectionContainerActor
import com.syncodec.graphite.presentation.ui.AnimationDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    TopAppBar(
        navigationIcon = { GraIconButton.CancelButton { onBackPressedDispatcher?.onBackPressed() } },
        title = { Text(text = stringResource(id = R.string.move_selected_items)) },
        actions = {},
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onBackground),
        modifier = Modifier.fillMaxWidth()
    )
}
