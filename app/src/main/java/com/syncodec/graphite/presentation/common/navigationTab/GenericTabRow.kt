package com.syncodec.graphite.presentation.common.navigationTab

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.AnimationDefaults


@Preview
@Composable
fun GenericTabRow(
    modifier: Modifier = Modifier,
    tabItemList: List<TabItem> = listOf(),
    selectedTabIndex: Int = 0,
    hideUnselectedText: Boolean = false,
    hideUnselectedIcon: Boolean = false,
    colors: TabColors = TabDefaults.tabColors(),
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val indicator = @Composable { tabPositions: List<TabPosition> ->
        TabIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
            tabItem = tabItemList[selectedTabIndex],
            containerColor = colors.selectedContainerColor,
            contentColor = colors.selectedContentColor,
            textStyle = textStyle,
            fontWeight = fontWeight
        )
    }

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = colors.containerColor,
        contentColor = colors.contentColor,
        indicator = indicator,
        divider = {},
        modifier = modifier
            .height(height = 40.dp)
            .clip(shape = MaterialTheme.shapes.medium)
    ) {
        tabItemList.forEachIndexed { index, tabItem ->
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(height = 40.dp)
                    .clickable { tabItem.onClick() }
            ) {
                if (!hideUnselectedIcon) tabItem.icon?.let {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = tabItem.text,
                        tint = colors.contentColor,
                        modifier = Modifier.requiredSize(16.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (!hideUnselectedText) Text(
                    text = tabItem.text,
                    style = textStyle,
                    color = colors.contentColor,
                    fontWeight = fontWeight,
                )
            }
        }
    }
}

@Preview
@Composable
fun TabIndicator(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    tabItem: TabItem = TabItem(text = "text", icon = R.drawable.ic_fa_note) {},
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Box(
        modifier
            .padding(all = 4.dp)
            .fillMaxSize()
            .background(color = containerColor, shape = MaterialTheme.shapes.medium),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = tabItem,
            transitionSpec = { AnimationDefaults.Fade },
            label = "text_animation",
        ) { tabItem1 ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                tabItem1.selectedIcon?.let {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = tabItem1.text,
                        tint = contentColor,
                        modifier = Modifier.requiredSize(size = 16.dp),
                    )
                    Spacer(modifier = Modifier.width(width = 8.dp))
                }
                Text(
                    text = tabItem1.text,
                    style = textStyle,
                    color = contentColor,
                    fontWeight = fontWeight,
                )
            }
        }
    }
}

data class TabItem(
    val text: String,
    val icon: Int? = null,
    val selectedIcon: Int? = icon,
    val onClick: () -> Unit = {},
)

@Immutable
data class TabColors(
    val containerColor: Color,
    val contentColor: Color,
    val selectedContainerColor: Color,
    val selectedContentColor: Color,
) {
    @Composable
    internal fun containerColor(selected: Boolean): State<Color> = rememberUpdatedState(if (selected) selectedContainerColor else containerColor)

    @Composable
    internal fun contentColor(selected: Boolean): State<Color> = rememberUpdatedState(if (selected) selectedContentColor else contentColor)

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + contentColor.hashCode()
        result = 31 * result + selectedContainerColor.hashCode()
        result = 31 * result + selectedContentColor.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TabColors

        if (containerColor != other.containerColor) return false
        if (contentColor != other.contentColor) return false
        if (selectedContainerColor != other.selectedContainerColor) return false
        return selectedContentColor == other.selectedContentColor
    }
}

object TabDefaults {
    @Composable
    fun tabColors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp).copy(alpha = 0.4f),
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
        selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    ): TabColors = TabColors(
        containerColor = containerColor,
        contentColor = contentColor,
        selectedContainerColor = selectedContainerColor,
        selectedContentColor = selectedContentColor,
    )
}

