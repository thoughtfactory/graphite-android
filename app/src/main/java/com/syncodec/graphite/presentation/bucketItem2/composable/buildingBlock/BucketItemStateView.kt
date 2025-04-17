package com.syncodec.graphite.presentation.bucketItem2.composable.buildingBlock

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.MultiChoiceSegmentedButtonRowScope
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.modelObjectBox.BucketBoxEncrypted
import com.syncodec.graphite.di.modelObjectBox.BucketItemBoxDecrypted
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToAlphaText
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToBetaIcon
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToBetaSelectedIcon
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToBetaText
import com.syncodec.graphite.di.modelObjectBox.bucketTypeToGammaText
import com.syncodec.graphite.presentation.common.shape.AbsoluteSmoothCornerShape


//@Composable
//fun BucketItemStateView(
//    modifier: Modifier = Modifier,
//    bucketType: BucketBoxEncrypted.BucketType = BucketBoxEncrypted.BucketType.Unknown,
//    bucketItemState: Int = 0,
//    onToggleBucketItemState: (BucketItemBoxDecrypted.State) -> Unit = {}
//) {
//
//    val context = LocalContext.current
//
//    val tabItemList: List<TabItem> by remember(key1 = bucketType) {
//        derivedStateOf {
//            listOf(
//                TabItem(
//                    text = context.getString(bucketType.bucketTypeToAlphaText()),
//                    icon = R.drawable.ic_fa_clock, selectedIcon = R.drawable.ic_fa_clock_duotone
//                ) { onToggleBucketItemState(BucketItemBoxDecrypted.State.Alpha) },
//                TabItem(
//                    text = context.getString(bucketType.bucketTypeToBetaText()),
//                    icon = bucketType.bucketTypeToBetaIcon(), selectedIcon = bucketType.bucketTypeToBetaSelectedIcon())
//                { onToggleBucketItemState(BucketItemBoxDecrypted.State.Beta) },
//                TabItem(
//                    text = context.getString(bucketType.bucketTypeToGammaText()),
//                    icon = R.drawable.ic_fa_circle_check, selectedIcon = R.drawable.ic_fa_circle_check_duotone
//                ) { onToggleBucketItemState(BucketItemBoxDecrypted.State.Gamma) },
//            )
//        }
//    }
//
//    GenericTabRow(
//        tabItemList = tabItemList,
//        selectedTabIndex = bucketItemState,
//        hideUnselectedIcon = true,
//        colors = TabDefaults.tabColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
//        modifier = modifier.fillMaxWidth()
//    )
//}


@Composable
fun BucketItemStateView(
    modifier: Modifier = Modifier,
    bucketType: BucketBoxEncrypted.BucketType = BucketBoxEncrypted.BucketType.Unknown,
    bucketItemState: Int = 0,
    onToggleBucketItemState: (BucketItemBoxDecrypted.State) -> Unit = {}
) {

    val context = LocalContext.current

//    val tabItemList: List<TabItem> by remember(key1 = bucketType) {
//        derivedStateOf {
//            listOf(
//                TabItem(text = context.getString(bucketType.bucketTypeToAlphaText()), icon = R.drawable.ic_fa_clock, selectedIcon = R.drawable.ic_fa_clock_duotone) { onClickBucketItemState(0) },
//                TabItem(text = context.getString(bucketType.bucketTypeToBetaText()), icon = bucketType.bucketTypeToBetaIcon(), selectedIcon = bucketType.bucketTypeToBetaSelectedIcon()) { onClickBucketItemState(1) },
//                TabItem(text = context.getString(bucketType.bucketTypeToGammaText()), icon = R.drawable.ic_fa_circle_check, selectedIcon = R.drawable.ic_fa_circle_check_duotone) { onClickBucketItemState(2) },
//            )
//        }
//    }

//    GenericTabRow(
//        tabItemList = tabItemList,
//        selectedTabIndex = bucketItemState,
//        hideUnselectedIcon = true,
//        colors = TabDefaults.tabColors(containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.onBackground),
//        modifier = Modifier.fillMaxWidth()
//    )

    MultiChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth(),
    ) {
        SegmentedButton(
            icon = R.drawable.ic_fa_clock,
            iconChecked = R.drawable.ic_fa_clock_duotone,
            text = context.getString(bucketType.bucketTypeToAlphaText()),
            shape = AbsoluteSmoothCornerShape(cornerRadiusTL = 12.dp, smoothnessAsPercentTL = 100, cornerRadiusBL = 12.dp, smoothnessAsPercentBL = 100),
            checked = bucketItemState == 0,
            onCheckedChange = { onToggleBucketItemState(BucketItemBoxDecrypted.State.Alpha) },
        )
        SegmentedButton(
            icon = bucketType.bucketTypeToBetaIcon(),
            iconChecked = bucketType.bucketTypeToBetaSelectedIcon(),
            text = context.getString(bucketType.bucketTypeToBetaText()),
            shape = RectangleShape,
            checked = bucketItemState == 1,
            onCheckedChange = { onToggleBucketItemState(BucketItemBoxDecrypted.State.Beta) },
        )
        SegmentedButton(
            icon = R.drawable.ic_fa_circle_check,
            iconChecked = R.drawable.ic_fa_circle_check_duotone,
            text = context.getString(bucketType.bucketTypeToGammaText()),
            shape = AbsoluteSmoothCornerShape(cornerRadiusTR = 12.dp, smoothnessAsPercentTR = 100, cornerRadiusBR = 12.dp, smoothnessAsPercentBR = 100),
            checked = bucketItemState == 2,
            onCheckedChange = { onToggleBucketItemState(BucketItemBoxDecrypted.State.Gamma) },
        )
    }
}

@Composable
private fun MultiChoiceSegmentedButtonRowScope.SegmentedButton(
    icon: Int,
    iconChecked: Int,
    text: String,
    shape: Shape,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    val color = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        inactiveContainerColor = MaterialTheme.colorScheme.background,
        inactiveContentColor = MaterialTheme.colorScheme.onBackground
    )

    SegmentedButton(
        checked = checked,
        onCheckedChange = { onCheckedChange() },
        shape = shape,
        modifier = Modifier.weight(weight = 1f),
        colors = color,
        icon = { Icon(painter = painterResource(id = if (checked) iconChecked else icon), contentDescription = text, modifier = Modifier.requiredSize(size = 18.dp)) },
        label = { Text(text = text) }
    )
}

