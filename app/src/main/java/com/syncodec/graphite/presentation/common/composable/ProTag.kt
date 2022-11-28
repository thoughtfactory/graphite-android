package com.syncodec.graphite.presentation.common.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R


@Composable
fun ProTag() {
	Box(
		modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(47))
	) {
		Text(
			text = "PRO",
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			fontSize = 16.sp,
			lineHeight = 18.sp,
			letterSpacing = 2.sp,
			color = MaterialTheme.colorScheme.onPrimary,
			modifier = Modifier.padding(8.dp, 6.dp)
		)
	}
}
