package com.syncodec.graphite.presentation.tags

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels


class TagsActivity: ComponentActivity() {

	private val viewModel by viewModels<TagsViewModel>()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)


	}
}
