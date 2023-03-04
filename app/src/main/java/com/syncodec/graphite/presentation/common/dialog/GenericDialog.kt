package com.syncodec.graphite.presentation.common.dialog

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.ColorUtils


@Preview
@Composable
fun GenericDialog(
	showDialog : Boolean = true,
	icon : Int? = null,
	iconTint : Color = MaterialTheme.colorScheme.onBackground,
	title : String = "Title",
	contentText : String? = null,
	dualActionButton : (@Composable () -> Unit)? = null,
	thirdActionButton : (@Composable ColumnScope.() -> Unit)? = null,
	onDismissRequest : () -> Unit = {},
	content : @Composable () -> Unit = {}
) {
	BackHandler(enabled = showDialog) { onDismissRequest() }

	if (showDialog) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f))
		) {
			Dialog(
				onDismissRequest = onDismissRequest,
				properties = DialogProperties(
					dismissOnBackPress = true,
					dismissOnClickOutside = true,
					usePlatformDefaultWidth = false,
				)
			) {
				Box(
					modifier = Modifier
						.fillMaxWidth(0.77f)
						.wrapContentHeight()
						.background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.extraLarge)
				) {
					Column(
						modifier = Modifier
							.fillMaxWidth()
							.padding(24.dp)
					) {
						icon?.let {
							Icon(
								painter = painterResource(id = it),
								contentDescription = null,
								tint = iconTint,
								modifier = Modifier.align(Alignment.CenterHorizontally)
							)
							Spacer(modifier = Modifier.height(16.dp))
						}

						Text(
							text = title,
							style = MaterialTheme.typography.titleLarge,
							color = MaterialTheme.colorScheme.onBackground,
							fontWeight = FontWeight.Bold,
							textAlign = if (icon == null) TextAlign.Start else TextAlign.Center,
							modifier = Modifier.fillMaxWidth()
						)

						Spacer(modifier = Modifier.height(16.dp))

						contentText?.let {
							Text(
								text = it,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onBackground,
								modifier = Modifier.fillMaxWidth()
							)
						}

						content()

						Spacer(modifier = Modifier.height(24.dp))

						thirdActionButton?.let {
							it()
							Spacer(modifier = Modifier.height(2.dp))
						}
						dualActionButton?.invoke()
					}
				}
			}
		}
	}
}
