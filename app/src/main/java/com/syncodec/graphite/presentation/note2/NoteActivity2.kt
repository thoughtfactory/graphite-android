package com.syncodec.graphite.presentation.note2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.syncodec.graphite.presentation.base.BaseComposable
import com.syncodec.graphite.presentation.base.LocalIsDarkTheme
import com.syncodec.graphite.presentation.note2.kitKat.KitKat
import com.syncodec.graphite.presentation.note2.model.NoteViewModel2
import com.syncodec.graphite.presentation.note2.screen.noteEditorScreen.NoteEditorScreen
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.compose.runtime.getValue
import com.syncodec.graphite.presentation.note2.kitKat.KitKatAction
import com.syncodec.graphite.utils.ActivityUtil
import io.realm.kotlin.types.RealmUUID
import kotlinx.coroutines.delay


class NoteActivity2 : ComponentActivity() {

    private val viewModel: NoteViewModel2 by viewModel()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        readActivity()

        val kitKat = KitKat(this)
        kitKat.loadExternalEditor()
        kitKat.isReady

        setContent {

            BaseComposable {

                val isDarkTheme = LocalIsDarkTheme.current

//                val isEditing by viewModel.isEditing.collectAsState()
                val isKitKatReady by kitKat.isReady.collectAsState()

//                val noteObject by viewModel.noteObject.collectAsState()
//                val locationData by viewModel.locationData.collectAsState()
//                val parentChapter by viewModel.parentChapter.collectAsState()
//
//                val attachmentList by viewModel.attachmentList.collectAsState()
//
//                val allTagList by viewModel.allTagsList.collectAsState()
//                val tagStateMap by viewModel.tagStateMap.collectAsState()
//
//                val storedTitle by remember { derivedStateOf { noteObject?.title } }
//                val storedContent by remember { derivedStateOf { noteObject?.content } }

                val kitKatFormat by kitKat.kitKatFormatFlow.collectAsState()

//                var isReCreateDataRestored by remember { mutableStateOf(false) }

                LaunchedEffect(key1 = isKitKatReady) {
                    if (isKitKatReady) {
//						!!! These delays are disgusting
                        delay(310)
                        kitKat.onKitKatActionAsync(KitKatAction.Other.SetMaxHeight)
                        if (isDarkTheme) kitKat.onKitKatActionAsync(KitKatAction.Other.EnableDarkMode)
                        else kitKat.onKitKatActionAsync(KitKatAction.Other.DisableDarkMode)
//                        if (isRecreated) {
//                            Log.d("npr71", "recreated : setting cached title and content")
//                            viewModel.getKitKatFormat().let { cachedKitKatFormat ->
//                                kitKat.onKitKatActionAsync(KitKatAction.Edit.SetTitle(cachedKitKatFormat.kitKatTitle))
//                                kitKat.onKitKatActionAsync(KitKatAction.Edit.SetContent.Html(content = cachedKitKatFormat.kitKatContent?.drop(1)?.dropLast(1)))
//                                Log.d("npr71", "recreated : ${cachedKitKatFormat.kitKatContent}")
//                            }
//                        }
//                        isReCreateDataRestored = true
                    }
                }

                LaunchedEffect(key1 = isKitKatReady, key2 = true) {
                    if (isKitKatReady) {
//						!!! These delays are disgusting
                        delay(470)
                        kitKat.onKitKatActionAsync(KitKatAction.Edit.Enable)
//                        if (isEditing == true) kitKat.onKitKatActionAsync(KitKatAction.Edit.Enable)
//                        else kitKat.onKitKatActionAsync(KitKatAction.Edit.Disable)
                    }
                }

//                LaunchedEffect(key1 = isKitKatReady, key2 = storedTitle) {
//                    if (isKitKatReady) {
////						!!! These delays are disgusting
//                        delay(470)
//                        if (!isRecreated) {
//                            Log.d("npr71", "setting saved title")
//                            kitKat.onKitKatActionAsync(KitKatAction.Edit.SetTitle(storedTitle))
//                        }
//                    }
//                }
//
//                LaunchedEffect(key1 = isKitKatReady, key2 = storedContent) {
//                    if (isKitKatReady) {
////						!!! These delays are disgusting
//                        delay(470)
//                        if (!isRecreated) {
//                            Log.d("npr71", "setting saved content : $storedContent")
//                            kitKat.onKitKatActionAsync(KitKatAction.Edit.SetContent.auto(content = storedContent))
//                        }
//                    }
//                }
//
//                LaunchedEffect(key1 = kitKatFormat) {
//                    if (isReCreateDataRestored) viewModel.putKitKatFormat(kitKatFormat)
//                }

                NoteEditorScreen(
                    kitKat = kitKat
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun readActivity() {
        val isNew = intent.getBooleanExtra(ActivityUtil.NoteExtra.IsNew.name, true)
        val isEditing = intent.getBooleanExtra(ActivityUtil.NoteExtra.IsEditing.name, true)
        val parentIdBytes = intent.getByteArrayExtra(ActivityUtil.NoteExtra.ParentIdBytes.name) ?: return

//        !!!   Check if parentIdBytes can be converted to realmuuid
        if (isNew) viewModel.initNote(parentId = RealmUUID.from(parentIdBytes))
    }

    companion object {
        const val TAG = "NoteActivity2"
    }
}

