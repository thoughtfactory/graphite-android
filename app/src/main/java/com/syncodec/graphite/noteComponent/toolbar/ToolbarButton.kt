package com.syncodec.graphite.noteComponent.toolbar

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.miscellaneous.ThemeUtils.Companion.tone
import com.syncodec.graphite.R


@Composable
fun ToolbarButton(
	name: String,
	icon: Int,
	highlight: Boolean,
	isEnabled: Boolean,
	onClick: () -> Unit
) {
	val context = LocalContext.current
	val containerColor by animateColorAsState(
		if (highlight)
			contentColorFor(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))
		else
			MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
	)
	val contentColor by animateColorAsState(
		if (highlight)
			MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)
		else
			contentColorFor(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))
	)
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center,
		modifier = Modifier
			.requiredSize(48.dp)
			.padding(2.dp)
			.clip(RoundedCornerShape(25, 25, if (isEnabled) (25) else 0, 25))
			.background(containerColor)
			.clickable {
				if (isEnabled) {
					onClick()
				} else {
					Toast
						.makeText(
							context,
							"Subscribe to Graphite Premium to unlock rich text",
							Toast.LENGTH_SHORT
						)
						.show()
				}
			},
	) {
		Spacer(modifier = Modifier.width(2.dp))
		Box(
			modifier = Modifier.fillMaxWidth()
		) {
			Icon(
				painter = painterResource(id = icon),
				contentDescription = name,
				tint = contentColor,
				modifier = Modifier
					.requiredSize(40.dp)
					.padding(6.dp)
			)

			if (!isEnabled) {
				Box(
					contentAlignment = Alignment.BottomEnd,
					modifier = Modifier.requiredSize(40.dp)
				) {
					Icon(
						painter = painterResource(id = R.drawable.ic_premium),
						contentDescription = name,
						tint = Color.Unspecified,
						modifier = Modifier
							.requiredSize(16.dp)
							.padding(2.dp, 2.dp, 0.dp, 0.dp)
					)
				}
			}
		}
		Spacer(modifier = Modifier.width(2.dp))
	}
}
