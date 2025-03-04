package com.syncodec.graphite.presentation.bucket2.composable.buildingBlock

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBox
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToAlphaText
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToBetaIcon
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToBetaSelectedIcon
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToBetaText
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToGammaText
import com.syncodec.graphite.presentation.common.navigationTab.GenericTabRow
import com.syncodec.graphite.presentation.common.navigationTab.TabDefaults
import com.syncodec.graphite.presentation.common.navigationTab.TabItem


@Composable
fun BucketItemStateView(
    bucketType: BucketBox.BucketType = BucketBox.BucketType.Unknown,
    bucketItemState: Int = 0,
    onClickBucketItemState: (Int) -> Unit = {}
) {

    val context = LocalContext.current

    val tabItemList: List<TabItem> by remember(key1 = bucketType) {
        derivedStateOf {
            listOf(
                TabItem(text = context.getString(bucketType.bucketTypeToAlphaText()), icon = R.drawable.ic_fa_clock, selectedIcon = R.drawable.ic_fa_clock_duotone) { onClickBucketItemState(0) },
                TabItem(text = context.getString(bucketType.bucketTypeToBetaText()), icon = bucketType.bucketTypeToBetaIcon(), selectedIcon = bucketType.bucketTypeToBetaSelectedIcon()) { onClickBucketItemState(1) },
                TabItem(text = context.getString(bucketType.bucketTypeToGammaText()), icon = R.drawable.ic_fa_circle_check, selectedIcon = R.drawable.ic_fa_circle_check_duotone) { onClickBucketItemState(2) },
            )
        }
    }

    GenericTabRow(
        tabItemList = tabItemList,
        selectedTabIndex = bucketItemState,
        hideUnselectedIcon = true,
        colors = TabDefaults.tabColors(containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground),
        modifier = Modifier.fillMaxWidth()
    )
}

