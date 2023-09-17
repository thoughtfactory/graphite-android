package com.syncodec.graphite.presentation.common.bottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.base.IconButtonSize


@Preview
@Composable
fun GenericBottomSheet(
	title : String = "Title",
	icon : Int = R.drawable.ic_menu,
	subTitle : String? = null,
	enableScroll : Boolean = false,
	content : @Composable ColumnScope.() -> Unit = {},
) {

	@Composable
	fun ColumnScope.innerContent() {
		Box(
			modifier = Modifier
				.width(48.dp)
				.height(4.dp)
				.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.31f), MaterialTheme.shapes.extraSmall)
		)

		Spacer(modifier = Modifier.height(12.dp))

		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.fillMaxWidth()
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold
			)

			Spacer(modifier = Modifier.weight(1f))

			Icon(
				painter = painterResource(id = icon),
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier.requiredSize(IconButtonSize)
			)
		}

		subTitle?.let {
			Text(
				text = it,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onBackground,
				maxLines = 2,
				modifier = Modifier.fillMaxWidth()
			)
		}

		Spacer(modifier = Modifier.height(12.dp))

		content()
	}

	if (enableScroll) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
		) {
			innerContent()
		}
	} else {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.fillMaxWidth()
		) {
			innerContent()
		}
	}
}
