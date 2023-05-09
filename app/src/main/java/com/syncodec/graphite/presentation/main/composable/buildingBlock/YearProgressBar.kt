package com.syncodec.graphite.presentation.main.composable.buildingBlock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.animation.AnimatedScrollText
import com.syncodec.graphite.utils.dataStore.DataStoreInstance
import java.util.Calendar


@Preview
@Composable
fun YearProgressBar(
	showCard : Boolean = true,
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val isYearProgressEnabled by dataStoreInstance.getYearProgress.collectAsState(initial = null)

	var startAnimation by remember { mutableStateOf(false) }
	val calendar = remember { Calendar.getInstance() }
	val totalDays = remember { if (calendar.get(Calendar.YEAR) % 4 == 0) 366 else 365 }
	val progress by animateIntAsState(
		targetValue = if (startAnimation) calendar.get(Calendar.DAY_OF_YEAR) else 0,
		animationSpec = tween(2400)
	)

	LaunchedEffect(key1 = startAnimation) { startAnimation = true }

	AnimatedVisibility(
		visible = showCard && isYearProgressEnabled == true,
		enter = expandVertically(tween(300)),
		exit = shrinkVertically(tween(300)),
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			Card(
				colors = CardDefaults.cardColors(Color.Transparent),
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp, 8.dp, 12.dp, 4.dp),
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = calendar.get(Calendar.YEAR).toString(),
						fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
						fontWeight = FontWeight.Bold,
						fontSize = 20.sp,
						lineHeight = 24.sp,
						letterSpacing = 2.sp,
						color = MaterialTheme.colorScheme.onBackground
					)

					Spacer(modifier = Modifier.width(12.dp))

					Box(
						modifier = Modifier
							.weight(1f)
							.background(Color.Transparent)
							.clip(MaterialTheme.shapes.small)
							.border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small),
					) {
						Box(
							modifier = Modifier
								.fillMaxWidth(progress.toFloat() / totalDays)
								.height(12.dp)
								.background(MaterialTheme.colorScheme.primary)
						)
					}

					Spacer(modifier = Modifier.width(12.dp))

					AnimatedScrollText(
						animatedText = (progress.toFloat() * 100 / totalDays).toInt().toString(),
						staticText = "%",
						color = MaterialTheme.colorScheme.onBackground
					)
				}
			}

			Spacer(modifier = Modifier.height(8.dp))
		}
	}
}
