package com.syncodec.momento.bucketComponent.miscellaneous

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.syncodec.momento.bucketComponent.BucketActivity
import com.syncodec.momento.R


@Composable
fun DataTypeSelectDropdownDemo(
	isVisible: Boolean,
	onClick: (BucketActivity.DataType?) -> Unit
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Box(
		modifier = Modifier
			.fillMaxSize()
	) {
		DropdownMenu(
			expanded = isVisible,
			onDismissRequest = { onClick(null) },
			modifier = Modifier
				.background(MaterialTheme.colorScheme.background),
			offset = DpOffset(screenWidth, 0.dp)
		) {
			DropdownMenuItem(
				onClick = { onClick(BucketActivity.DataType.TV) },
				leadingIcon = {
					Icon(
						painter = painterResource(id = R.drawable.ic_tv),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.requiredSize(20.dp)
					)
				},
				text = {
					Text(
						text = "Tv show",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
					)
				}
			)

			DropdownMenuItem(
				onClick = { onClick(BucketActivity.DataType.MOVIE) },
				leadingIcon = {
					Icon(
						painter = painterResource(id = R.drawable.ic_show),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier.requiredSize(20.dp)
					)
				},
				text = {
					Text(
						text = "Movie",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Bold,
					)
				}
			)
		}
	}
}
