package com.syncodec.momento.database.diary

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.syncodec.momento.miscellaneous.generatePrimaryKey

@Entity(tableName = "diary_table")
data class DiaryDbEntry(
	@PrimaryKey(autoGenerate = false)
	@ColumnInfo(name = "primary_key")
	@Expose
	val primaryKey: String,
	@ColumnInfo(name = "timezone_offset")
	@Expose
	val timezoneOffset: Int
) {
	@ColumnInfo(name = "created_timestamp")
	@Expose
	var createdTimestamp: Long = -1

	@ColumnInfo(name = "modified_timestamp")
	@Expose
	var modifiedTimestamp: Long = -1

	@ColumnInfo(name = "user_timestamp")
	@Expose
	var userTimestamp: Long = -1

	@ColumnInfo(name = "content_thumbnail")
	@Expose
	var contentThumbnail: String? = null

	@ColumnInfo(name = "latitude")
	@Expose
	var latitude: Double? = null

	@ColumnInfo(name = "longitude")
	@Expose
	var longitude: Double? = null

	@ColumnInfo(name = "address")
	@Expose
	var address: String? = null

	@ColumnInfo(name = "mood")
	@Expose
	var mood: Int = 0

	@ColumnInfo(name = "title")
	@Expose
	var title: String? = null

	@ColumnInfo(name = "is_favourite")
	@Expose
	var isFavourite: Boolean = false

	@ColumnInfo(name = "is_archived")
	@Expose
	var isArchived: Boolean = false

	@ColumnInfo(name = "is_locked")
	@Expose
	var isLocked: Boolean = false

	@ColumnInfo(name = "g_drive_file_id")
	var gDriveFileId: String? = null

	@Expose
	@ColumnInfo(name = "hash")
	var hash: Long? = null

	override fun hashCode(): Int {
		return primaryKey.toInt()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as DiaryDbEntry

		if (primaryKey != other.primaryKey) return false

		return true
	}
}

class MockDiaryDbEntry : PreviewParameterProvider<DiaryDbEntry> {
	override val values = sequenceOf(
		DiaryDbEntry(
			primaryKey = "generatePrimaryKey()",
			timezoneOffset = 330 * 60
		).apply {
			this.createdTimestamp = 1644265384000
			this.modifiedTimestamp = 1644351784000
			this.userTimestamp = 1651695784000
			this.contentThumbnail = "the question is what color will everything be at the moment I come for you? What will the sky be sayin\n\nalpha beta gamma"
			this.latitude = 22.3054116329618
			this.longitude = 70.78487291405152
			this.address = "Street Numer 5, Hari Nagar, Rajkot, Gujarat 360005"
			this.mood = 1
			this.title = "My favourite quotes"
			this.isFavourite = true
			this.isArchived = false
			this.isLocked = false
		}
	)
}

class MockDiaryDbEntryList : PreviewParameterProvider<List<DiaryDbEntry>> {
	override val values = sequenceOf(
		listOf(
			DiaryDbEntry(
				primaryKey = "generatePrimaryKey()",
				timezoneOffset = 330 * 60
			).apply {
				this.createdTimestamp = 1644265384000
				this.modifiedTimestamp = 1644351784000
				this.userTimestamp = 1651695784000
				this.contentThumbnail = "the question is what color will everything be at the moment I come for you? What will the sky be sayin\n\nalpha beta gamma"
				this.latitude = 22.3054116329618
				this.longitude = 70.78487291405152
				this.address = "Street Numer 5, Hari Nagar, Rajkot, Gujarat 360005"
				this.mood = 1
				this.title = "My favourite quotes"
				this.isFavourite = true
				this.isArchived = false
				this.isLocked = false
			},
			DiaryDbEntry(
				primaryKey = "generatePrimaryKey()",
				timezoneOffset = 330 * 60
			).apply {
				this.createdTimestamp = 1644265384000
				this.modifiedTimestamp = 1644351784000
				this.userTimestamp = 1651695784000
				this.contentThumbnail = "the question is what color will everything be at the moment I come for you? What will the sky be sayin\n\nalpha beta gamma"
				this.latitude = 22.3054116329618
				this.longitude = 70.78487291405152
				this.address = "Street Numer 5, Hari Nagar, Rajkot, Gujarat 360005"
				this.mood = 1
				this.title = "My favourite quotes"
				this.isFavourite = true
				this.isArchived = false
				this.isLocked = false
			},
			DiaryDbEntry(
				primaryKey = "generatePrimaryKey()",
				timezoneOffset = 330 * 60
			).apply {
				this.createdTimestamp = 1644265384000
				this.modifiedTimestamp = 1644351784000
				this.userTimestamp = 1651695784000
				this.contentThumbnail = "the question is what color will everything be at the moment I come for you? What will the sky be sayin\n\nalpha beta gamma"
				this.latitude = 22.3054116329618
				this.longitude = 70.78487291405152
				this.address = "Street Numer 5, Hari Nagar, Rajkot, Gujarat 360005"
				this.mood = 1
				this.title = "My favourite quotes"
				this.isFavourite = true
				this.isArchived = false
				this.isLocked = false
			},
			DiaryDbEntry(
				primaryKey = "generatePrimaryKey()",
				timezoneOffset = 330 * 60
			).apply {
				this.createdTimestamp = 1644265384000
				this.modifiedTimestamp = 1644351784000
				this.userTimestamp = 1651695784000
				this.contentThumbnail = "the question is what color will everything be at the moment I come for you? What will the sky be sayin\n\nalpha beta gamma"
				this.latitude = 22.3054116329618
				this.longitude = 70.78487291405152
				this.address = "Street Numer 5, Hari Nagar, Rajkot, Gujarat 360005"
				this.mood = 1
				this.title = "My favourite quotes"
				this.isFavourite = true
				this.isArchived = false
				this.isLocked = false
			}
		)
	)
}
