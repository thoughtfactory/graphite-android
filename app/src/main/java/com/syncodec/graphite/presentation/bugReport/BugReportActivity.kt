package com.syncodec.graphite.presentation.bugReport

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.functions.FirebaseFunctions
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.bugReport.composable.BugReportScreen
import com.syncodec.graphite.presentation.ui.BaseContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BugReportActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BaseContent {
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
							}
					}
				)
			}
		}
	}

	companion object {
		enum class BugType {
			Bug,
			FeatureRequest,
			Other
		}

		enum class BugComponent {
			Note,
			Chapter,
			Notebook,
			BucketList,
			Attachment,
			Tag,
			BackupAndSync,
			UI,
			Search,
			Vault,
			Data,
			Other
		}

		val bugTypeIconMap = mapOf(
			BugType.Bug to R.drawable.ic_bug,
			BugType.FeatureRequest to R.drawable.ic_sparkle,
			BugType.Other to R.drawable.ic_exclamation
		)

		val bugComponentIconMap = mapOf(
			BugComponent.Note to R.drawable.ic_pencil,
			BugComponent.Chapter to R.drawable.ic_note,
			BugComponent.Notebook to R.drawable.ic_notebook,
			BugComponent.BucketList to R.drawable.ic_bucket,
			BugComponent.Attachment to R.drawable.ic_gallery,
			BugComponent.Tag to R.drawable.ic_tag,
			BugComponent.BackupAndSync to R.drawable.ic_local_backup,
			BugComponent.UI to R.drawable.ic_theme,
			BugComponent.Search to R.drawable.ic_search,
			BugComponent.Vault to R.drawable.ic_vault,
			BugComponent.Data to R.drawable.ic_data,
			BugComponent.Other to R.drawable.ic_exclamation
		)

		val bugTypeNameMap = mapOf(
			BugType.Bug to "BUG",
			BugType.FeatureRequest to "FEATURE REQUEST",
			BugType.Other to "OTHER"
		)

		val bugComponentNameMap = mapOf(
			BugComponent.Note to "NOTE",
			BugComponent.Chapter to "CHAPTER",
			BugComponent.Notebook to "NOTEBOOK",
			BugComponent.BucketList to "BUCKET LIST",
			BugComponent.Attachment to "ATTACHMENT",
			BugComponent.Tag to "TAG",
			BugComponent.BackupAndSync to "BACKUP AND SYNC",
			BugComponent.UI to "UI",
			BugComponent.Search to "SEARCH",
			BugComponent.Vault to "VAULT",
			BugComponent.Data to "DATA",
			BugComponent.Other to "OTHER"
		)
	}
}
