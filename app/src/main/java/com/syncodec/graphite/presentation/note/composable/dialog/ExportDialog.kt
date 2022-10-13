package com.syncodec.graphite.presentation.note.composable.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.common.dialog.ColorPickerDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DialogTextField
import com.syncodec.graphite.utils.getRandomColor


@Composable
fun ExportDialog(
	showDialog: Boolean,
	onDismiss: () -> Unit,
) {
	Box(
		modifier = Modifier.fillMaxSize()
	) {
		AnimatedVisibility(
			visible = showDialog,
			enter = fadeIn(tween(300)),
			exit = fadeOut(tween(300)),
			modifier = Modifier.fillMaxSize()
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.background)
					.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
			) {
				Column(
					modifier = Modifier.fillMaxSize()
				) {
					TagTopAppBar(onDismiss = onDismiss)

					Spacer(modifier = Modifier.height(16.dp))


				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagTopAppBar(
	onDismiss : () -> Unit
) {
	TopAppBar(
		modifier = Modifier,
		navigationIcon = {
			MenuButton(
				icon = R.drawable.ic_close,
				tint = MaterialTheme.colorScheme.onBackground,
				onClick = onDismiss
			)
		},
		title = {
			Text(
				text = "Export Note",
				color = MaterialTheme.colorScheme.onBackground,
				fontWeight = FontWeight.Bold
			)
		},
		colors = TopAppBarDefaults.smallTopAppBarColors(
			containerColor = MaterialTheme.colorScheme.background,
			navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
			titleContentColor = MaterialTheme.colorScheme.onBackground,
		),
	)
}
