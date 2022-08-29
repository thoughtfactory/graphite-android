package com.syncodec.graphite.presentation.custom.text

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.DoNotInline
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus


//class TextToolbar(private val view: View) : TextToolbar {
//	private var actionMode: ActionMode? = null
//	private val textActionModeCallback: TextActionModeCallback = TextActionModeCallback()
//	override var status: TextToolbarStatus = TextToolbarStatus.Hidden
//
//	override fun showMenu(
//		rect: Rect,
//		onCopyRequested: (() -> Unit)?,
//		onPasteRequested: (() -> Unit)?,
//		onCutRequested: (() -> Unit)?,
//		onSelectAllRequested: (() -> Unit)?
//
//	) {
//		textActionModeCallback.rect = rect
//		textActionModeCallback.onCopyRequested = onCopyRequested
//		textActionModeCallback.onCutRequested = onCutRequested
//		textActionModeCallback.onPasteRequested = onPasteRequested
//		textActionModeCallback.onSelectAllRequested = onSelectAllRequested
//
//		if (actionMode == null) {
//			status = TextToolbarStatus.Shown
//			actionMode = TextToolbarHelperMethods.startActionMode(
//				view,
//				FloatingTextActionModeCallback(textActionModeCallback),
//				ActionMode.TYPE_FLOATING
//			)
//		} else {
//			actionMode?.invalidate()
//		}
//	}
//
//	override fun hide() {
//		status = TextToolbarStatus.Hidden
//		actionMode?.finish()
//		actionMode = null
//	}
//}
//
//object TextToolbarHelperMethods {
//	@DoNotInline
//	fun startActionMode(
//		view: View,
//		actionModeCallback: ActionMode.Callback,
//		type: Int
//	): ActionMode {
//		return view.startActionMode(
//			actionModeCallback,
//			type
//		)
//	}
//
//	fun invalidateContentRect(actionMode: ActionMode) {
//		actionMode.invalidateContentRect()
//	}
//}
//
//class FloatingTextActionModeCallback(
//	private val callback: TextActionModeCallback
//) : ActionMode.Callback2() {
//	override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
//		return callback.onActionItemClicked(mode, item)
//	}
//
//	override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
//		return callback.onCreateActionMode(mode, menu)
//	}
//
//	override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
//		return callback.onPrepareActionMode(mode, menu)
//	}
//
//	override fun onDestroyActionMode(mode: ActionMode?) {
//		callback.onDestroyActionMode()
//	}
//
//	override fun onGetContentRect(
//		mode: ActionMode?,
//		view: View?,
//		outRect: android.graphics.Rect?
//	) {
//		val rect = callback.rect
//		outRect?.set(
//			rect.left.toInt(),
//			rect.top.toInt(),
//			rect.right.toInt(),
//			rect.bottom.toInt()
//		)
//	}
//}
//
//class TextActionModeCallback(
//	val onActionModeDestroy: (() -> Unit)? = null,
//	var rect: Rect = Rect.Zero,
//	var onCopyRequested: (() -> Unit)? = null,
//	var onPasteRequested: (() -> Unit)? = null,
//	var onCutRequested: (() -> Unit)? = null,
//	var onSelectAllRequested: (() -> Unit)? = null,
//	var onActionShare: (() -> Unit)? = null
//) {
//	fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
//		requireNotNull(menu)
//		requireNotNull(mode)
//
//		onCopyRequested?.let {
//			addMenuItem(menu, MenuItemOption.Copy)
//		}
//		onPasteRequested?.let {
//			addMenuItem(menu, MenuItemOption.Paste)
//		}
//		onCutRequested?.let {
//			addMenuItem(menu, MenuItemOption.Cut)
//		}
//		onSelectAllRequested?.let {
//			addMenuItem(menu, MenuItemOption.SelectAll)
//		}
//		onActionShare?.let {
//			addMenuItem(menu, MenuItemOption.Share)
//		}
//		return true
//	}
//
//	// this method is called to populate new menu items when the actionMode was invalidated
//	fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
//		if (mode == null || menu == null) return false
//		updateMenuItems(menu)
//		// should return true so that new menu items are populated
//		return true
//	}
//
//	fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
//		when (item!!.itemId) {
//			MenuItemOption.Copy.id -> onCopyRequested?.invoke()
//			MenuItemOption.Paste.id -> onPasteRequested?.invoke()
//			MenuItemOption.Cut.id -> onCutRequested?.invoke()
//			MenuItemOption.SelectAll.id -> onSelectAllRequested?.invoke()
//			else -> return false
//		}
//		mode?.finish()
//		return true
//	}
//
//	fun onDestroyActionMode() {
//		onActionModeDestroy?.invoke()
//	}
//
//	@VisibleForTesting
//	fun updateMenuItems(menu: Menu) {
//		addOrRemoveMenuItem(menu, MenuItemOption.Copy, onCopyRequested)
//		addOrRemoveMenuItem(menu, MenuItemOption.Paste, onPasteRequested)
//		addOrRemoveMenuItem(menu, MenuItemOption.Cut, onCutRequested)
//		addOrRemoveMenuItem(menu, MenuItemOption.SelectAll, onSelectAllRequested)
//	}
//
//	private fun addMenuItem(menu: Menu, item: MenuItemOption) {
//		menu
//			.add(0, item.id, item.order, item.title)
//			.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
//	}
//
//	private fun addOrRemoveMenuItem(
//		menu: Menu,
//		item: MenuItemOption,
//		callback: (() -> Unit)?
//	) {
//		when {
//			callback != null && menu.findItem(item.id) == null -> addMenuItem(menu, item)
//			callback == null && menu.findItem(item.id) != null -> menu.removeItem(item.id)
//		}
//	}
//}
//
//enum class MenuItemOption(val id: Int) {
//	Copy(0),
//	Paste(1),
//	Cut(2),
//	SelectAll(3),
//	Share(4);
//
//	val title: String
//		get() = when (this) {
//			Copy -> "Copy"
//			Paste -> "Paste"
//			Cut -> "Cut"
//			SelectAll -> "Select All"
//			Share -> "Share"
//		}
//
//	/**
//	 * This item will be shown before all items that have order greater than this value.
//	 */
//	val order = id
//}










class TextToolbar(
	val onShowMenu: () -> Unit,
	val onHideMenu: () -> Unit,
): TextToolbar {
	override var status: TextToolbarStatus = TextToolbarStatus.Hidden

	override fun hide() {
		status = TextToolbarStatus.Hidden
		onHideMenu()
	}

	override fun showMenu(
		rect: Rect,
		onCopyRequested: (() -> Unit)?,
		onPasteRequested: (() -> Unit)?,
		onCutRequested: (() -> Unit)?,
		onSelectAllRequested: (() -> Unit)?
	) {
		status = TextToolbarStatus.Shown
		onShowMenu()
	}
}
