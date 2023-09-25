package com.syncodec.graphite.presentation.common.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.ICON_BUTTON_SIZE


@Immutable
class InfoCardColors constructor(
	val containerColor : Color,
	val contentColor : Color,
) {
	override fun hashCode() : Int {
		var result = containerColor.hashCode()
		result = 31 * result + contentColor.hashCode()
		return result
	}

	override fun equals(other : Any?) : Boolean {
		if (this === other) return true
		if (other !is InfoCardColors) return false

		if (containerColor != other.containerColor) return false
		if (contentColor != other.contentColor) return false

		return true
	}
}

object InfoCardDefaults {
	@Composable
	fun infoCardColors(
		containerColor : Color = Color(0xFF82AAE3),
		contentColor : Color = Color.White,
	) : InfoCardColors = InfoCardColors(
		containerColor = containerColor,
		contentColor = contentColor,
	)

	@Composable
	fun warningCardColors(
		containerColor : Color = MaterialTheme.colorScheme.errorContainer,
		contentColor : Color = MaterialTheme.colorScheme.onErrorContainer,
	) : InfoCardColors = InfoCardColors(
		containerColor = containerColor,
		contentColor = contentColor,
	)

	@Composable
	fun errorCardColors(
		containerColor : Color = MaterialTheme.colorScheme.error,
		contentColor : Color = MaterialTheme.colorScheme.onError,
	) : InfoCardColors = InfoCardColors(
		containerColor = containerColor,
		contentColor = contentColor,
	)
}

@Preview
@Composable
fun InfoCard(
	modifier : Modifier = Modifier,
	title : String = "Info Card",
	description : String = "This is an info card. It can be used to display information to the user.",
	icon : Int = R.drawable.ic_info,
	colors : InfoCardColors = InfoCardDefaults.infoCardColors(),
	shape : Shape = MaterialTheme.shapes.medium,
	buttonText : String = "Learn more",
	onClickButton : (() -> Unit)? = null,
) {
	Card(
		shape = shape,
		colors = CardDefaults.cardColors(
			containerColor = colors.containerColor,
			contentColor = colors.contentColor,
		),
		modifier = modifier
			.fillMaxWidth()
			.padding(0.dp, 4.dp),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.fillMaxWidth(),
			) {
				Icon(
					painter = painterResource(id = icon),
					contentDescription = title,
					modifier = Modifier.requiredSize(ICON_BUTTON_SIZE)
				)
				Spacer(modifier = Modifier.width(12.dp))
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
			}
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				text = description,
				style = MaterialTheme.typography.bodyMedium,
			)

			onClickButton?.let {
				Spacer(modifier = Modifier.height(24.dp))

				Button(
					shape = MaterialTheme.shapes.medium,
					colors = ButtonDefaults.buttonColors(
						containerColor = colors.contentColor,
						contentColor = colors.containerColor,
					),
					modifier = Modifier.fillMaxWidth(),
					onClick = it,
				) {
					Text(text = buttonText)
				}
			}
		}
	}
}
