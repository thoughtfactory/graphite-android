package com.syncodec.graphite.presentation.pro.composable.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revenuecat.purchases.Package
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.common.modifier.BorderSide
import com.syncodec.graphite.presentation.common.modifier.oneSideBorder
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold
import com.syncodec.graphite.presentation.pro.ProActivity
import com.syncodec.graphite.presentation.pro.composable.bar.TopBar
import com.syncodec.graphite.presentation.pro.composable.buildingBlock.LifetimePackageView
import com.syncodec.graphite.presentation.pro.composable.buildingBlock.ProFeaturesView
import com.syncodec.graphite.presentation.pro.composable.buildingBlock.SubscriptionPackageView
import com.syncodec.graphite.utils.ContentStatus


@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun SubscriptionScreen(
	contentStatus : ContentStatus<ProActivity.Companion.ProductPackage> = ContentStatus.Init,
	userEmail : String? = null,
	onClickPackage : (Package?) -> Unit = {},
	onRestore : () -> Unit = {},
) {

	val context = LocalContext.current

	GenericScaffold(
		topBar = { TopBar() }
	) {
		Box(
			modifier = Modifier.fillMaxSize()
		) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally,
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
			) {
				Text(
					text = "Unlock the full potential of Graphite with pro",
					fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
					fontWeight = FontWeight.Bold,
					fontSize = 16.sp,
					lineHeight = 18.sp,
					letterSpacing = 2.sp,
					color = MaterialTheme.colorScheme.onBackground,
					modifier = Modifier.padding(32.dp, 0.dp)
				)

				Spacer(modifier = Modifier.height(8.dp))

				ProFeaturesView()

				Spacer(modifier = Modifier.height(24.dp))
				SubscriptionPackageView(
					monthlyPackage = contentStatus.dataOrNull?.monthlyPackage,
					annualPackage = contentStatus.dataOrNull?.annualPackage,
					onClickPackage = onClickPackage
				)
				Spacer(modifier = Modifier.height(16.dp))
				LifetimePackageView(
					lifetimePackage = contentStatus.dataOrNull?.lifetimePackage,
					onClickPackage = onClickPackage
				)
				Spacer(modifier = Modifier.height(16.dp))

				Text(
					text = userEmail?.let { "Logged in as $it" } ?: "Not logged in",
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onBackground,
				)

				Spacer(modifier = Modifier.height(8.dp))

				ClickableText(
					text = buildAnnotatedString {
						append("Support email : support@syncodec.com")
						this.addStyle(
							style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
								color = MaterialTheme.colorScheme.onBackground
							),
							start = 0,
							end = length
						)
					},
					style = MaterialTheme.typography.bodyMedium,
					onClick = {
						Intent(Intent.ACTION_SENDTO).apply {
							data = Uri.parse("mailto:support@syncodec.com")
							if (resolveActivity(context.packageManager) != null) context.startActivity(this)
							else Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
						}
					},
				)

				Spacer(modifier = Modifier.height(16.dp))

				OfferingView(
					title = "Unleash the power of Rich Text Editing",
					offering = { RichTextEditorOffering() }
				)

				Spacer(modifier = Modifier.height(16.dp))

				OfferingView(
					title = "Don't run out of attachments",
					offering = { AttachmentsOffering() }
				)

				Spacer(modifier = Modifier.height(16.dp))

				OfferingView(
					title = "Don't let an idea slip away",
					offering = { NotificationPinOffering() }
				)

				Spacer(modifier = Modifier.height(16.dp))

				OfferingView(
					title = "Segregate your buckets",
					offering = { BucketOffering() }
				)

				Spacer(modifier = Modifier.height(16.dp))

				OfferingView(
					title = "Better organize your notes and chapters",
					offering = { NotebookOffering() }
				)

				Spacer(modifier = Modifier.height(16.dp))

				OfferingView(
					title = "More ways to share your notes",
					offering = { FormatOffering() }
				)

				Spacer(modifier = Modifier.height(16.dp))

				OfferingView(
					title = "And what about ads?\nWe don't do that here",
					offering = { AdsOffering() }
				)

				Spacer(modifier = Modifier.height(128.dp))
			}

			Button(
				onClick = onRestore,
				shape = MaterialTheme.shapes.medium,
				contentPadding = PaddingValues(start = 16.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
				modifier = Modifier
					.align(Alignment.BottomCenter)
					.padding(bottom = 16.dp)
			) {
				Icon(
					painter = painterResource(id = R.drawable.ic_restore),
					contentDescription = "Restore purchase",
					modifier = Modifier.requiredSize(18.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(text = "Restore purchase")
			}
		}
	}
}

