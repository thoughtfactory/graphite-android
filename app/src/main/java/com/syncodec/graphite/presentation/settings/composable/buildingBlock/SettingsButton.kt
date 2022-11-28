package com.syncodec.graphite.presentation.settings.composable.buildingBlock

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BaseApplication
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.composable.ProTag


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsButton(
	title: String,
	icon: Int? = null,
	subTitle: String? = null,
	isProFeature: Boolean = false,
	containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.47f),
	contentColor: Color = MaterialTheme.colorScheme.onSurface,
	iconColor : Color = MaterialTheme.colorScheme.onSurface,
	enabled: Boolean = true,
	onClick: () -> Unit
) {
	val isPro by BaseApplication.isPro

	Card(
		colors = CardDefaults.cardColors(
			containerColor = containerColor,
			contentColor = contentColor,
			disabledContainerColor = containerColor.copy(alpha = 0.47f),
			disabledContentColor = contentColor.copy(alpha = 0.47f),
		),
		modifier = Modifier.padding(12.dp, 4.dp),
		enabled = enabled,
		onClick = onClick
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			if (icon != null) {
				Icon(
					painter = painterResource(id = icon),
					contentDescription = title,
					tint = iconColor,
					modifier = Modifier.requiredSize(24.dp)
				)
				Spacer(modifier = Modifier.width(16.dp))
			} else {
				Spacer(modifier = Modifier.width(40.dp))
			}

			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
				if (subTitle != null) {
					Spacer(modifier = Modifier.height(4.dp))
					Text(
						text = subTitle,
						style = MaterialTheme.typography.bodySmall,
					)
				}
			}

			if (isProFeature && !isPro) {
				Spacer(modifier = Modifier.width(16.dp))
				ProTag()
			}

			Spacer(modifier = Modifier.width(16.dp))

			Icon(
				painter = painterResource(id = R.drawable.ic_chevron_right),
				contentDescription = title,
				modifier = Modifier.requiredSize(24.dp)
			)
		}
	}
}
