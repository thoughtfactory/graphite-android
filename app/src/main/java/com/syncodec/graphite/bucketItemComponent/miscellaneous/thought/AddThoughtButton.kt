package com.syncodec.graphite.bucketItemComponent.miscellaneous.thought

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
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
fun AddThoughtButton(
	onClick: () -> Unit = {}
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onClick() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_bulb),
				contentDescription = "Add your thought",
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.requiredSize(20.dp)
			)
			Spacer(modifier = Modifier.width(16.dp))
			Text(
				text = "Add your thoughts",
				color = MaterialTheme.colorScheme.onBackground,
				style = MaterialTheme.typography.bodyLarge,
			)
		}
	}
}
