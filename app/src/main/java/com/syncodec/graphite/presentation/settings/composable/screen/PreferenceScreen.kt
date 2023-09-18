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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.main.composable.buildingBlock.DropdownMenuItemView
import com.syncodec.graphite.presentation.settings.SettingsActivity
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.GenericSettingsScaffold
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButton
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonDefaults
import com.syncodec.graphite.presentation.settings.composable.buildingBlock.SettingsButtonIcon
import com.syncodec.graphite.presentation.base.Defaults
import com.syncodec.graphite.presentation.base.MontserratTypography
import com.syncodec.graphite.presentation.base.PTMonoTypography
import com.syncodec.graphite.presentation.base.RobotoTypography
import com.syncodec.graphite.presentation.base.TiltNeonTypography
import com.syncodec.graphite.presentation.base.UbuntuTypography
import com.syncodec.graphite.utils.dataStore.DataStoreInstance


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreferenceScreen() {
	val context = LocalContext.current
	val dataStoreInstance = remember { DataStoreInstance(context = context) }

	val darkTheme by dataStoreInstance.getDarkTheme.collectAsState(null)
	val typography by dataStoreInstance.getTypography.collectAsState(initial = null)

	var showTypographyDropdownMenu by remember { mutableStateOf(false) }
	var showForceSideDropdownMenu by remember { mutableStateOf(false) }

	GenericSettingsScaffold(
		title = stringResource(id = R.string.preferences),
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
						title = stringResource(id = R.string.typography),
						leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_typography),
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
					expanded = showForceSideDropdownMenu,
					onExpandedChange = { showForceSideDropdownMenu = !it },
				) {
					SettingsButton(
						title = stringResource(id = R.string.dark_light_theme),
						subTitle = when (darkTheme) {
							SettingsActivity.Companion.DarkTheme.SyncWithSystem -> stringResource(id = R.string.theme_same_as_system)
							SettingsActivity.Companion.DarkTheme.AlwaysOn -> stringResource(id = R.string.theme_dark)
							SettingsActivity.Companion.DarkTheme.AlwaysOff -> stringResource(id = R.string.theme_light)
							null -> stringResource(id = R.string.theme_same_as_system)
						},
						leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_bulb),
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
						onClick = { showForceSideDropdownMenu = true }
					)
					ExposedDropdownMenu(
						expanded = showForceSideDropdownMenu,
						onDismissRequest = { showForceSideDropdownMenu = false },
						modifier = Modifier
							.wrapContentWidth()
							.background(MaterialTheme.colorScheme.background),
					) {
						DropdownMenuItemView(
							icon = R.drawable.ic_flat_r2d2,
							title = stringResource(id = R.string.theme_same_as_system),
							iconColor = MaterialTheme.colorScheme.onBackground,
						) {
							dataStoreInstance.putDarkTheme(SettingsActivity.Companion.DarkTheme.SyncWithSystem)
							showForceSideDropdownMenu = false
						}

						DropdownMenuItemView(
							icon = R.drawable.ic_light_side,
							title = stringResource(id = R.string.theme_light),
							iconColor = Color.Unspecified,
						) {
							dataStoreInstance.putDarkTheme(SettingsActivity.Companion.DarkTheme.AlwaysOff)
							showForceSideDropdownMenu = false
						}

						DropdownMenuItemView(
							icon = R.drawable.ic_dark_side,
							title = stringResource(id = R.string.theme_dark),
							iconColor = Color.Unspecified,
							isPro = false,
						) {
							dataStoreInstance.putDarkTheme(SettingsActivity.Companion.DarkTheme.AlwaysOn)
							showForceSideDropdownMenu = false
						}
					}
				}
			}
			item {
				SettingsButton(
					title = stringResource(id = R.string.language),
					leadingIcon = SettingsButtonDefaults.settingsButtonLeadingIcon(icon = R.drawable.ic_fa_language),
					subTitle = stringResource(id = R.string.language_sub),
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
