package com.syncodec.graphite.presentation.tags.composable

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.syncodec.graphite.R
import com.syncodec.graphite.di.model.local.TagObject
import com.syncodec.graphite.presentation.common.scaffold.GenericScaffold2
import com.syncodec.graphite.presentation.search.SearchActivity
import com.syncodec.graphite.presentation.tags.composable.bar.TopBar
import com.syncodec.graphite.presentation.tags.composable.bottomSheet.AddTagBottomSheet
import com.syncodec.graphite.presentation.tags.composable.bottomSheet.EditTagBottomSheet
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.EmptyView
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.TagBar
import com.syncodec.graphite.presentation.tags.composable.buildingBlock.TagItemView
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TagScreen(
	tagList: List<TagObject> = listOf(),
	putTag: (String, Color) -> Boolean = { _, _ -> false },
	onUpdateTag: (RealmUUID, String, Color) -> Unit = { _, _, _ -> },
	onDeleteTag: (RealmUUID) -> Unit = {},
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	var tagText by remember { mutableStateOf("") }

	val bottomSheetState = rememberModalBottomSheetState()
	var isNewTagBottomSheetVisible by remember { mutableStateOf(false) }
	var isEditTagBottomSheetVisible by remember { mutableStateOf(false) }

	var selectedTagObject by remember { mutableStateOf<TagObject?>(null) }

	GenericScaffold2(
		topBar = { TopBar() }
	) {
		Column(
			modifier = Modifier.fillMaxSize()
		) {
			TagBar(
				value = tagText,
				onValueChange = { tagText = it.lowercase().split(" ").firstOrNull() ?: "" },
				onClickAddTag = { isNewTagBottomSheetVisible = true },
			)
			if (tagList.isEmpty()) {
				EmptyView()
			} else {
				LazyColumn(
					modifier = Modifier.weight(1f)
				) {
					items(
						items = tagList.filter { it.tag.contains(tagText) }
					) { tagObject ->
						TagItemView(
							tagObject = tagObject,
							onClick = {
								Intent(context, SearchActivity::class.java).apply {
									putExtra(Extra.Companion.Extra.TagId.name, tagObject.id.bytes)
									context.startActivity(this)
								}
							},
							onLongClick = {
								selectedTagObject = tagObject
								isEditTagBottomSheetVisible = true
							}
						)
					}
				}
			}
		}
	}

	AddTagBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isNewTagBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isNewTagBottomSheetVisible = false } },
		tagText = tagText,
		onChangeTagText = { tagText = it.lowercase().split(" ").firstOrNull() ?: "" },
		onCreateTag = { tag, color ->
			if (!putTag(tag, color)) Toast.makeText(context, context.getText(R.string.toast_duplicate_tag), Toast.LENGTH_SHORT).show()
			else {
				scope.launch { bottomSheetState.hide(); isNewTagBottomSheetVisible = false }
				tagText = ""
			}
		},
	)

	EditTagBottomSheet(
		bottomSheetState = bottomSheetState,
		isBottomSheetVisible = isEditTagBottomSheetVisible,
		onDismissRequest = { scope.launch { bottomSheetState.hide(); isEditTagBottomSheetVisible = false } },
		tagObject = selectedTagObject,
		onUpdateTag = onUpdateTag,
		onDeleteTag = {
			onDeleteTag(it)
			isEditTagBottomSheetVisible = false
		},
	)
}