@Preview
@Composable
fun OfferingView(
	title : String = "Title",
	offering : @Composable () -> Unit = {}
) {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(24.dp, 0.dp)
			.background(
				MaterialTheme.colorScheme
					.surfaceColorAtElevation(8.dp)
					.copy(alpha = 0.13f), MaterialTheme.shapes.extraLarge
			)
			.padding(24.dp, 24.dp, 0.dp, 12.dp)
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium,
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground,
			modifier = Modifier.padding(end = 12.dp)
		)
		Spacer(modifier = Modifier.height(16.dp))
		offering()
	}
}

@Preview
@Composable
private fun FeatureHeader(
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier,
	) {
		Spacer(modifier = Modifier.weight(1f))
		Text(
			text = "Standard",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.width(96.dp)
				.oneSideBorder(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f), BorderSide.Left)
				.padding(0.dp, 8.dp)
		)
		Text(
			text = "Pro",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			fontWeight = FontWeight.Bold,
			modifier = Modifier
				.width(96.dp)
				.oneSideBorder(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f), BorderSide.Left)
				.padding(0.dp, 8.dp)
		)
	}
}

@Preview
@Composable
private fun Feature(
	feature : String = "Feature",
	isPro : Boolean = false,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier,
	) {
		Text(
			text = feature,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Start,
			modifier = Modifier
				.heightIn(28.dp)
				.wrapContentHeight(Alignment.CenterVertically)
				.weight(1f)
				.padding(0.dp, 2.dp)
		)
		Availability(icon = if (isPro) R.drawable.ic_cancelmark else R.drawable.ic_checkmark)
		Availability(icon = R.drawable.ic_checkmark)
	}
}

@Preview
@Composable
private fun Feature(
	feature : String = "Feature",
	standard : String = "Standard",
	pro : String = "Pro",
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier,
	) {
		Text(
			text = feature,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Start,
			modifier = Modifier
				.heightIn(28.dp)
				.wrapContentHeight(Alignment.CenterVertically)
				.weight(1f)
				.padding(0.dp, 2.dp)
		)
		Text(
			text = standard,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			modifier = Modifier
				.width(96.dp)
				.heightIn(28.dp)
				.wrapContentHeight(Alignment.CenterVertically)
				.oneSideBorder(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f), BorderSide.Left)
				.padding(0.dp, 2.dp)
		)
		Text(
			text = pro,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			modifier = Modifier
				.width(96.dp)
				.heightIn(28.dp)
				.wrapContentHeight(Alignment.CenterVertically)
				.oneSideBorder(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f), BorderSide.Left)
				.padding(0.dp, 2.dp)
		)
	}
}

@Preview
@Composable
private fun Feature(
	feature : String = "Feature",
	pro : String = "Pro",
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier,
	) {
		Text(
			text = feature,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Start,
			modifier = Modifier
				.heightIn(28.dp)
				.wrapContentHeight(Alignment.CenterVertically)
				.weight(1f)
				.padding(0.dp, 2.dp)
		)
		Availability(icon = R.drawable.ic_cancelmark)
		Text(
			text = pro,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			modifier = Modifier
				.width(96.dp)
				.heightIn(28.dp)
				.wrapContentHeight(Alignment.CenterVertically)
				.oneSideBorder(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f), BorderSide.Left)
				.padding(0.dp, 2.dp)
		)
	}
}

