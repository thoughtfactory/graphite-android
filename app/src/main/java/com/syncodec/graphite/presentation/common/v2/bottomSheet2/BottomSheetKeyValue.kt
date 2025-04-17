package com.syncodec.graphite.presentation.common.v2.bottomSheet2

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


//object BottomSheetKeyValue {
//    data class Colors(
//        val containerColor: Color,
//        val contentColor: Color,
//        val outlineColor: Color = Color.Transparent
//    )
//
//    object Defaults {
//        @Composable
//        fun colors(
//            containerColor: Color = MaterialTheme.colorScheme.background,
//            contentColor: Color = MaterialTheme.colorScheme.onBackground,
//            outlineColor: Color = Color.Transparent
//        ) = Colors(containerColor = containerColor, contentColor = contentColor, outlineColor = outlineColor)
//    }
//
//    @Composable
//    fun Composable(
//        key: String,
//        value: String?,
//        colors: Colors = Defaults.colors(),
//    ) {
//        Surface(
//            color = colors.containerColor,
//            contentColor = colors.contentColor,
//            shape = MaterialTheme.shapes.medium,
//            border = BorderStroke(width = 1.dp, color = colors.outlineColor),
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 4.dp, vertical = 2.dp)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 12.dp, vertical = 12.dp)
//            ) {
//                Text(
//                    text = key,
//                    style = MaterialTheme.typography.labelMedium,
//                    fontWeight = FontWeight.Bold,
//                    color = colors.contentColor.copy(alpha = 0.71f)
//                )
//                Spacer(modifier = Modifier.height(height = 4.dp))
//                Text(
//                    text = if (value.isNullOrBlank()) stringResource(id = R.string.no_data) else value,
//                    style = MaterialTheme.typography.bodySmall,
//                    fontStyle = if (value.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//    }
//}

