package com.syncodec.graphite.presentation.tags

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.syncodec.graphite.presentation.tags.composable.screen.TagScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class TagsActivity : ComponentActivity() {

	private val viewModel : TagsViewModel by viewModel()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BaseContent {
				val tagList by viewModel.tagList.collectAsState()

				TagScreen(
					tagList = tagList,
					putTag = {
						viewModel.putTag(it) { withContext(Dispatchers.Main) { Toast.makeText(this@TagsActivity, it, Toast.LENGTH_SHORT).show() } }
					},
					deleteTag = viewModel::deleteTag,
				)
			}
		}
	}
}
