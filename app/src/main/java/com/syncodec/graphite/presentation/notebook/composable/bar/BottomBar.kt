package com.syncodec.graphite.presentation.notebook.composable.bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.custom.button.MenuButton


@Composable
fun BottomBar(
	openSheet: () -> Unit
) {
	LazyRow() {
		item { Spacer(modifier = Modifier.width(16.dp)) }
	}
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.height(80.dp)
			.background(MaterialTheme.colorScheme.surface),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(16.dp))

		MenuButton(
			icon = R.drawable.ic_share,
			contentDescription = "Share Bucket"
		) {}

		MenuButton(
			icon = R.drawable.ic_export,
			contentDescription = "Export Bucket"
		) {}

		Spacer(modifier = Modifier.weight(1f))
		Spacer(modifier = Modifier.width(16.dp))
		FloatingActionButton(
			onClick = { openSheet() },
			elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
		) {
			Icon(
				painter = painterResource(id = R.drawable.ic_add_2),
				contentDescription = "Add new item",
			)
		}
		Spacer(modifier = Modifier.width(16.dp))
	}
}
