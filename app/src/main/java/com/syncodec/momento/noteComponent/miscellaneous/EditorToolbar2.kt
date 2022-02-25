//package com.syncodec.momento.noteComponent.miscellaneous
//
//import android.app.DatePickerDialog
//import android.app.TimePickerDialog
//import androidx.compose.animation.AnimatedContent
//import androidx.compose.animation.animateColorAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.*
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.ExperimentalMaterialApi
//import androidx.compose.material.Icon
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.painter.Painter
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.syncodec.momento.R
//import com.syncodec.momento.custom.richText.RichTextEditor
//import com.syncodec.momento.konstant.ErrorCode
//import com.syncodec.momento.noteComponent.NoteViewModel
//import com.syncodec.momento.noteComponent.modalBottomSheet.BottomSheetType
//import kotlinx.coroutines.launch
//import java.text.SimpleDateFormat
//import java.util.*
//
//
//data class ToolItem(
//	val title: String?,
//	val icon: ImageVector,
//	var highlight: Boolean = false,
//	var _highlight: MutableState<Boolean> = mutableStateOf(false),
//	val showText: Boolean = false,
//	val fullSize: Boolean = false,
//	var dropDownMenu: @Composable () -> Unit = {},
//	val onClick: () -> Unit
//)
//
//data class TagData(
//	val tag: String,
//	val color: Color,
//)
//
//private enum class ToolbarEditor {
//	NULL,
//	STATE,
//	TAG,
//	LINK
//}
//
//@OptIn(ExperimentalMaterialApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
//@Composable
//fun EditorToolbar(
//	richTextEditor: RichTextEditor,
//	onError: (ErrorCode.Companion.ErrorCode) -> Unit,
//) {
//	val context = LocalContext.current
//	val scope = rememberCoroutineScope()
//	val viewModel: NoteViewModel = viewModel()
//	var textFormat by remember { mutableStateOf(RichTextEditor.TextFormat()) }
//
//	var boldState by remember { mutableStateOf(textFormat.bold) }
//	var italicState by remember { mutableStateOf(textFormat.italic) }
//	var underlineState by remember { mutableStateOf(textFormat.underline) }
//	var bulletListState by remember { mutableStateOf(textFormat.bulletList) }
//	var orderedListState by remember { mutableStateOf(textFormat.orderedList) }
//	var taskListState by remember { mutableStateOf(textFormat.taskList) }
//
//	var toolbarEditor by remember { mutableStateOf(ToolbarEditor.NULL) }
//
//	val tagList: List<TagData> = listOf(
//		TagData(tag = "alpha", Color(150, 206, 180)),
//		TagData(tag = "beta", Color(255, 171, 118)),
//		TagData(tag = "gamma", Color(154, 208, 236)),
//		TagData(tag = "delta", Color(227, 190, 198)),
//		TagData(tag = "epsilon", Color(200, 75, 49)),
//	)
//
//	richTextEditor.setOnFormatUpdate(object : RichTextEditor.OnFormatUpdateListener {
//		override fun onFormatUpdate(newTextFormat: RichTextEditor.TextFormat) {
//			textFormat = newTextFormat
//
//			boldState = textFormat.bold
//			italicState = textFormat.italic
//			underlineState = textFormat.underline
//			bulletListState = textFormat.bulletList
//			orderedListState = textFormat.orderedList
//			taskListState = textFormat.taskList
//		}
//	})
//
//	Box(
//		modifier = Modifier
//			.fillMaxWidth()
//			.background(MaterialTheme.colorScheme.secondaryContainer)
//	) {
//		Column(
//			modifier = Modifier
//				.fillMaxWidth()
//		) {
//			AnimatedContent(targetState = toolbarEditor) {
//				when (it) {
//					ToolbarEditor.NULL -> null
//					ToolbarEditor.STATE -> StateToolbar()
//					ToolbarEditor.TAG -> TagToolbar(tagList = tagList)
//					ToolbarEditor.LINK -> null
//				}
//			}
//
//			Row(
//				modifier = Modifier
//					.fillMaxWidth()
//					.padding(0.dp, 8.dp, 0.dp, 0.dp)
//					.horizontalScroll(rememberScrollState()),
//				verticalAlignment = Alignment.CenterVertically
//			) {
//				Spacer(modifier = Modifier.width(4.dp))
//
//				DateTimeButton(userTimestamp = viewModel.userTimestamp.value) {
//					val timestamp = viewModel.userTimestamp.value
//					val calendar = Calendar.getInstance()
//					calendar.timeInMillis = timestamp
//
//					DatePickerDialog(
//						context,
//						{ _, year, month, day ->
//							calendar.apply {
//								set(Calendar.YEAR, year)
//								set(Calendar.MONTH, month)
//								set(Calendar.DAY_OF_MONTH, day)
//
//								viewModel.userTimestamp.value = timeInMillis
//
//								TimePickerDialog(
//									context,
//									{ _, hour, minute ->
//										set(Calendar.HOUR_OF_DAY, hour)
//										set(Calendar.MINUTE, minute)
//
//										viewModel.userTimestamp.value = timeInMillis
//
//									},
//									calendar.get(Calendar.HOUR_OF_DAY),
//									calendar.get(Calendar.MINUTE),
//									false
//								).show()
//							}
//						},
//						calendar.get(Calendar.YEAR),
//						calendar.get(Calendar.MONDAY),
//						calendar.get(Calendar.DAY_OF_MONTH)
//					).show()
//				}
//				ToolbarButton(
//					icon = painterResource(id = R.drawable.ic_attachment),
//					description = "Add attachment",
//					highlight = viewModel.attachmentList.isNotEmpty()
//				) {
//					scope.launch {
//						viewModel.activityState.bottomSheetType.value = BottomSheetType.AttachmentBottomSheet
//						viewModel.activityState.bottomSheetState.show()
//					}
//				}
//				ToolbarButton(
//					icon = painterResource(id = R.drawable.ic_state),
//					description = "Entry state",
//					highlight = false
//				) {
//					toolbarEditor = if (toolbarEditor == ToolbarEditor.STATE) ToolbarEditor.NULL else ToolbarEditor.STATE
//				}
//				ToolbarButton(
//					icon = painterResource(id = R.drawable.ic_hashtag),
//					description = "Add tag",
//					highlight = false
//				) {
//					toolbarEditor = if (toolbarEditor == ToolbarEditor.TAG) ToolbarEditor.NULL else ToolbarEditor.TAG
//				}
//
//				ToolbarSpacer()
//
//				ToolbarButton(icon = painterResource(id = R.drawable.ic_format_bold), description = "Bold", highlight = boldState)
//				{ richTextEditor.exec("editor.commands.toggleBold();") }
//				ToolbarButton(icon = painterResource(id = R.drawable.ic_format_italic), description = "Italic", highlight = italicState)
//				{ richTextEditor.exec("editor.commands.toggleItalic();") }
//				ToolbarButton(icon = painterResource(id = R.drawable.ic_format_underline), description = "Underline", highlight = underlineState)
//				{ richTextEditor.exec("editor.commands.toggleUnderline();") }
//
//				ToolbarSpacer()
//
//				ToolbarButton(icon = painterResource(id = R.drawable.ic_format_list_bullet), description = "Bullet List", highlight = bulletListState)
//				{ richTextEditor.exec("editor.commands.toggleBulletList();") }
//				ToolbarButton(icon = painterResource(id = R.drawable.ic_format_list_ordered), description = "Ordered List", highlight = orderedListState)
//				{ richTextEditor.exec("editor.commands.toggleOrderedList();") }
//				ToolbarButton(icon = painterResource(id = R.drawable.ic_format_list_task), description = "Task List", highlight = taskListState)
//				{ richTextEditor.exec("editor.commands.toggleTaskList();") }
//			}
//		}
//	}
//}
//
//@Composable
//private fun DateTimeButton(
//	userTimestamp: Long,
//	onClick: () -> Unit
//) {
//	Box(
//		modifier = Modifier
//			.height(48.dp)
//			.clip(RoundedCornerShape(8.dp))
//			.clickable { onClick() },
//		contentAlignment = Alignment.Center
//	) {
//		Column(
//			modifier = Modifier
//				.fillMaxHeight()
//				.padding(8.dp, 0.dp),
//			horizontalAlignment = Alignment.Start,
//			verticalArrangement = Arrangement.Center
//		) {
//			Text(
//				text = SimpleDateFormat("h:mm a, EEE").format(userTimestamp),
//				style = MaterialTheme.typography.bodyMedium,
//				fontWeight = FontWeight.Bold,
//				color = MaterialTheme.colorScheme.onSecondaryContainer
//			)
//			Text(
//				text = SimpleDateFormat("MMM d, yyyy").format(userTimestamp),
//				style = MaterialTheme.typography.bodySmall,
//				fontWeight = FontWeight.Bold,
//				color = MaterialTheme.colorScheme.onSecondaryContainer
//			)
//		}
//	}
//}
//
//@Composable
//private fun ToolbarButton(
//	icon: Painter,
//	description: String,
//	highlight: Boolean,
//	onClick: () -> Unit
//) {
//	val interactionSource = remember { MutableInteractionSource() }
//	val containerColor = animateColorAsState(
//		targetValue = if (highlight) MaterialTheme.colorScheme.onSecondaryContainer else Color.Companion.Transparent,
//		animationSpec = tween(200)
//	)
//	val contentColor = animateColorAsState(
//		targetValue = if (highlight) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
//		animationSpec = tween(200)
//	)
//
//	Surface(
//		shape = RoundedCornerShape(8.dp),
//		color = containerColor.value,
//		modifier = Modifier
//			.requiredSize(48.dp)
//			.padding(2.dp)
//			.clip(RoundedCornerShape(8.dp))
//			.clickable(
//				interactionSource = interactionSource,
//				indication = null
//			) { onClick() },
//		shadowElevation = 0.dp
//	) {
//		Icon(
//			painter = icon,
//			contentDescription = description,
//			tint = contentColor.value,
//			modifier = Modifier
//				.requiredSize(24.dp)
//		)
//	}
//}
//
//@Composable
//private fun ToolbarSpacer() {
//	Spacer(
//		modifier = Modifier
//			.width(10.dp)
//			.height(32.dp)
//			.padding(4.dp, 2.dp)
//			.background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.47f))
//			.clip(RoundedCornerShape(50))
//	)
//}
//
//@Composable
//private fun StateToolbar() {
//	val viewModel: NoteViewModel = viewModel()
//	Box(
//		modifier = Modifier
//			.fillMaxWidth()
//			.background(MaterialTheme.colorScheme.secondaryContainer)
//	) {
//		Row(
//			modifier = Modifier
//				.fillMaxWidth()
//				.padding(0.dp, 8.dp, 0.dp, 0.dp)
//				.horizontalScroll(rememberScrollState()),
//			verticalAlignment = Alignment.CenterVertically
//		) {
//			Spacer(modifier = Modifier.width(4.dp))
//
//			ToolbarButton(
//				icon = painterResource(id = if (viewModel.isLocked) R.drawable.ic_locked else R.drawable.ic_unlocked),
//				description = if (viewModel.isLocked) "Entry is locked" else "Entry is not locked",
//				highlight = viewModel.isLocked
//			) {
//				viewModel.isLocked = !viewModel.isLocked
//			}
//			ToolbarButton(
//				icon = painterResource(id = R.drawable.ic_box),
//				description = if (viewModel.isArchived) "Entry is archived" else "Entry is not archived",
//				highlight = viewModel.isArchived
//			) {
//				viewModel.isArchived = !viewModel.isArchived
//			}
//			ToolbarButton(
//				icon = painterResource(id = if (viewModel.isFavourite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite),
//				description = if (viewModel.isFavourite) "Entry is favourite" else "Entry is not favourite",
//				highlight = viewModel.isFavourite
//			) {
//				viewModel.isFavourite = !viewModel.isFavourite
//			}
//
//			Spacer(modifier = Modifier.width(4.dp))
//		}
//
//	}
//}
//
//@Composable
//private fun TagToolbar(
//	tagList: List<TagData>
//) {
//	Row(
//		modifier = Modifier
//			.fillMaxWidth()
//			.height(56.dp)
//			.horizontalScroll(rememberScrollState()),
//		verticalAlignment = Alignment.CenterVertically
//	) {
//		Spacer(modifier = Modifier.width(4.dp))
//		NewTagCard()
//		tagList.forEach {
//			TagCard(
//				tag = it.tag,
//				color = it.color,
//				isSelected = false
//			) {
//
//			}
//		}
//		Spacer(modifier = Modifier.width(4.dp))
//	}
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun NewTagCard() {
//	Card(
//		modifier = Modifier
//			.fillMaxHeight()
//			.padding(4.dp, 8.dp, 4.dp, 4.dp)
//			.clip(RoundedCornerShape(50))
//			.clickable { },
//		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
//		shape = RoundedCornerShape(50),
//		containerColor = Color.Transparent,
//		border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSecondaryContainer),
//	) {
//		Box(
//			modifier = Modifier
//				.fillMaxHeight()
//				.padding(12.dp, 0.dp),
//			contentAlignment = Alignment.Center
//		) {
//			Icon(
//				painter = painterResource(id = R.drawable.ic_add),
//				contentDescription = "Add new tag",
//				tint = MaterialTheme.colorScheme.onSecondaryContainer,
//				modifier = Modifier
//			)
//		}
//	}
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun TagCard(
//	tag: String,
//	color: Color,
//	isSelected: Boolean,
//	onClick: () -> Unit
//) {
//	Card(
//		modifier = Modifier
//			.fillMaxHeight()
//			.padding(4.dp, 8.dp, 4.dp, 4.dp),
//		elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
//		shape = RoundedCornerShape(50),
//		containerColor = color,
//		border = BorderStroke(2.dp, color)
//	) {
//		Box(
//			modifier = Modifier
//				.fillMaxHeight()
//				.padding(12.dp, 0.dp),
//			contentAlignment = Alignment.Center
//		) {
//			Text(
//				text = tag,
//				style = MaterialTheme.typography.bodyMedium,
//				fontWeight = FontWeight.Bold,
//				color = Color.White
//			)
//		}
//	}
//}
