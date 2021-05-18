package com.syncodec.momento.bucketItemComponent.thought

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Bulb

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
				imageVector = TablerIcons.Bulb,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary
			)
			Spacer(modifier = Modifier.width(16.dp))
			Text(
				text = "Add your thoughts",
				color = MaterialTheme.colorScheme.primary,
				style = MaterialTheme.typography.bodyLarge,
			)
		}
	}
}
