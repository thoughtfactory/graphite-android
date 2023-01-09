package com.syncodec.graphite.presentation.common.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R


@Preview
@Composable
fun ExperimentalTag() {
	Box(
		modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(47))
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.padding(8.dp, 6.dp)
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_experimental),
				contentDescription = "Exp",
				tint = MaterialTheme.colorScheme.onPrimary,
				modifier = Modifier.requiredSize(16.dp)
			)

			Spacer(modifier = Modifier.width(4.dp))

			Text(
				text = "Exp",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onPrimary,
			)
		}
	}
}
