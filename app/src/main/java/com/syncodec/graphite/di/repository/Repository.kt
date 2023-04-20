package com.syncodec.graphite.di.repository


enum class RepositoryState {
	Init,
	Locked,
	Loading,
	Success,
	Error,
}

class RealmNotInitializedException : Exception("Realm not initialized")
class ParentChapterNotFoundException : Exception("Parent chapter not found")
class ChapterNotFoundException : Exception("Chapter not found")
class BucketNotFoundException : Exception("Bucket not found")
class NoteNotFoundException : Exception("Note not found")
class SameBookException : Exception("Same book")

enum class CallbackStatus {
	SUCCESS, ERROR, UNINITIALIZED
}
