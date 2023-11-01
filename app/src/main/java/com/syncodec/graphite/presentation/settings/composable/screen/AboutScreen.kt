package com.syncodec.graphite.presentation.settings.composable.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.syncodec.graphite.BuildConfig
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults


@Preview
@Composable
fun AboutScreen() {
	val context = LocalContext.current

	GenericSettingsScaffold(
		title = stringResource(id = R.string.about),
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally
		) {

			item {
				Image(
					painter = painterResource(R.mipmap.ic_launcher_foreground),
					contentDescription = null,
					modifier = Modifier.requiredSize(256.dp)
				)
			}

			item {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					Text(
						text = "Graphite",
						style = MaterialTheme.typography.headlineMedium,
						fontWeight = FontWeight.Bold,
					)
					Spacer(modifier = Modifier.height(12.dp))
					Text(
						text = "${BuildConfig.VERSION_NAME} / ${BuildConfig.VERSION_CODE}",
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.Bold,
					)
				}
			}

			item { Spacer(modifier = Modifier.height(64.dp)) }

			item {
				SettingsButton(
					title = stringResource(id = R.string.open_source_licenses),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_code),
					onClick = { context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))  },
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.spread_a_word),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_share),
					onClick = {
						try {
							val shareIntent = Intent(Intent.ACTION_SEND)
							shareIntent.type = "text/plain"
							shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Graphite")
							var shareMessage = "\nHey... Check out Graphite, an everyday diary and bucket list\n\n"
							shareMessage = """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
							shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
							context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_using)))
						} catch (e : Exception) {
							Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
						}
					},
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.rate_us),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_star),
					onClick = {
						context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")))
					},
				)
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.join_us_on_instagram),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_logo_instagram, color = Color.Unspecified),
					onClick = {
						val uri = Uri.parse("https://www.instagram.com/graphite.diary/?hl=en")
						val likeIng = Intent(Intent.ACTION_VIEW, uri)

						likeIng.setPackage("com.instagram.android")

						try {
							context.startActivity(likeIng)
						} catch (e : ActivityNotFoundException) {
							context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/graphite.diary/?hl=en")))
						} catch (e : Exception) {
							if (BuildConfig.DEBUG) e.printStackTrace()
							Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
						}
					},
				)
			}
		}
	}
}
