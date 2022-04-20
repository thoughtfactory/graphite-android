package com.syncodec.momento.bucketItemComponent.miscellaneous.thought

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.momento.R


@Composable
fun ThoughtController(
	onSave: () -> Unit,
	onDiscard: () -> Unit,
	onDelete: (() -> Unit)?
) {
	Row(
		modifier = Modifier
	) {
		Spacer(modifier = Modifier.width(4.dp))

		Button(
			onClick = { onDiscard() },
			colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
			modifier = Modifier.weight(1f),
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_cancel),
				contentDescription = "Discard",
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.requiredSize(20.dp)
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = "Discard",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground
			)
		}

		Button(
			onClick = { onSave() },
			colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
			modifier = Modifier.weight(1f),
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_done),
				contentDescription = "Save",
				tint = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.requiredSize(20.dp)
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = "Save",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
		}

		Spacer(modifier = Modifier.width(4.dp))

		if (onDelete!=null) {
			IconButton(onClick = { onDelete.invoke() }) {
				Icon(
					painter = painterResource(id = R.drawable.ic_trash),
					contentDescription = "Delete",
					tint = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.requiredSize(20.dp)
				)
			}
		}

		Spacer(modifier = Modifier.width(4.dp))
	}
}
