package com.syncodec.graphite.presentation.explorer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.explorer.screen.explorerScreen.ExplorerScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID


class ExplorerActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		var explorerType : Extra.Companion.ExplorerType?

		intent.getStringExtra(Extra.Companion.Extra.ExplorerType.name).let {
			explorerType = when (it) {
				Extra.Companion.ExplorerType.Atlas.name -> Extra.Companion.ExplorerType.Atlas
				Extra.Companion.ExplorerType.Attachment.name -> Extra.Companion.ExplorerType.Attachment
				Extra.Companion.ExplorerType.Calendar.name -> Extra.Companion.ExplorerType.Calendar
				Extra.Companion.ExplorerType.Search.name -> Extra.Companion.ExplorerType.Search
				else -> {
					Toast.makeText(applicationContext, "Error loading data", Toast.LENGTH_SHORT).show()
					finish()
					null
				}
			}
		}

		val chapterId = intent.getByteArrayExtra(Extra.Companion.Extra.ChapterId.name)?.let { RealmUUID.from(it) }

		setContent {
			setContent {
				BaseContent {

					val systemUiController = rememberSystemUiController()
					systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
					systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp))

					var isSelecting : Boolean by remember { mutableStateOf(false) }
					var selectedIdList : List<RealmUUID> by remember { mutableStateOf(listOf()) }

					BackHandler(enabled = isSelecting) {
						isSelecting = false
						selectedIdList = listOf()
					}

					ExplorerScreen(
						explorerType = explorerType,
						initSearchInChapterId = chapterId,
						searchInDefaultChapter = false,
						isStatic = false,
						isSelecting = isSelecting,
						onSelect = {
							isSelecting = true
							selectedIdList.toMutableList().apply {
								if (it in this) remove(it) else add(it)
								selectedIdList = this
							}
						},
						selectedIdList = selectedIdList,
						onClickCancelSelect = {
							isSelecting = false
							selectedIdList = listOf()
						},
					)
				}
			}
		}
	}
}
