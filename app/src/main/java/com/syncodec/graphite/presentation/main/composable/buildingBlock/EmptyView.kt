package com.syncodec.graphite.presentation.main.composable.buildingBlock

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R


@Composable
fun EmptyView(
	modifier: Modifier = Modifier,
	image: Int,
	title: String,
	subTitle: String? = null,
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
		modifier = modifier
	) {
		Spacer(modifier = Modifier.weight(1f))
		Image(
			painter = painterResource(id = image),
			contentDescription = "No items found",
			modifier = Modifier.size(screenWidth * 3 / 4)
		)

		Spacer(modifier = Modifier.height(16.dp))

		Text(
			text = title,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			fontSize = 14.sp,
			lineHeight = 16.sp,
			letterSpacing = 2.sp,
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center,
			modifier = Modifier.width(screenWidth * 3 / 4)
		)

		subTitle?.let {
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = it,
				fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
				fontWeight = FontWeight.Bold,
				fontSize = 14.sp,
				lineHeight = 16.sp,
				letterSpacing = 2.sp,
				color = MaterialTheme.colorScheme.onBackground,
				textAlign = TextAlign.End,
				modifier = Modifier.width(screenWidth * 3 / 4)
			)
		}

		Spacer(modifier = Modifier.weight(1f))

		Spacer(modifier = Modifier.height(64.dp))
	}
}
