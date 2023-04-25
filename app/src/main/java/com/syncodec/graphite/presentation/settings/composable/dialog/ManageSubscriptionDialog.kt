package com.syncodec.graphite.presentation.settings.composable.dialog

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.dialog.GenericDialog
import com.syncodec.graphite.presentation.common.dialog.buildingBlock.DualActionButtons


@OptIn(ExperimentalTextApi::class)
@Preview
@Composable
fun ManageSubscriptionDialog(
	showDialog : Boolean = false,
	onDismiss : () -> Unit = { },
) {
	val context = LocalContext.current
	val clipboardManager =LocalClipboardManager.current

	GenericDialog(
		showDialog = showDialog,
		icon = R.drawable.ic_setting,
		iconTint = MaterialTheme.colorScheme.onBackground,
		title = "Manage Subscription",
		contentText = "If opening the Play Store doesn't work, you can copy the link and open it in a browser.",
		dualActionButton = {
			DualActionButtons(
				primaryText = "Open Play Store",
				secondaryText = "Cancel",
				onClickPrimary = {
					try {
						context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions?package=com.syncodec.graphite")))
					} catch (_ : Exception) {
						Toast.makeText(context, "Unable to open Play Store. Try copying the link and open it in a browser.", Toast.LENGTH_SHORT).show()
					}
				},
				onClickSecondary = onDismiss
			)
		},
		onDismissRequest = onDismiss,
	) {
		ClickableText(
			text = buildAnnotatedString {
				this.append("Click to copy link")
				addStyle(
					style = MaterialTheme.typography.bodyLarge.toSpanStyle().copy(color = Color.Blue, fontWeight = FontWeight.Bold),
					start = 0,
					end = this.length
				)
			},
			onClick = {
				val link = "https://support.google.com/googleplay/answer/7018481?hl=en&co=GENIE.Platform%3DAndroid"
				buildAnnotatedString {
					append(link)
					addUrlAnnotation(UrlAnnotation(link), 0, link.length)
					clipboardManager.setText(this.toAnnotatedString())
				}
			}
		)
	}
}
