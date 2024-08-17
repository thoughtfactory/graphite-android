package com.syncodec.graphite.utils

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.syncodec.graphite.presentation.note2.NoteActivity2
import io.realm.kotlin.types.RealmUUID

object ActivityUtil {
    @Composable
    inline fun <reified T : ComponentActivity> launchActivity(
        withExtra: Intent.() -> Map<String, Any> = { mapOf() }
    ) {
        val context = LocalContext.current
        val intent = Intent(context, T::class.java).apply {
            val extraMap = withExtra()
            extraMap.forEach { key, value ->
                when (value) {
                    is Int -> putExtra(key, value)
                    is String -> putExtra(key, value)
                    is Boolean -> putExtra(key, value)
                }
            }
        }
        context.startActivity(intent)
    }

    inline fun <reified T : ComponentActivity> launchActivity(
        context: Context,
        withExtra: Intent.() -> Map<String, Any> = { mapOf() }
    ) {
        val intent = Intent(context, T::class.java).apply {
            val extraMap = withExtra()
            extraMap.forEach { key, value ->
                when (value) {
                    is Int -> putExtra(key, value)
                    is String -> putExtra(key, value)
                    is Boolean -> putExtra(key, value)
                }
            }
        }
        context.startActivity(intent)
    }

    inline fun launchNewNoteActivity(
        context: Context,
        parentId: RealmUUID
    ) {
        val intent = Intent(context, NoteActivity2::class.java).apply {
            putExtra(NoteExtra.IsNew.name, true)
            putExtra(NoteExtra.IsEditing.name, true)
            putExtra(NoteExtra.ParentIdBytes.name, parentId.bytes)
        }
        context.startActivity(intent)
    }

    enum class NoteExtra {
        IsNew,
        IsEditing,
        Id,
        ParentIdBytes,
    }
}
