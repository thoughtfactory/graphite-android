package com.syncodec.graphite.custom

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

data class ChipData(
	val title: String,
	val icon: Int,
	val isSelected: Boolean,
	val onClick: (() -> Unit)? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipView(
	chipDataList: List<ChipData>,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState()),
		verticalAlignment = Alignment.CenterVertically
	) {
		Spacer(modifier = Modifier.width(12.dp))
		chipDataList.forEach {
			FilterChip(
				selected = it.isSelected,
				onClick = { it.onClick?.let { it1 -> it1() } },
				leadingIcon = {
					Icon(
						painter = painterResource(id = it.icon),
						contentDescription = it.title,
						modifier = Modifier.requiredSize(16.dp)
					)
				},
				selectedIcon = {
					Icon(
						painter = painterResource(id = it.icon),
						contentDescription = it.title,
						modifier = Modifier.requiredSize(16.dp)
					)
				},
				label = {
					Text(
						text = it.title,
						style = MaterialTheme.typography.bodyMedium,
					)
				}
			)
			Spacer(modifier = Modifier.width(8.dp))
		}
		Spacer(modifier = Modifier.width(4.dp))
	}
}
