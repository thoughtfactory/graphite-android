package com.syncodec.graphite.presentation.bugReport

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.functions.FirebaseFunctions
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bugReport.composable.BugReportScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import com.syncodec.graphite.utils.tone
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class BugReportActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BaseContent {
				val scope = rememberCoroutineScope()
				val systemUiController = rememberSystemUiController()
				systemUiController.setStatusBarColor(MaterialTheme.colorScheme.background)
				systemUiController.setNavigationBarColor(MaterialTheme.colorScheme.surface.tone(isSystemInDarkTheme(), 1))

				BugReportScreen(
					onClickBack = { finish() },
					onClickSubmit = { bugType, bugComponent, title, description ->
						val data = hashMapOf(
							"bugType" to bugType.name,
							"bugComponent" to bugComponent.name,
							"title" to title,
							"description" to description
						)
						FirebaseFunctions
							.getInstance()
							.getHttpsCallable("submitBugReport")
							.call(data)
							.addOnSuccessListener {
								CoroutineScope(Dispatchers.Main).launch {
									Toast.makeText(this@BugReportActivity, "Report submitted", Toast.LENGTH_SHORT).show()
									finish()
								}
							}
							.addOnFailureListener {
								it.printStackTrace()
							}
					}
				)
			}
		}
	}

	companion object {
		enum class BugType {
			BUG,
			FEATURE_REQUEST,
			OTHER
		}

		enum class BugComponent {
			NOTE,
			CHAPTER,
			NOTEBOOK,
			BUCKET_LIST,
			ATTAHCMENT,
			TAG,
			BACKUP_AND_SYNC,
			UI,
			SEARCH,
			VAULT,
			DATA,
			OTHER
		}

		val bugTypeIconMap = mapOf(
			BugType.BUG to R.drawable.ic_bug,
			BugType.FEATURE_REQUEST to R.drawable.ic_sparkle,
			BugType.OTHER to R.drawable.ic_exclamation
		)

		val bugComponentIconMap = mapOf(
			BugComponent.NOTE to R.drawable.ic_pencil,
			BugComponent.CHAPTER to R.drawable.ic_note,
			BugComponent.NOTEBOOK to R.drawable.ic_notebook,
			BugComponent.BUCKET_LIST to R.drawable.ic_bucket,
			BugComponent.ATTAHCMENT to R.drawable.ic_gallery,
			BugComponent.TAG to R.drawable.ic_tag,
			BugComponent.BACKUP_AND_SYNC to R.drawable.ic_local_backup,
			BugComponent.UI to R.drawable.ic_theme,
			BugComponent.SEARCH to R.drawable.ic_search,
			BugComponent.VAULT to R.drawable.ic_vault,
			BugComponent.DATA to R.drawable.ic_data,
			BugComponent.OTHER to R.drawable.ic_exclamation
		)

		val bugTypeNameMap = mapOf(
			BugType.BUG to "BUG",
			BugType.FEATURE_REQUEST to "FEATURE REQUEST",
			BugType.OTHER to "OTHER"
		)

		val bugComponentNameMap = mapOf(
			BugComponent.NOTE to "NOTE",
			BugComponent.CHAPTER to "CHAPTER",
			BugComponent.NOTEBOOK to "NOTEBOOK",
			BugComponent.BUCKET_LIST to "BUCKET LIST",
			BugComponent.ATTAHCMENT to "ATTACHMENT",
			BugComponent.TAG to "TAG",
			BugComponent.BACKUP_AND_SYNC to "BACKUP AND SYNC",
			BugComponent.UI to "UI",
			BugComponent.SEARCH to "SEARCH",
			BugComponent.VAULT to "VAULT",
			BugComponent.DATA to "DATA",
			BugComponent.OTHER to "OTHER"
		)
	}
}
