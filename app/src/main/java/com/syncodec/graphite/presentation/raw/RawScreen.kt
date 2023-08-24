package com.syncodec.graphite.presentation.raw

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.ExpandableBox
import com.syncodec.graphite.presentation.common.button.GenericButton
import org.json.JSONArray
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RawScreen(
	jsonObject : JSONObject,
	onClickBack: () -> Unit
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		TopAppBar(
			navigationIcon = {
				GenericButton(
					icon = R.drawable.ic_back,
					onClick = onClickBack
				)
			},
			title = {
				Text(
					text = "Raw",
					fontWeight = FontWeight.Bold
				)
			},
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.background,
				navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
				titleContentColor = MaterialTheme.colorScheme.onBackground
			)
		)

		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(rememberScrollState())
		) {
			Spacer(modifier = Modifier.height(0.dp))
			ContentView(jsonObject = jsonObject, indent = 0)
			Spacer(modifier = Modifier.height(16.dp))
		}
	}
}

@Composable
private fun ContentView(
	jsonObject : JSONObject,
	indent : Int
) {
	jsonObject.keys().forEach {
		val value = jsonObject.get(it)
		when (value) {
			is JSONObject -> ContentView(jsonObject = value, indent = indent + 1)
			is JSONArray -> null
			else -> EdgeView(key = it, value = value, indent = indent)
		}
	}
}

@Composable
private fun EdgeView(
	key : String,
	value : Any,
	indent : Int
) {

	var isExpanded by remember { mutableStateOf(true) }

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 4.dp)
			.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
			.clip(RoundedCornerShape(12.dp))
			.clickable { isExpanded = ! isExpanded }
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			Text(
				text = key,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.71f),
				fontWeight = FontWeight.Bold
			)

			Spacer(modifier = Modifier.height(8.dp))

			ExpandableBox(
				isVisible = isExpanded
			) {
				Text(
					text = value.toString(),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
				)
			}
		}
	}
}
