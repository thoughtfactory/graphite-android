package com.syncodec.graphite.presentation.note2.kitKat

import org.json.JSONObject


sealed class KitKatAction(val action: String, val callback: (String) -> Unit = {}) {
	data object Undo : KitKatAction(action = "editor.commands.undo();")
	data object Redo : KitKatAction(action = "editor.commands.redo();")
	data object Bold : KitKatAction(action = "editor.chain().focus().toggleBold().run()")
	data object Italic : KitKatAction(action = "editor.chain().focus().toggleItalic().run()")
	data object Underline : KitKatAction(action = "editor.chain().focus().toggleUnderline().run()")
	data object StrikeThrough : KitKatAction(action = "editor.chain().focus().toggleStrike().run();")
	data object Superscript : KitKatAction(action = "editor.chain().focus().toggleSuperscript().run()")
	data object Subscript : KitKatAction(action = "editor.chain().focus().toggleSubscript().run();")
	data object HardLineBreak : KitKatAction(action = "editor.chain().focus().setHardBreak().run()")
	data object HorizontalRule : KitKatAction(action = "editor.chain().focus().setHorizontalRule().run()")
	sealed class List(action2: String) : KitKatAction(action = action2) {
		data object CheckList : List(action2 = "editor.commands.toggleTaskList();")
		data object BulletList : List(action2 = "editor.commands.toggleBulletList();")
		data object OrderedList : List(action2 = "editor.commands.toggleOrderedList();")
	}

	sealed class Heading(val action2: String) : KitKatAction(action = action2) {
		data object Paragraph : Heading(action2 = "editor.commands.toggleHeading({ level: 3 });")
		data object Heading1 : Heading(action2 = "editor.commands.toggleHeading({ level: 1 });")
		data object Heading2 : Heading(action2 = "editor.commands.toggleHeading({ level: 2 });")
		data object Heading3 : Heading(action2 = "editor.commands.toggleHeading({ level: 3 });")
		data object Heading4 : Heading(action2 = "editor.commands.toggleHeading({ level: 4 });")
		data object Heading5 : Heading(action2 = "editor.commands.toggleHeading({ level: 5 });")
		data object Heading6 : Heading(action2 = "editor.commands.toggleHeading({ level: 6 });")
	}

	data object Blockquote : KitKatAction(action = "editor.chain().focus().toggleBlockquote().run();")
	data object Indent : KitKatAction(action = "editor.chain().focus().sinkListItem('listItem').run()")
	data object Outdent : KitKatAction(action = "editor.chain().focus().liftListItem('listItem').run()")

	sealed class Link(val action2: String) : KitKatAction(action = action2) {
		data class Set(val url: String) : Link(action2 = "editor.commands.setLink({ href: '$url' })")
		data object Unset : Link(action2 = "editor.commands.unsetLink()")
		data object ExtendSelection : Link(action2 = "editor.commands.extendMarkRange('link')")
	}

	sealed class Align(action2: String) : KitKatAction(action = action2) {
		data object Left : Align(action2 = "editor.commands.setTextAlign('left');")
		data object Center : Align(action2 = "editor.commands.setTextAlign('center');")
		data object Right : Align(action2 = "editor.commands.setTextAlign('right');")
		data object Justify : Align(action2 = "editor.commands.setTextAlign('justify');")
		data object Unset : Align(action2 = "editor.commands.unsetTextAlign();")
	}

	sealed class TextColor(action2: String) : KitKatAction(action = action2) {
		data class Set(val color: String) : TextColor(action2 = "editor.commands.setColor('$color');")
		data object Unset : TextColor(action2 = "editor.commands.unsetColor();")
		data object ExtendSelection : Link(action2 = "editor.commands.extendMarkRange('textStyle')")
	}

	sealed class HighlightColor(action2: String) : KitKatAction(action = action2) {
		data class Set(val color: String) : HighlightColor(action2 = "editor.commands.setHighlight({ color: '$color' });")
		data object Unset : HighlightColor(action2 = "editor.commands.unsetHighlight();")
		data object ExtendSelection : Link(action2 = "editor.commands.extendMarkRange('highlight')")
	}

	sealed class Edit(action2: String) : KitKatAction(action = action2) {
		data object Enable : Edit(action2 = "editor.enable();")
		data object Disable : Edit(action2 = "editor.disable();")
		data class SetTitle(val title: String?) : Edit(action2 = "editor.setTitle('${title ?: ""}');")
		data class SetContent(val content: String?) : Edit(action2 = "editor.setContent(${content?.let { JSONObject().apply { put("content", it) }.toString() } ?: DEFAULT_CONTENT});") {
			companion object {
				const val DEFAULT_CONTENT = ""
			}
		}
	}

	sealed class Other(action2: String) : KitKatAction(action = action2) {
		data object SetMaxHeight : Other(action2 = "document.getElementsByClassName(\"ProseMirror\")[0].style.setProperty('height', (window.innerHeight) + 'px');")
		data object EnableDarkMode : Other(action2 = "editor.enableDarkMode();")
		data object DisableDarkMode : Other(action2 = "editor.disableDarkMode();")
	}

	sealed class Export(action2: String, callback2: (String) -> Unit) : KitKatAction(action = action2, callback = callback2) {
		data class Text(val callback3: (String) -> Unit) : Export(action2 = "editor.execExportText();", callback2 = callback3)
		data class Html(val callback3: (String) -> Unit) : Export(action2 = "editor.execExportHtml();", callback2 = callback3)
		data class Json(val callback3: (String) -> Unit) : Export(action2 = "editor.execExportJson();", callback2 = callback3)
		data class Markdown(val callback3: (String) -> Unit) : Export(action2 = "editor.execExportMarkdown();", callback2 = callback3)
	}

	data class TestAction(val action2: String, val callback2: (String) -> Unit) : KitKatAction(action = action2, callback = callback2)
}
