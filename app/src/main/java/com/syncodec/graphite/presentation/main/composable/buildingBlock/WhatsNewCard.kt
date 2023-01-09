package com.syncodec.graphite.presentation.main.composable.buildingBlock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.utils.DataStoreInstance


@Preview
@Composable
fun WhatsNewCard(
	showCard : Boolean = true,
) {
	val context = LocalContext.current

	val dataStoreInstance = remember { DataStoreInstance(context = context) }
	val showWhatsNewCard by dataStoreInstance.getShowWhatsNewCard.collectAsState(initial = null)

	var cardHeight by remember { mutableStateOf(0) }

	val whatsNew = "• UI changes" +
			"\n• Fixed: Add bucket list" +
			"\n• Fixed: Minor bug fixes" +
			"\n• Added: Report bugs or provide suggestions from settings" +
			"\n• Added: Add note directly from notification" +
			"\n• Added: Reorderable bucket and notebook"

	AnimatedVisibility(
		visible = showCard && showWhatsNewCard == true,
		enter = expandVertically(tween(300)),
		exit = shrinkVertically(tween(300)),
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp, 0.dp, 12.dp, 0.dp),
		) {
			Spacer(modifier = Modifier.width(6.dp))
			Box(
				modifier = Modifier
					.width(4.dp)
					.height(with(LocalDensity.current) { cardHeight.toDp() })
					.background(MaterialTheme.colorScheme.surface)
			)
			Spacer(modifier = Modifier.width(10.dp))

			OutlinedCard(
				shape = MaterialTheme.shapes.medium,
				border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f)),
				colors = CardDefaults.outlinedCardColors(
					containerColor = MaterialTheme.colorScheme.background,
					contentColor = MaterialTheme.colorScheme.onBackground,
				),
				modifier = Modifier
					.fillMaxWidth()
					.onGloballyPositioned {
						cardHeight = it.size.height
					},
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(8.dp)
				) {
					Row(
						verticalAlignment = Alignment.Bottom,
						modifier = Modifier.fillMaxWidth()
					) {
						Text(
							text = "Whats New",
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Bold
						)

						Spacer(modifier = Modifier.weight(1f))

						Text(
							text = "Version ${BuildConfig.VERSION_NAME}",
							style = MaterialTheme.typography.bodyMedium,
							fontWeight = FontWeight.Bold
						)
					}

					Spacer(modifier = Modifier.height(4.dp))

					Text(
						text = whatsNew,
						style = MaterialTheme.typography.bodySmall,
					)

					Spacer(modifier = Modifier.height(8.dp))

					Button(
						onClick = { dataStoreInstance.putShowWhatsNewCard(false) },
						modifier = Modifier.fillMaxWidth()
					) {
						Text(text = "Dismiss")
					}
				}
			}
		}
	}
}
