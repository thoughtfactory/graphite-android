package com.syncodec.momento.bucketItemComponent.thought

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.X


@Preview
@Composable
fun ThoughtController(
	onSave: () -> Unit = {},
	onDiscard: () -> Unit = {}
) {
	Row(
		modifier = Modifier
	) {
		Box(
			modifier = Modifier
				.weight(1f)
				.padding(4.dp)
				.clip(RoundedCornerShape(12.dp))
				.clickable { onDiscard() },
			contentAlignment = Alignment.Center
		) {
			Row(
				modifier = Modifier
					.height(32.dp)
					.padding(4.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Center
			) {
				Icon(
					imageVector = TablerIcons.X,
					contentDescription = "Discard",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier
						.requiredSize(16.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "Discard",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground
				)
			}
		}

		Box(
			modifier = Modifier
				.weight(1f)
				.padding(4.dp)
				.clip(RoundedCornerShape(12.dp))
				.background(MaterialTheme.colorScheme.primaryContainer)
				.clickable { onSave() },
			contentAlignment = Alignment.Center
		) {
			Row(
				modifier = Modifier
					.height(32.dp)
					.padding(4.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.Center
			) {
				Icon(
					imageVector = TablerIcons.Check,
					contentDescription = "Save",
					tint = MaterialTheme.colorScheme.onPrimaryContainer,
					modifier = Modifier
						.requiredSize(16.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = "Save",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onPrimaryContainer
				)
			}
		}
	}
}
