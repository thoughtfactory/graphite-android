package com.syncodec.graphite.presentation.tags

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.syncodec.graphite.presentation.tags.composable.TagScreen
import com.syncodec.graphite.presentation.base.BaseComposable
import org.koin.androidx.viewmodel.ext.android.viewModel


class TagsActivity : ComponentActivity() {

	private val viewModel : TagsViewModel by viewModel()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BaseComposable {
				val tagList by viewModel.tagList.collectAsState()

				TagScreen(
					tagList = tagList,
					putTag = viewModel::putTag,
					onUpdateTag = viewModel::updateTag,
					onDeleteTag = viewModel::deleteTag,
				)
			}
		}
	}
}
