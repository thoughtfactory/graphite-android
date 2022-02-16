package com.syncodec.momento.mainComponent.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.syncodec.momento.R
import com.syncodec.momento.custom.ExpandableBox
import com.syncodec.momento.mainComponent.MainViewModel
import com.syncodec.momento.mainComponent.miscellaneous.ComponentChooser
import com.syncodec.momento.mainComponent.modalBottomSheet.BottomSheetType
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun MeScreen() {
	val configuration = LocalConfiguration.current
	val screenHeight = configuration.screenHeightDp.dp
	val screenWidth = configuration.screenWidthDp.dp

	val viewModel: MainViewModel = viewModel()
	val scope = rememberCoroutineScope()
	val openSheet: (BottomSheetType) -> Unit = { bottomSheetType ->
		scope.launch {
			viewModel.mainActivityState.bottomSheetState.show()
		}
	}

	Image(
		painter = painterResource(id = R.drawable.home_background),
		contentDescription = null,
		contentScale = ContentScale.Crop,
		modifier = Modifier
			.fillMaxWidth()
			.height(192.dp),
	)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState()),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Spacer(modifier = Modifier.height(144.dp))

		Card(
			modifier = Modifier
				.requiredSize(96.dp),
			shape = RoundedCornerShape(12.dp),
			elevation = 16.dp
		) {
			Image(
				painter = painterResource(id = R.drawable.debug_profile_picture),
				contentDescription = null
			)
		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.padding(0.dp, 24.dp, 0.dp, 128.dp)
				.background(MaterialTheme.colorScheme.background)
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				ProfileCard()

				EntryStatsCard()

				Spacer(modifier = Modifier.height(8.dp))
				RecentCard()

				Spacer(modifier = Modifier.height(8.dp))
				ComponentChooser() {}
			}
		}
	}
}

@Composable
private fun ProfileCard() {
	Text(
		text = "Bruce Wayne",
		style = MaterialTheme.typography.titleLarge,
		fontWeight = FontWeight.Bold,
		color = MaterialTheme.colorScheme.primary
	)

	Text(
		text = "bruce.wayne@gmail.com",
		style = MaterialTheme.typography.bodyMedium,
		fontWeight = FontWeight.Bold,
		color = MaterialTheme.colorScheme.onBackground
	)
}

@Composable
private fun EntryStatsCard() {
	var expandCard by remember { mutableStateOf(false) }

	Box(
		modifier = Modifier
			.padding(12.dp)
			.clip(RoundedCornerShape(16.dp))
			.background(MaterialTheme.colorScheme.secondaryContainer)
			.clickable { expandCard = !expandCard },
		contentAlignment = Alignment.Center
	) {
		Column(
			modifier = Modifier
				.padding(12.dp, 8.dp)
		) {
			Row(
				modifier = Modifier,
				verticalAlignment = Alignment.CenterVertically
			) {
				Row(
					modifier = Modifier
						.weight(1f)
						.padding(8.dp, 4.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = TablerIcons.Signature,
						contentDescription = "Entries",
						modifier = Modifier
							.requiredSize(20.dp)
					)
					Spacer(modifier = Modifier.width(4.dp))
					Text(
						text = "13 entries",
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSecondaryContainer,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}
				Box(
					modifier = Modifier
						.width(3.dp)
						.height(24.dp)
						.clip(RoundedCornerShape(4.dp))
						.background(MaterialTheme.colorScheme.primary)
				)

				Row(
					modifier = Modifier
						.weight(1f)
						.padding(8.dp, 4.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = TablerIcons.Bucket,
						contentDescription = "Bucket",
						modifier = Modifier
							.requiredSize(20.dp)
					)
					Spacer(modifier = Modifier.width(4.dp))
					Text(
						text = "47 bucket",
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSecondaryContainer,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}
				Box(
					modifier = Modifier
						.width(3.dp)
						.height(24.dp)
						.clip(RoundedCornerShape(4.dp))
						.background(MaterialTheme.colorScheme.primary)
				)

				Row(
					modifier = Modifier
						.weight(1f)
						.padding(8.dp, 4.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = TablerIcons.Notebook,
						contentDescription = "Notebook",
						modifier = Modifier
							.requiredSize(20.dp)
					)
					Spacer(modifier = Modifier.width(4.dp))
					Text(
						text = "71 notebook",
						style = MaterialTheme.typography.bodySmall,
						fontWeight = FontWeight.Bold,
						color = MaterialTheme.colorScheme.onSecondaryContainer,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}
			}

			ExpandableBox(
				isVisible = expandCard
			) {
				Row(
					modifier = Modifier,
					verticalAlignment = Alignment.CenterVertically
				) {
					Row(
						modifier = Modifier
							.weight(1f)
							.padding(8.dp, 4.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							imageVector = TablerIcons.AspectRatio,
							contentDescription = "Attachment",
							modifier = Modifier
								.requiredSize(20.dp)
						)
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = "7 attachments",
							style = MaterialTheme.typography.bodySmall,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onSecondaryContainer,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					}
					Box(
						modifier = Modifier
							.width(3.dp)
							.height(24.dp)
							.clip(RoundedCornerShape(4.dp))
							.background(MaterialTheme.colorScheme.primary)
					)

					Row(
						modifier = Modifier
							.weight(1f)
							.padding(8.dp, 4.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							imageVector = TablerIcons.Notes,
							contentDescription = "Chapter",
							modifier = Modifier
								.requiredSize(20.dp)
						)
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = "3 chapters",
							style = MaterialTheme.typography.bodySmall,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onSecondaryContainer,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					}
					Box(
						modifier = Modifier
							.width(3.dp)
							.height(24.dp)
							.clip(RoundedCornerShape(4.dp))
							.background(MaterialTheme.colorScheme.primary)
					)

					Row(
						modifier = Modifier
							.weight(1f)
							.padding(8.dp, 4.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							imageVector = TablerIcons.Notes,
							contentDescription = "Note",
							modifier = Modifier
								.requiredSize(20.dp)
						)
						Spacer(modifier = Modifier.width(4.dp))
						Text(
							text = "2 notes",
							style = MaterialTheme.typography.bodySmall,
							fontWeight = FontWeight.Bold,
							color = MaterialTheme.colorScheme.onSecondaryContainer,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
					}
				}
			}
		}
	}
}

@Composable
private fun RecentCard() {
	Column(
		modifier = Modifier
			.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp, 0.dp),
			verticalAlignment = Alignment.Bottom
		) {
			Box(
				modifier = Modifier
					.width(3.dp)
					.height(32.dp)
					.clip(RoundedCornerShape(3.dp))
					.background(MaterialTheme.colorScheme.primary)
			)
			Spacer(modifier = Modifier.width(4.dp))
			Text(
				text = "Recently edited",
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onBackground,
			)
		}

		Row(
			modifier = Modifier
		) {

		}
	}
}
