package com.syncodec.graphite.presentation.raw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.syncodec.graphite.presentation.common.LoadingView
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.Extra
import io.realm.kotlin.types.RealmUUID

class RawActivity : ComponentActivity() {

	private val viewModel : RawViewModel by viewModels()

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		try {
			val hasRealmUUID = intent.hasExtra(Extra.Companion.Extra.OBJECT_ID.name)
			val hasObjectType = intent.hasExtra(Extra.Companion.Extra.OBJECT_TYPE.name)

			if (hasRealmUUID && hasObjectType) {
				val RealmUUID = intent.getStringExtra(Extra.Companion.Extra.OBJECT_ID.name)?.let { RealmUUID.from(it) }
				val objectType = intent.getStringExtra(Extra.Companion.Extra.OBJECT_TYPE.name)?.let { Extra.Companion.ObjectType.valueOf(it) }

				if (RealmUUID != null && objectType != null) {
//					viewModel.readObject(RealmUUID, objectType)
				} else {
					finish()
				}
			} else {
				finish()
			}
		} catch (e : Exception) {
//			e.printStackTrace()
			finish()
		}

		setContent {
			BaseContent {
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)
				systemUiController.setNavigationBarColor(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground)

				val jsonObject by viewModel.jsonObject

				Crossfade(targetState = jsonObject) {
					if (it == null) {
						LoadingView()
					} else {
						RawScreen(jsonObject = it, onClickBack = this::onBackPressed)
					}
				}
			}
		}
	}
}
