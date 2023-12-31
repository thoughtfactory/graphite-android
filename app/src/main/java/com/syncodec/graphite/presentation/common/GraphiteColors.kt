package com.syncodec.graphite.presentation.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable


@Composable
fun getGraphiteTextFieldColors() = OutlinedTextFieldDefaults.colors(
	unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
	unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.42f),
	unfocusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.42f),
	unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.69f),
	focusedTextColor = MaterialTheme.colorScheme.onBackground,
	focusedLabelColor = MaterialTheme.colorScheme.onBackground,
	focusedPlaceholderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.69f),
	focusedBorderColor = MaterialTheme.colorScheme.onBackground,
)
