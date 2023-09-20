package com.syncodec.graphite.presentation.common.dialog.dialog2

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.syncodec.graphite.R


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun GenericDialog2(
	isDialogVisible: Boolean = false,
	icon: GenericDialogIcon? = null,
	title: String? = null,
	contentText: String? = null,
	primaryButton: GenericDialogButton? = null,
	secondaryButton: GenericDialogButton? = null,
	isPrimaryButtonEnabled: Boolean = true,
	isSecondaryButtonEnabled: Boolean = true,
	innerPadding: PaddingValues = PaddingValues(24.dp),
	onDismissRequest: () -> Unit = {},
	content: @Composable (ColumnScope.() -> Unit)? = null,
) {

	BackHandler(enabled = isDialogVisible) { onDismissRequest() }

	if (isDialogVisible) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.071f))
		) {
			AlertDialog(
				onDismissRequest = onDismissRequest,
				properties = DialogProperties(
					dismissOnBackPress = true,
					dismissOnClickOutside = true,
				)
			) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.extraLarge)
				) {
					Column(
						modifier = Modifier
							.fillMaxWidth()
							.padding(innerPadding)
					) {
						icon?.let {
							Icon(
								painter = painterResource(id = it.icon),
								contentDescription = title,
								tint = it.tint,
								modifier = Modifier
									.requiredSize(24.dp)
									.align(Alignment.CenterHorizontally)
							)
							Spacer(modifier = Modifier.height(24.dp))
						}
						title?.let {
							Text(
								text = it,
								style = MaterialTheme.typography.titleLarge,
								color = MaterialTheme.colorScheme.onBackground,
								fontWeight = FontWeight.Bold,
								textAlign = if (icon == null) TextAlign.Start else TextAlign.Center,
								modifier = Modifier.fillMaxWidth()
							)
							Spacer(modifier = Modifier.height(24.dp))
						}

						contentText?.let {
							Text(
								text = it,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onBackground,
							)
							Spacer(modifier = Modifier.height(24.dp))
						}

						content?.let {
							it()
							Spacer(modifier = Modifier.height(24.dp))
						}

						GenericDialogButtonView(
							primaryButton = primaryButton,
							secondaryButton = secondaryButton,
							isPrimaryButtonEnabled = isPrimaryButtonEnabled,
							isSecondaryButtonEnabled = isSecondaryButtonEnabled,
						)
					}
				}
			}
		}
	}
}

@Preview
@Composable
fun GenericDialogButtonView(
	primaryButton: GenericDialogButton? = GenericDialogDefaults.genericDialogButtonPrimary(text = "Primary", onClick = {}),
	secondaryButton: GenericDialogButton? = GenericDialogDefaults.genericDialogButtonSecondary(text = "Secondary", onClick = {}),
	isPrimaryButtonEnabled: Boolean = true,
	isSecondaryButtonEnabled: Boolean = true,
) {
	if (primaryButton != null || secondaryButton != null) {
		Row(
			modifier = Modifier.fillMaxWidth()
		) {
			secondaryButton?.let {
				TextButton(
					onClick = it.onClick,
					shape = MaterialTheme.shapes.medium,
					colors = it.colors,
					enabled = isSecondaryButtonEnabled,
					modifier = Modifier.weight(1f),
				) {
					Text(text = it.text)
				}
			}
			if (primaryButton != null && secondaryButton != null) Spacer(modifier = Modifier.width(8.dp))
			primaryButton?.let {
				Button(
					onClick = it.onClick,
					shape = MaterialTheme.shapes.medium,
					colors = it.colors,
					enabled = isPrimaryButtonEnabled,
					modifier = Modifier.weight(1f),
				) {
					Text(text = it.text)
				}
			}
		}
	}
}

data class GenericDialogIcon(
	val icon: Int,
	val tint: Color,
) {
	override fun hashCode(): Int {
		var result = icon
		result = 31 * result + tint.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is GenericDialogIcon) return false

		if (icon != other.icon) return false
		return tint == other.tint
	}
}

data class GenericDialogButton(
	val text: String,
	val colors: ButtonColors,
	val onClick: () -> Unit,
) {
	override fun hashCode(): Int {
		var result = text.hashCode()
		result = 31 * result + colors.hashCode()
		result = 31 * result + onClick.hashCode()
		return result
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is GenericDialogButton) return false

		if (text != other.text) return false
		if (colors != other.colors) return false
		return onClick == other.onClick
	}
}

object GenericDialogDefaults {
	@Composable
	fun genericDialogIcon(icon: Int, tint: Color = MaterialTheme.colorScheme.onBackground): GenericDialogIcon = GenericDialogIcon(icon = icon, tint = tint)

	@Composable
	fun genericDialogWarningIcon(): GenericDialogIcon = GenericDialogIcon(icon = R.drawable.ic_fa_warning, tint = MaterialTheme.colorScheme.error)

	@Composable
	fun genericDialogButton(text: String, buttonColors: ButtonColors, onClick: () -> Unit): GenericDialogButton = GenericDialogButton(text = text, colors = buttonColors, onClick = onClick)

	@Composable
	fun genericDialogButtonPrimary(text: String, onClick: () -> Unit): GenericDialogButton =
		GenericDialogButton(
			text = text,
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primaryContainer,
				contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
				disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.47f),
				disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
			),
			onClick = onClick,
		)

	@Composable
	fun genericDialogButtonSecondary(text: String, onClick: () -> Unit): GenericDialogButton =
		GenericDialogButton(
			text = text,
			colors = ButtonDefaults.buttonColors(
				containerColor = Color.Transparent,
				contentColor = MaterialTheme.colorScheme.onBackground,
				disabledContainerColor = Color.Transparent,
				disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f),
			),
			onClick = onClick,
		)

	@Composable
	fun genericDialogButtonWarning(text: String, onClick: () -> Unit): GenericDialogButton =
		GenericDialogButton(
			text = text,
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.errorContainer,
				contentColor = MaterialTheme.colorScheme.onErrorContainer,
				disabledContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.47f),
				disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
			),
			onClick = onClick,
		)

	@Composable
	fun genericDialogButtonDismiss(onClick: () -> Unit) = genericDialogButtonSecondary(text = stringResource(id = R.string.dismiss), onClick = onClick)
}
