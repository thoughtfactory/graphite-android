package com.syncodec.graphite.presentation.bugReport.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bugReport.BugReportActivity
import com.syncodec.graphite.presentation.bugReport.composable.bar.TopBar
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.ui.IconButtonSize


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Preview
@Composable
fun BugReportScreen(
	onClickBack : () -> Unit = {},
	onClickSubmit : (BugReportActivity.Companion.BugType, BugReportActivity.Companion.BugComponent, String, String) -> Unit = { _, _, _, _ -> }
) {

	var selectedBugType by remember { mutableStateOf<BugReportActivity.Companion.BugType?>(null) }
	var selectedBugComponent by remember { mutableStateOf<BugReportActivity.Companion.BugComponent?>(null) }

	var bugTitle by remember { mutableStateOf("") }
	var bugDescription by remember { mutableStateOf("") }

	var isBugTypeExpanded by remember { mutableStateOf(false) }
	var isBugComponentExpanded by remember { mutableStateOf(false) }

	GenericScaffold(
		topBar = { TopBar(onClickBack = onClickBack) },
	) {
		BugTypeMenu(
			selectedBugType = selectedBugType,
			onBugTypeSelected = { selectedBugType = it; isBugTypeExpanded = false },
			expanded = isBugTypeExpanded,
			onDismiss = { isBugTypeExpanded = false },
		)
		BugComponentMenu(
			selectedBugComponent = selectedBugComponent,
			onBugComponentSelected = { selectedBugComponent = it; isBugComponentExpanded = false },
			expanded = isBugComponentExpanded,
			onDismiss = { isBugComponentExpanded = false },
		)

		Column(
			modifier = Modifier.fillMaxSize()
		) {
			BugTypeSelector(
				selectedBugType = selectedBugType,
				onClick = { isBugTypeExpanded = true },
			)
			Spacer(modifier = Modifier.height(8.dp))

			BugComponentSelector(
				selectedBugComponent = selectedBugComponent,
				onClick = { isBugComponentExpanded = true },
			)
			Spacer(modifier = Modifier.height(4.dp))

			OutlinedTextField(
				value = bugTitle,
				onValueChange = { bugTitle = it },
				label = { Text(text = "Title") },
				placeholder = { Text(text = "Keep it simple") },
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp, 0.dp),
			)
			Spacer(modifier = Modifier.height(4.dp))

			OutlinedTextField(
				value = bugDescription,
				onValueChange = { bugDescription = it },
				label = { Text(text = "Description") },
				placeholder = { Text(text = "An overview") },
				shape = MaterialTheme.shapes.medium,
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp, 0.dp)
					.weight(1f)
			)
			Spacer(modifier = Modifier.height(8.dp))

			Button(
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.primary,
					contentColor = MaterialTheme.colorScheme.onPrimary,
				),
				modifier = Modifier
					.fillMaxWidth()
					.padding(12.dp, 0.dp),
				onClick = {
					onClickSubmit(
						selectedBugType ?: BugReportActivity.Companion.BugType.OTHER,
						selectedBugComponent ?: BugReportActivity.Companion.BugComponent.OTHER,
						bugTitle,
						bugDescription,
					)
				}
			) {
				Text(text = "Submit")
			}

			Spacer(modifier = Modifier.height(8.dp))
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
private fun BugTypeSelector(
	selectedBugType : BugReportActivity.Companion.BugType? = null,
	onClick : () -> Unit = {},
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 0.dp)
			.border(1.dp, MaterialTheme.colorScheme.onBackground, MaterialTheme.shapes.medium)
			.clip(MaterialTheme.shapes.medium)
			.clickable { onClick() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			AnimatedContent(
				targetState = selectedBugType,
				modifier = Modifier.weight(1f),
				transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) }
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.fillMaxWidth()
				) {
					Icon(
						painter = painterResource(id = BugReportActivity.bugTypeIconMap.getOrDefault(it, R.drawable.ic_exclamation)),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier
							.requiredSize(IconButtonSize)
							.padding(2.dp)
					)
					Spacer(modifier = Modifier.width(8.dp))
					Text(
						text = BugReportActivity.bugTypeNameMap.getOrDefault(it, "Select Report Type"),
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier
					)
				}
			}
			Spacer(modifier = Modifier.width(12.dp))
			Icon(
				painter = painterResource(id = R.drawable.ic_caret),
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier
					.requiredSize(IconButtonSize)
					.graphicsLayer { rotationZ = if (selectedBugType == null) 0f else 180f }
			)
		}
	}
}

