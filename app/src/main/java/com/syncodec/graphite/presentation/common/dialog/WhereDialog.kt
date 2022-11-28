package com.syncodec.graphite.presentation.common.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.ChapterObjectLite
import com.syncodec.graphite.presentation.common.button.MenuButton
import com.syncodec.graphite.presentation.notebook.composable.buildingBlock.navigator.Navigator
import com.syncodec.graphite.presentation.ui.FavouriteContainer
import com.syncodec.graphite.presentation.ui.LockClosedContainer
import com.syncodec.graphite.utils.Authenticator
import com.syncodec.graphite.utils.LocalAuthenticatorAction
import com.syncodec.graphite.utils.LocalVaultIsOpened
import com.syncodec.graphite.utils.tone
import io.realm.kotlin.types.RealmUUID


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun WhereDialog(
	showDialog : Boolean,
	parentChapter : ChapterObjectLite?,
	chapterList : List<ChapterObjectLite>,
	chapterPath : List<ChapterObjectLite>,
	onWhere : (RealmUUID?) -> Unit,
	onSetWhere : (ChapterObjectLite?) -> Unit,
	onDismiss : () -> Unit,
) {
	val isVaultOpen = LocalVaultIsOpened.current

	AnimatedVisibility(
		visible = showDialog,
		modifier = Modifier.fillMaxSize(),
		enter = fadeIn(tween(300)),
		exit = fadeOut(tween(300))
	) {
		Scaffold(
			topBar = {
				WhereDialogTopBar(
					chapterPath = chapterPath,
					onSelectChapter = { onWhere(it) },
					onDismiss = onDismiss
				)
			},
			bottomBar = {
				WhereDialogBottomBar {
					onSetWhere(parentChapter)
					onDismiss()
				}
			}
		) {
			AnimatedContent(
				targetState = chapterList,
				modifier = Modifier
					.fillMaxSize()
					.padding(it)
			) {
				LazyColumn(
					modifier = Modifier.fillMaxSize()
				) {
					item { Spacer(modifier = Modifier.height(8.dp)) }
					it.filter { if (it.isLocked) isVaultOpen else true }.forEach {
						item(key = it.id.toString()) {
							Box(
								modifier = Modifier.animateItemPlacement()
							) {
								ChapterItem(chapterObject = it) { onWhere(it.id) }
							}
						}
					}
					item { Spacer(modifier = Modifier.height(32.dp)) }
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WhereDialogTopBar(
	chapterPath : List<ChapterObjectLite>,
	onSelectChapter : (RealmUUID?) -> Unit,
	onDismiss : () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxWidth()
	) {
		TopAppBar(
			navigationIcon = {
				MenuButton(
					icon = R.drawable.ic_close,
					onClick = onDismiss
				)
			},
			title = {
				Text(
					text = "Select Chapter",
					fontWeight = FontWeight.Bold
				)
			},
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.surface,
			)
		)

		Spacer(modifier = Modifier.height(8.dp))

		Navigator(
			showRoot = true,
			defaultChapterId = null,
			chapterObjectLiteList = chapterPath,
			onClick = onSelectChapter
		)
	}
}

@Composable
private fun WhereDialogBottomBar(
	onMove : () -> Unit,
) {
	val isVaultOpened = LocalVaultIsOpened.current
	val onAuthenticatorAction = LocalAuthenticatorAction.current

	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.height(80.dp)
			.background(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1)),
	) {
		Spacer(modifier = Modifier.width(16.dp))
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.height(44.dp)
				.weight(1f)
				.background(MaterialTheme.colorScheme.background.copy(alpha = 0.71f), RoundedCornerShape(12.dp))
				.clip(RoundedCornerShape(12.dp))
				.clickable { onMove() }
		) {
			Spacer(modifier = Modifier.width(16.dp))
			Text(
				text = "Search in here",
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Normal
			)
			Spacer(modifier = Modifier.width(16.dp))
		}
		Spacer(modifier = Modifier.width(48.dp))
		MenuButton(
			icon = R.drawable.ic_vault,
			contentDescription = "Vault",
			tint = if (isVaultOpened) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface,
			containerColor = if (isVaultOpened) MaterialTheme.colorScheme.background else Color.Companion.Transparent
		) { onAuthenticatorAction(Authenticator.AUTHENTICATE) }
		Spacer(modifier = Modifier.width(16.dp))
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterItem(
	chapterObject : ChapterObjectLite,
	onClick : () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(12.dp, 4.dp)
	) {
		OutlinedCard(
			shape = RoundedCornerShape(12.dp),
			border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.17f)),
			colors = CardDefaults.cardColors(containerColor = Color.Transparent),
			elevation = CardDefaults.outlinedCardElevation(defaultElevation = 0.dp),
			onClick = onClick,
			modifier = Modifier
				.fillMaxWidth()
				.clip(RoundedCornerShape(12.dp))
		) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			) {
				Row(
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = chapterObject.title ?: "Untitled",
						style = MaterialTheme.typography.bodyLarge,
						fontWeight = FontWeight.Bold,
						fontStyle = if (chapterObject.title.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
						color = MaterialTheme.colorScheme.onBackground
					)

					Spacer(modifier = Modifier.weight(1f))

					if (chapterObject.isLocked) {
						Icon(
							painter = painterResource(id = R.drawable.ic_shield),
							contentDescription = "Locked",
							tint = Color.LockClosedContainer,
							modifier = Modifier.requiredSize(14.dp)
						)
						if (chapterObject.isFavourite) {
							Spacer(modifier = Modifier.width(2.dp))
							Text(
								text = "·",
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onBackground,
								fontWeight = FontWeight.Bold,
								maxLines = 1,
								modifier = Modifier
							)
							Spacer(modifier = Modifier.width(2.dp))
							Icon(
								painter = painterResource(id = R.drawable.ic_favourite),
								contentDescription = "Favourite",
								tint = Color.FavouriteContainer,
								modifier = Modifier.requiredSize(14.dp)
							)
						}
					}
				}
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = chapterObject.description.let { if (it.isNullOrBlank()) "No description" else it },
					style = MaterialTheme.typography.bodyMedium,
					fontStyle = if (chapterObject.description.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
					color = MaterialTheme.colorScheme.onBackground
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = chapterObject.id.toString(),
					style = MaterialTheme.typography.bodySmall,
					fontStyle = FontStyle.Italic,
					color = MaterialTheme.colorScheme.onBackground,
					overflow = TextOverflow.Ellipsis,
					maxLines = 1,
				)
			}
		}
	}
}
