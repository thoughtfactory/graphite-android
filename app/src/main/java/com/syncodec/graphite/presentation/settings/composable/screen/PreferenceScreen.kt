package com.syncodec.graphite.presentation.settings.composable.screen

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.main.composable.buildingBlock.DropdownMenuItemView
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonIcon
import com.syncodec.graphite.presentation.ui.Defaults
import com.syncodec.graphite.presentation.ui.MontserratTypography
import com.syncodec.graphite.presentation.ui.PTMonoTypography
import com.syncodec.graphite.presentation.ui.RobotoTypography
import com.syncodec.graphite.presentation.ui.TiltNeonTypography
import com.syncodec.graphite.presentation.ui.UbuntuTypography
import com.syncodec.graphite.utils.dataStore.DataStoreInstance


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreferenceScreen(
	onClickBack : () -> Unit = {},
) {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val darkTheme by dataStoreInstance.getDarkTheme.collectAsState(null)
	val typography by dataStoreInstance.getTypography.collectAsState(initial = null)

	var showTypographyDropdownMenu by remember { mutableStateOf(false) }
	var showforceSideDropdownMenu by remember { mutableStateOf(false) }

	GenericSettingsScaffold(
		title = "Preferences",
		onClickBack = onClickBack,
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize()
		) {
			item {
				ExposedDropdownMenuBox(
					expanded = showTypographyDropdownMenu,
					onExpandedChange = { showTypographyDropdownMenu = !it },
				) {
					SettingsButton(
						title = "Typography",
						leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_typography),
						subTitle = typography ?: Defaults.DefaultTypagrophyName,
						modifier = Modifier.menuAnchor(),
						onClick = { showTypographyDropdownMenu = true },
					)
					ExposedDropdownMenu(
						expanded = showTypographyDropdownMenu,
						onDismissRequest = { showTypographyDropdownMenu = false },
						modifier = Modifier
							.wrapContentWidth()
							.background(MaterialTheme.colorScheme.background),
					) {
						TypographyDropdownMenuItemView(
							title = "PT Mono",
							fontFamily = PTMonoTypography.bodyMedium.fontFamily,
						) {
							dataStoreInstance.putTypography("PT Mono")
							showTypographyDropdownMenu = false
						}
						TypographyDropdownMenuItemView(
							title = "Ubuntu",
							fontFamily = UbuntuTypography.bodyMedium.fontFamily,
						) {
							dataStoreInstance.putTypography("Ubuntu")
							showTypographyDropdownMenu = false
						}
						TypographyDropdownMenuItemView(
							title = "Montserrat",
							fontFamily = MontserratTypography.bodyMedium.fontFamily,
						) {
							dataStoreInstance.putTypography("Montserrat")
							showTypographyDropdownMenu = false
						}
						TypographyDropdownMenuItemView(
							title = "Roboto",
							fontFamily = RobotoTypography.bodyMedium.fontFamily,
						) {
							dataStoreInstance.putTypography("Roboto")
							showTypographyDropdownMenu = false
						}
						TypographyDropdownMenuItemView(
							title = "Tilt Neon",
							fontFamily = TiltNeonTypography.bodyMedium.fontFamily,
						) {
							dataStoreInstance.putTypography("Tilt Neon")
							showTypographyDropdownMenu = false
						}
					}
				}
			}
			item {
				ExposedDropdownMenuBox(
					expanded = showforceSideDropdownMenu,
					onExpandedChange = { showforceSideDropdownMenu = !it },
				) {
					SettingsButton(
						title = "Which side are you on?",
						subTitle = when (darkTheme) {
							SettingsActivity.Companion.DarkTheme.SyncWithSystem -> "Same as system"
							SettingsActivity.Companion.DarkTheme.AlwaysOn -> "Dark side"
							SettingsActivity.Companion.DarkTheme.AlwaysOff -> "Light side"
							null -> "Same as system"
						},
						leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_bulb),
						trailingIcon = SettingsButtonIcon(
							icon = when (darkTheme) {
								SettingsActivity.Companion.DarkTheme.SyncWithSystem -> R.drawable.ic_flat_r2d2
								SettingsActivity.Companion.DarkTheme.AlwaysOn -> R.drawable.ic_dark_side
								SettingsActivity.Companion.DarkTheme.AlwaysOff -> R.drawable.ic_light_side
								null -> R.drawable.ic_flat_r2d2
							},
							color = when (darkTheme) {
								SettingsActivity.Companion.DarkTheme.SyncWithSystem -> MaterialTheme.colorScheme.onBackground
								SettingsActivity.Companion.DarkTheme.AlwaysOn -> Color.Unspecified
								SettingsActivity.Companion.DarkTheme.AlwaysOff -> Color.Unspecified
								null -> MaterialTheme.colorScheme.onBackground
							}
						),
						modifier = Modifier.menuAnchor(),
						onClick = { showforceSideDropdownMenu = true }
					)
					ExposedDropdownMenu(
						expanded = showforceSideDropdownMenu,
						onDismissRequest = { showforceSideDropdownMenu = false },
						modifier = Modifier
							.wrapContentWidth()
							.background(MaterialTheme.colorScheme.background),
					) {
						DropdownMenuItemView(
							icon = R.drawable.ic_flat_r2d2,
							title = "Same as system",
							iconColor = MaterialTheme.colorScheme.onBackground,
						) {
							dataStoreInstance.putDarkTheme(SettingsActivity.Companion.DarkTheme.SyncWithSystem)
							showforceSideDropdownMenu = false
						}

						DropdownMenuItemView(
							icon = R.drawable.ic_light_side,
							title = "Light side",
							iconColor = Color.Unspecified,
						) {
							dataStoreInstance.putDarkTheme(SettingsActivity.Companion.DarkTheme.AlwaysOff)
							showforceSideDropdownMenu = false
						}

						DropdownMenuItemView(
							icon = R.drawable.ic_dark_side,
							title = "Dark side",
							iconColor = Color.Unspecified,
							isPro = false,
						) {
							dataStoreInstance.putDarkTheme(SettingsActivity.Companion.DarkTheme.AlwaysOn)
							showforceSideDropdownMenu = false
						}
					}
				}
			}
			item {
				SettingsButton(
					title = "Language",
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_flat_language),
					subTitle = "We are working on more languages",
					onClick = { },
				)
			}
		}
	}
}

@Preview
@Composable
fun TypographyDropdownMenuItemView(
	title: String = "Title",
	fontFamily: FontFamily? = Defaults.DefaultTypagrophy.bodyMedium.fontFamily,
	onClick: () -> Unit = {},
) {
	DropdownMenuItem(
		text = {
			Text(
				text = title,
				fontFamily = fontFamily,
			)
		},
		onClick = onClick
	)
}
