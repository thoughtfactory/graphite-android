package com.syncodec.graphite.presentation.common.component.chapter

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.realm.kotlin.types.RealmUUID


@Preview
@Composable
fun ChapterListCard(
	id: RealmUUID = RealmUUID.random(),
	createdTimestamp: Long? = null,
	modifiedTimestamp: Long? = null,
	title: String? = null,
	description: String? = null,
	isFavourite: Boolean = false,
	isLocked: Boolean = false,
	color: Color? = null,
	thumbnail: String? = null,
	noteCount: Int = 0,
	chapterCount: Int = 0,
	isSelected: Boolean = false,
	onClick: () -> Unit = {},
	onLongClick: () -> Unit = {}
) {}