@OptIn(ExperimentalAnimationApi::class)
@Preview
@Composable
private fun BugComponentSelector(
	selectedBugComponent : BugReportActivity.Companion.BugComponent? = null,
	onClick : () -> Unit = {},
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 0.dp)
			.border(1.dp, MaterialTheme.colorScheme.onBackground, MaterialTheme.shapes.medium)
			.clip(MaterialTheme.shapes.medium)
			.clickable { onClick() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp)
		) {
			AnimatedContent(
				targetState = selectedBugComponent,
				modifier = Modifier.weight(1f),
				transitionSpec = { fadeIn(tween(300)) with fadeOut(tween(300)) }
			) {
				Row(
					verticalAlignment = Alignment.CenterVertically,
					modifier = Modifier.fillMaxWidth()
				) {
					Icon(
						painter = painterResource(id = BugReportActivity.bugComponentIconMap.getOrDefault(it, R.drawable.ic_exclamation)),
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier
							.requiredSize(IconButtonSize)
							.padding(2.dp)
					)
					Spacer(modifier = Modifier.width(8.dp))
					Text(
						text = BugReportActivity.bugComponentNameMap.getOrDefault(it, "Select Component Type"),
						style = MaterialTheme.typography.titleMedium,
						color = MaterialTheme.colorScheme.onBackground,
						modifier = Modifier
					)
				}
			}
			Spacer(modifier = Modifier.width(12.dp))
			Icon(
				painter = painterResource(id = R.drawable.ic_caret),
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onBackground,
				modifier = Modifier
					.requiredSize(IconButtonSize)
					.graphicsLayer { rotationZ = if (selectedBugComponent == null) 0f else 180f }
			)
		}
	}
}

@Preview
@Composable
private fun BugTypeMenu(
	selectedBugType : BugReportActivity.Companion.BugType? = null,
	onBugTypeSelected : (BugReportActivity.Companion.BugType) -> Unit = {},
	expanded : Boolean = true,
	onDismiss : () -> Unit = {}
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Box(
		modifier = Modifier
			.fillMaxSize()
			.wrapContentSize(Alignment.TopStart)
			.background(MaterialTheme.colorScheme.background)
	) {
		DropdownMenu(
			expanded = expanded,
			offset = DpOffset(12.dp, 0.dp),
			onDismissRequest = onDismiss,
			modifier = Modifier
				.width(screenWidth - 24.dp)
				.background(MaterialTheme.colorScheme.background),
		) {
			BugReportActivity.Companion.BugType.values().forEach {
				DropdownMenuItem(
					leadingIcon = {
						Icon(
							painter = painterResource(id = BugReportActivity.bugTypeIconMap.getOrDefault(it, R.drawable.ic_exclamation)),
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onBackground,
							modifier = Modifier
								.requiredSize(IconButtonSize)
								.padding(2.dp)
						)
					},
					text = {
						Text(
							text = BugReportActivity.bugTypeNameMap.getOrDefault(it, "Select Report Type"),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onBackground,
						)
					},
					onClick = { onBugTypeSelected(it) }
				)
			}
		}
	}
}

@Preview
@Composable
private fun BugComponentMenu(
	selectedBugComponent : BugReportActivity.Companion.BugComponent? = null,
	onBugComponentSelected : (BugReportActivity.Companion.BugComponent) -> Unit = {},
	expanded : Boolean = true,
	onDismiss : () -> Unit = {}
) {
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	Box(
		modifier = Modifier
			.fillMaxSize()
			.wrapContentSize(Alignment.TopStart)
			.background(MaterialTheme.colorScheme.background)
	) {
		DropdownMenu(
			expanded = expanded,
			offset = DpOffset(12.dp, 0.dp),
			onDismissRequest = onDismiss,
			modifier = Modifier
				.width(screenWidth - 24.dp)
				.background(MaterialTheme.colorScheme.background),
		) {
			BugReportActivity.Companion.BugComponent.values().forEach {
				DropdownMenuItem(
					leadingIcon = {
						Icon(
							painter = painterResource(id = BugReportActivity.bugComponentIconMap.getOrDefault(it, R.drawable.ic_exclamation)),
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onBackground,
							modifier = Modifier
								.requiredSize(IconButtonSize)
								.padding(0.dp)
						)
					},
					text = {
						Text(
							text = BugReportActivity.bugComponentNameMap.getOrDefault(it, "Select Component Type"),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onBackground,
						)
					},
					onClick = { onBugComponentSelected(it) }
				)
			}
		}
	}
}
