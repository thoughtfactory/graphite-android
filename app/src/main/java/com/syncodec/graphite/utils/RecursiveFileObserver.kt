package com.syncodec.graphite.utils

import android.os.FileObserver
import java.io.File
import java.util.Stack


class RecursiveFileObserver(private val mPath : String, mask : Int, private val mListener : EventListener?) : FileObserver(
	File(mPath), mask
) {
	private val mObservers : MutableMap<String, FileObserver?> = HashMap()
	private val mMask : Int

	interface EventListener {
		fun onEvent(event : Int, file : File?)
	}

	constructor(path : String, listener : EventListener?) : this(path, ALL_EVENTS, listener)

	init {
		mMask = mask or CREATE or DELETE_SELF
	}

	private fun startWatching(path : String) {
		synchronized(mObservers) {
			var observer = mObservers.remove(path)
			observer?.stopWatching()
			observer = SingleFileObserver(path, mMask)
			observer !!.startWatching()
			mObservers.put(path, observer)
		}
	}

	override fun startWatching() {
		val stack = Stack<String>()
		stack.push(mPath)

		// Recursively watch all child directories
		while (! stack.empty()) {
			val parent = stack.pop()
			startWatching(parent)
			val path = File(parent)
			val files : Array<out File>? = path.listFiles()
			if (files != null) {
				for (file in files) {
					if (watch(file)) {
						stack.push(file.getAbsolutePath())
					}
				}
			}
		}
	}

	private fun watch(file : File) : Boolean {
		return file.isDirectory && ! file.name.equals(".") && ! file.name.equals("..")
	}

	private fun stopWatching(path : String) {
		synchronized(mObservers) {
			val observer = mObservers.remove(path)
			observer?.stopWatching()
		}
	}

	override fun stopWatching() {
		synchronized(mObservers) {
			for (observer in mObservers.values) {
				observer !!.stopWatching()
			}
			mObservers.clear()
		}
	}

	override fun onEvent(event : Int, path : String?) {
		val file : File
		if (path == null) {
			file = File(mPath)
		} else {
			file = File(mPath, path)
		}
		notify(event, file)
	}

	private fun notify(event : Int, file : File) {
		mListener?.onEvent(event and ALL_EVENTS, file)
	}

	private inner class SingleFileObserver(private val filePath : String, mask : Int) : FileObserver(filePath, mask) {
		override fun onEvent(event : Int, path : String?) {
			val file : File
			if (path == null) {
				file = File(filePath)
			} else {
				file = File(filePath, path)
			}
			when (event and ALL_EVENTS) {
				DELETE_SELF -> this@RecursiveFileObserver.stopWatching(filePath)
				CREATE -> if (watch(file)) {
					this@RecursiveFileObserver.startWatching(file.getAbsolutePath())
				}
			}
			notify(event, file)
		}
	}
}


//class RecursiveFileObserver(private val mPath : String, mask : Int, private val mListener : EventListener?) : FileObserver(
//	mPath, mask
//) {
//	private val mObservers : MutableMap<String, FileObserver?> = HashMap()
//	private val mMask : Int
//
//	interface EventListener {
//		fun onEvent(event : Int, file : File?)
//	}
//
//	constructor(path : String, listener : EventListener?) : this(path, ALL_EVENTS, listener)
//
//	init {
//		mMask = mask or CREATE or DELETE_SELF
//	}
//
//	private fun startWatching(path : String) {
//		synchronized(mObservers) {
//			var observer = mObservers.remove(path)
//			observer?.stopWatching()
//			observer = SingleFileObserver(path, mMask)
//			observer !!.startWatching()
//			mObservers.put(path, observer)
//		}
//	}
//
//	override fun startWatching() {
//		val stack = Stack<String>()
//		stack.push(mPath)
//
//		// Recursively watch all child directories
//		while (! stack.empty()) {
//			val parent = stack.pop()
//			startWatching(parent)
//			val path = File(parent)
//			val files : Array<File> = path.listFiles()
//			if (files != null) {
//				for (file in files) {
//					if (watch(file)) {
//						stack.push(file.getAbsolutePath())
//					}
//				}
//			}
//		}
//	}
//
//	private fun watch(file : File) : Boolean {
//		return file.isDirectory() && ! file.getName().equals(".") && ! file.getName().equals("..")
//	}
//
//	private fun stopWatching(path : String) {
//		synchronized(mObservers) {
//			val observer = mObservers.remove(path)
//			observer?.stopWatching()
//		}
//	}
//
//	override fun stopWatching() {
//		synchronized(mObservers) {
//			for (observer in mObservers.values) {
//				observer !!.stopWatching()
//			}
//			mObservers.clear()
//		}
//	}
//
//	override fun onEvent(event : Int, path : String?) {
//		val file : File
//		if (path == null) {
//			file = File(mPath)
//		} else {
//			file = File(mPath, path)
//		}
//		notify(event, file)
//	}
//
//	private fun notify(event : Int, file : File) {
//		mListener?.onEvent(event and ALL_EVENTS, file)
//	}
//
//	private inner class SingleFileObserver(private val filePath : String, mask : Int) : FileObserver(filePath, mask) {
//		override fun onEvent(event : Int, path : String?) {
//			val file : File
//			if (path == null) {
//				file = File(filePath)
//			} else {
//				file = File(filePath, path)
//			}
//			when (event and ALL_EVENTS) {
//				DELETE_SELF -> this@RecursiveFileObserver.stopWatching(filePath)
//				CREATE -> if (watch(file)) {
//					this@RecursiveFileObserver.startWatching(file.getAbsolutePath())
//				}
//			}
//			notify(event, file)
//		}
//	}
//}
