package com.syncodec.graphite.presentation.settings.composable.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton


@Preview
@Composable
fun AboutUsScreen() {
	val context = LocalContext.current
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.dp

	val scrollState = SettingsActivity.scrollState.current

	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(scrollState),
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(0.dp, 32.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Spacer(modifier = Modifier.height(32.dp))
			Icon(
				painter = painterResource(id = R.drawable.ic_icon),
				contentDescription = null,
				tint = Color.Unspecified,
				modifier = Modifier.size(screenWidth / 3)
			)

			Spacer(modifier = Modifier.height(32.dp))

			Text(
				text = "GRAPHITE",
				fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
				fontWeight = FontWeight.Bold,
				fontSize = 24.sp,
				lineHeight = 28.sp,
				letterSpacing = 2.sp,
				color = MaterialTheme.colorScheme.primary
			)

			Spacer(modifier = Modifier.height(8.dp))

			Text(
				text = BuildConfig.VERSION_NAME,
				fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
				fontWeight = FontWeight.Bold,
				fontSize = 16.sp,
				lineHeight = 18.sp,
				letterSpacing = 2.sp,
				color = MaterialTheme.colorScheme.primary
			)
		}

		Spacer(modifier = Modifier.height(32.dp))

		SettingsButton(
			title = "Open Source Licenses",
			icon = R.drawable.ic_code,
		) { Intent(context, OssLicensesMenuActivity::class.java).apply { context.startActivity(this) } }

		SettingsButton(
			title = "Spread a word",
			icon = R.drawable.ic_share,
		) {
			try {
				val shareIntent = Intent(Intent.ACTION_SEND)
				shareIntent.type = "text/plain"
				shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Graphite")
				var shareMessage = "\nHey... Check out Graphite, an everyday diary and bucket list\n\n"
				shareMessage = """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
				shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
				context.startActivity(Intent.createChooser(shareIntent, "choose one"))
			} catch (e: Exception) {
				Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
			}
		}

		SettingsButton(
			title = "Rate us",
			icon = R.drawable.ic_rate_us,
		) { Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")).apply { context.startActivity(this) } }

		SettingsButton(
			title = "Find us on Instagram",
			icon = R.drawable.ic_instagram,
			iconColor = Color.Unspecified
		) {
			val uri = Uri.parse("https://www.instagram.com/graphite.diary/?hl=en")
			val likeIng = Intent(Intent.ACTION_VIEW, uri)

			likeIng.setPackage("com.instagram.android")

			try {
				context.startActivity(likeIng)
			} catch (e : ActivityNotFoundException) {
				context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/graphite.diary/?hl=en")))
			} catch (e : Exception) {
				Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
			}
		}

		Spacer(modifier = Modifier.height(32.dp))

		MadeWithLove()

		Spacer(modifier = Modifier.height(32.dp))
	}
}


@Composable
private fun MadeWithLove() {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.Center,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = "made with",
			style = MaterialTheme.typography.bodyLarge,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)
		)

		Spacer(modifier = Modifier.width(6.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_love),
			contentDescription = "Love",
			tint = Color.Unspecified,
			modifier = Modifier.requiredSize(14.dp)
		)

		Spacer(modifier = Modifier.width(6.dp))

		Text(
			text = "on",
			style = MaterialTheme.typography.bodyLarge,
			fontFamily = FontFamily(Font(R.font.graduate_regular, FontWeight.Normal)),
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.47f)
		)

		Spacer(modifier = Modifier.width(6.dp))

		Icon(
			painter = painterResource(id = R.drawable.ic_earth),
			contentDescription = "Earth",
			tint = Color.Unspecified,
			modifier = Modifier.requiredSize(16.dp)
		)
	}
}
