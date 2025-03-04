package com.syncodec.graphite.presentation.common.v2.bottomSheet2

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.GridScope
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.maxkeppeker.sheets.core.views.Grid
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.ui.AnimationDefaults
import com.syncodec.graphite.presentation.ui.ICON_SIZE


object BottomSheetActionButton {
    data class Colors(
        val containerColor: Color,
        val contentColor: Color,
        val checkedContainerColor: Color,
        val checkedContentColor: Color,
        val outlineColor: Color = Color.Transparent
    )

    object Defaults {
        @Composable
        fun colors(
            containerColor: Color = MaterialTheme.colorScheme.background,
            contentColor: Color = MaterialTheme.colorScheme.onBackground,
            checkedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
            checkedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
            outlineColor: Color = Color.Transparent
        ) = Colors(containerColor = containerColor, contentColor = contentColor, checkedContainerColor = checkedContainerColor, checkedContentColor = checkedContentColor, outlineColor = outlineColor)

        @Composable
        fun deleteColor(
            containerColor: Color = MaterialTheme.colorScheme.errorContainer,
            contentColor: Color = MaterialTheme.colorScheme.onErrorContainer,
            checkedContainerColor: Color = containerColor,
            checkedContentColor: Color = contentColor,
            outlineColor: Color = Color.Transparent
        ) = Colors(containerColor = containerColor, contentColor = contentColor, checkedContainerColor = checkedContainerColor, checkedContentColor = checkedContentColor, outlineColor = outlineColor)
    }

    @Composable
    fun Composable(
        icon: Int,
        text: String,
        itemInRow: Int = 4,
        colors: Colors = Defaults.colors(),
        checked: Boolean = false,
        onClick: () -> Unit
    ) {
        val containerColor by animateColorAsState(targetValue = if (checked == true) colors.checkedContainerColor else colors.containerColor, animationSpec = AnimationDefaults.stateAnimationSpec())
        val contentColor by animateColorAsState(targetValue = if (checked == true) colors.checkedContentColor else colors.contentColor, animationSpec = AnimationDefaults.stateAnimationSpec())
        val borderColor = colors.outlineColor

        Surface(
            color = containerColor,
            contentColor = contentColor,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(width = 1.dp, color = borderColor),
            checked = checked,
            onCheckedChange = { onClick() },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 4f / itemInRow)
                .padding(all = 4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.weight(weight = 1f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = text,
                        modifier = Modifier.requiredSize(size = 18.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(weight = 1f),
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(height = 6.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun BottomSheetActionGrid(
        itemInRow: Int = 4,
        content: @Composable () -> Unit
    ) {
        VerticalGrid(
            columns = SimpleGridCells.Fixed(count = itemInRow),
            modifier = Modifier.fillMaxWidth(),
        ) {
            content()
        }
    }

    @Composable
    fun EditButton(
        itemInRow: Int = 4,
        onClick: () -> Unit
    ) {
        Composable(
            icon = R.drawable.ic_fa_pencil,
            text = stringResource(id = R.string.edit),
            itemInRow = itemInRow,
            onClick = onClick
        )
    }

    @Composable
    fun ShareButton(
        itemInRow: Int = 4,
        onClick: () -> Unit
    ) {
        Composable(
            icon = R.drawable.ic_fa_share,
            text = stringResource(id = R.string.share),
            itemInRow = itemInRow,
            onClick = onClick
        )
    }

    @Composable
    fun DeleteButton(
        itemInRow: Int = 4,
        onClick: () -> Unit
    ) {
        Composable(
            icon = R.drawable.ic_fa_delete,
            text = stringResource(id = R.string.delete),
            colors = Defaults.deleteColor(),
            itemInRow = itemInRow,
            onClick = onClick
        )
    }
}
