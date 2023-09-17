package com.syncodec.graphite.presentation.report

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.google.firebase.functions.FirebaseFunctions
import com.syncodec.graphite.R
import com.syncodec.graphite.presentation.report.composable.ReportScreen
import com.syncodec.graphite.presentation.base.BaseComposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ReportActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState : Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			BaseComposable {
				ReportScreen(
					onClickSubmit = { reportType, reportComponent, title, email, description ->
						val data = hashMapOf(
							"bugType" to reportType.name,
							"bugComponent" to reportComponent.name,
							"title" to title,
							"email" to email,
							"description" to description
						)
						FirebaseFunctions
							.getInstance()
							.getHttpsCallable("submitBugReport")
							.call(data)
							.addOnSuccessListener {
								lifecycleScope.launch(Dispatchers.Main) {
									Toast.makeText(this@ReportActivity, "Report submitted", Toast.LENGTH_SHORT).show()
									finish()
								}
							}
							.addOnFailureListener {
								lifecycleScope.launch(Dispatchers.Main) {
									Toast.makeText(this@ReportActivity, "Error submitting report. You can mail us on support@syncodec.com", Toast.LENGTH_SHORT).show()
								}
							}
					}
				)
			}
		}
	}

	companion object {
		enum class ReportType {
			Bug,
			FeatureRequest,
			Other
		}

		enum class ReportComponent {
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
			Sync,
			Other
		}

		val reportTypeIconMap = mapOf(
			ReportType.Bug to R.drawable.ic_bug,
			ReportType.FeatureRequest to R.drawable.ic_sparkle,
			ReportType.Other to R.drawable.ic_exclamation
		)

		val reportComponentIconMap = mapOf(
			ReportComponent.Note to R.drawable.ic_pencil,
			ReportComponent.Chapter to R.drawable.ic_note,
			ReportComponent.Notebook to R.drawable.ic_notebook,
			ReportComponent.BucketList to R.drawable.ic_bucket,
			ReportComponent.Attachment to R.drawable.ic_gallery,
			ReportComponent.Tag to R.drawable.ic_tag,
			ReportComponent.BackupAndSync to R.drawable.ic_local_backup,
			ReportComponent.UI to R.drawable.ic_theme,
			ReportComponent.Search to R.drawable.ic_search,
			ReportComponent.Vault to R.drawable.ic_vault,
			ReportComponent.Data to R.drawable.ic_data,
			ReportComponent.Sync to R.drawable.ic_cloud,
			ReportComponent.Other to R.drawable.ic_exclamation
		)

		val reportTypeNameMap = mapOf(
			ReportType.Bug to "BUG",
			ReportType.FeatureRequest to "FEATURE REQUEST",
			ReportType.Other to "OTHER"
		)

		val reportComponentNameMap = mapOf(
			ReportComponent.Note to "NOTE",
			ReportComponent.Chapter to "CHAPTER",
			ReportComponent.Notebook to "NOTEBOOK",
			ReportComponent.BucketList to "BUCKET LIST",
			ReportComponent.Attachment to "ATTACHMENT",
			ReportComponent.Tag to "TAG",
			ReportComponent.BackupAndSync to "BACKUP AND SYNC",
			ReportComponent.UI to "UI",
			ReportComponent.Search to "SEARCH",
			ReportComponent.Vault to "VAULT",
			ReportComponent.Data to "DATA",
			ReportComponent.Sync to "SYNC",
			ReportComponent.Other to "OTHER"
		)
	}
}
