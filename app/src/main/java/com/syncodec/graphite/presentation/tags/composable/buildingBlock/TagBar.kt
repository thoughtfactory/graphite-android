package com.syncodec.graphite.presentation.tags.composable.buildingBlock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.AddButton


@Preview
@Composable
fun TagBar(
	value: String = "",
	onValueChange: (String) -> Unit = {},
	onClickAddTag: () -> Unit = {},
) {
	OutlinedTextField(
		value = value,
		shape = MaterialTheme.shapes.medium,
		onValueChange = onValueChange,
		label = { Text(text = stringResource(id = R.string.tag)) },
		placeholder = { Text(text = stringResource(id = R.string.search_or_add_tag)) },
		trailingIcon = { AddButton(onClick = onClickAddTag)		},
		maxLines = 1,
		singleLine = true,
		keyboardOptions = KeyboardOptions(
			capitalization = KeyboardCapitalization.None,
			autoCorrect = true,
			keyboardType = KeyboardType.Text,
			imeAction = ImeAction.Done,
		),
		keyboardActions = KeyboardActions { onClickAddTag() },
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 24.dp)
	)
}
