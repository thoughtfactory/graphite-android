package com.syncodec.graphite.presentation.custom.bottomSheet

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.utils.tone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetGridCard(
	modifier: Modifier,
	title: String,
	subtitle: String? = null,
	icon: Int,
	onClick: () -> Unit
) {
	Card(
		onClick = { onClick() },
		shape = RoundedCornerShape(12.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), -3),
			contentColor = MaterialTheme.colorScheme.onSurface
		),
		modifier = modifier
	) {
		Column(
			modifier = Modifier.fillMaxWidth(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Spacer(modifier = Modifier.height(8.dp))

			Icon(
				painter = painterResource(id = icon),
				contentDescription = title,
				modifier = Modifier.requiredSize(24.dp)
			)

			Spacer(modifier = Modifier.height(4.dp))

			Text(
				text = title,
				style = MaterialTheme.typography.bodyMedium,
			)
			if (subtitle != null) {
				Spacer(modifier = Modifier.width(4.dp))
				Text(
					text = subtitle,
					style = MaterialTheme.typography.bodyMedium,
					fontWeight = FontWeight.Bold
				)
			}

			Spacer(modifier = Modifier.height(8.dp))
		}
	}
}