@Preview
@Composable
private fun RichTextEditorOffering() {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		FeatureHeader()
		Feature(feature = "Bold", isPro = false)
		Feature(feature = "Italic", isPro = true)
		Feature(feature = "Underline", isPro = true)
		Feature(feature = "Strikethrough", isPro = true)
		Feature(feature = "Hard Break", isPro = false)
		Feature(feature = "Bullet List", isPro = false)
		Feature(feature = "Ordered List", isPro = true)
		Feature(feature = "Check List", isPro = true)
		Feature(feature = "Heading Style", isPro = true)
		Feature(feature = "Blockquote", isPro = false)
		Feature(feature = "Indent", isPro = false)
		Feature(feature = "Outdent", isPro = false)
		Feature(feature = "Superscript", isPro = true)
		Feature(feature = "Subscript", isPro = true)
	}
}

@Preview
@Composable
private fun AttachmentsOffering() {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		FeatureHeader()
		Feature(feature = "Number of attachments per note", standard = "8", pro = "Unlimited")
	}
}

@Preview
@Composable
private fun NotificationPinOffering() {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		FeatureHeader()
		Feature(feature = "Pin notes to notification", isPro = true)
		Feature(feature = "Write notes from notification", isPro = true)
	}
}

@Preview
@Composable
private fun BucketOffering() {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		FeatureHeader()
		Feature(feature = "Todo Lists", standard = "Unlimited", pro = "Unlimited")
		Feature(feature = "Books Lists", standard = "1", pro = "Unlimited")
		Feature(feature = "Shows Lists", standard = "1", pro = "Unlimited")
		Feature(feature = "Links Lists", standard = "1", pro = "Unlimited")
	}
}

@Preview
@Composable
private fun NotebookOffering() {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		FeatureHeader()
		Feature(feature = "Notebooks", standard = "3", pro = "Unlimited")
		Feature(feature = "Chapters per Notebook", pro = "Unlimited")
		Feature(feature = "Chapters per Chapters", pro = "Unlimited")
		Feature(feature = "Notes per Notebook", standard = "Unlimited", pro = "Unlimited")
		Feature(feature = "Notes per Chapter", pro = "Unlimited")
		Feature(feature = "Custom Notebook cover", isPro = true)
		Feature(feature = "Custom Chapter cover", isPro = true)
		Feature(feature = "Total number of tags", standard = "8", pro = "Unlimited")
	}
}

@Preview
@Composable
private fun FormatOffering() {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		FeatureHeader()
		Feature(feature = "As TXT", isPro = false)
		Feature(feature = "As PDF", isPro = false)
		Feature(feature = "As HTML", isPro = true)
		Feature(feature = "As JSON", isPro = true)
		Feature(feature = "As Markdown", isPro = true)
	}
}

@Preview
@Composable
private fun AdsOffering() {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		FeatureHeader()
		Feature(feature = "Ad free", isPro = false)
	}
}

@Preview
@Composable
fun Availability(
	icon : Int = R.drawable.ic_coming_soon,
) {
	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.width(96.dp)
			.oneSideBorder(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f), BorderSide.Left)
			.padding(0.dp, 2.dp)
	) {
		Icon(
			painter = painterResource(id = icon),
			contentDescription = "Bold",
			tint = Color.Unspecified,
			modifier = Modifier.requiredSize(24.dp),
		)
	}
}

@Preview
@Composable
fun BorderedSpacer(
	height : Dp = 4.dp,
	borderSide : BorderSide = BorderSide.Left,
) {
	Spacer(
		modifier = Modifier
			.height(height)
			.oneSideBorder(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.13f), borderSide)
	)
}
