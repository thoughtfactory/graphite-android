package com.syncodec.graphite.presentation.note2.kitKat

import org.json.JSONObject


open class KitKatAction(open val action: String, open val callback: (String) -> Unit = {}) {
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
    sealed class List(override val action: String) : KitKatAction(action = action) {
        data object CheckList : List(action = "editor.commands.toggleTaskList();")
        data object BulletList : List(action = "editor.commands.toggleBulletList();")
        data object OrderedList : List(action = "editor.commands.toggleOrderedList();")
    }

    sealed class Heading(override val action: String) : KitKatAction(action = action) {
        data object Paragraph : Heading(action = "editor.commands.toggleHeading({ level: 3 });")
        data object Heading1 : Heading(action = "editor.commands.toggleHeading({ level: 1 });")
        data object Heading2 : Heading(action = "editor.commands.toggleHeading({ level: 2 });")
        data object Heading3 : Heading(action = "editor.commands.toggleHeading({ level: 3 });")
        data object Heading4 : Heading(action = "editor.commands.toggleHeading({ level: 4 });")
        data object Heading5 : Heading(action = "editor.commands.toggleHeading({ level: 5 });")
        data object Heading6 : Heading(action = "editor.commands.toggleHeading({ level: 6 });")
    }

    data object Blockquote : KitKatAction(action = "editor.chain().focus().toggleBlockquote().run();")
    data object Indent : KitKatAction(action = "editor.chain().focus().sinkListItem('listItem').run()")
    data object Outdent : KitKatAction(action = "editor.chain().focus().liftListItem('listItem').run()")

    sealed class Link(override val action: String) : KitKatAction(action = action) {
        data class Set(val url: String) : Link(action = "editor.commands.setLink({ href: '$url' })")
        data object Unset : Link(action = "editor.commands.unsetLink()")
        data object ExtendSelection : Link(action = "editor.commands.extendMarkRange('link')")
    }

    sealed class Align(override val action: String) : KitKatAction(action = action) {
        data object Left : Align(action = "editor.commands.setTextAlign('left');")
        data object Center : Align(action = "editor.commands.setTextAlign('center');")
        data object Right : Align(action = "editor.commands.setTextAlign('right');")
        data object Justify : Align(action = "editor.commands.setTextAlign('justify');")
        data object Unset : Align(action = "editor.commands.unsetTextAlign();")
    }

    sealed class TextColor(override val action: String) : KitKatAction(action = action) {
        data class Set(val color: String) : TextColor(action = "editor.commands.setColor('$color');")
        data object Unset : TextColor(action = "editor.commands.unsetColor();")
        data object ExtendSelection : Link(action = "editor.commands.extendMarkRange('textStyle')")
    }

    sealed class HighlightColor(override val action: String) : KitKatAction(action = action) {
        data class Set(val color: String) : HighlightColor(action = "editor.commands.setHighlight({ color: '$color' });")
        data object Unset : HighlightColor(action = "editor.commands.unsetHighlight();")
        data object ExtendSelection : Link(action = "editor.commands.extendMarkRange('highlight')")
    }

    sealed class Edit(override val action: String) : KitKatAction(action = action) {
        data object Enable : Edit(action = "editor.enable();")
        data object Disable : Edit(action = "editor.disable();")
        data class SetTitle(val title: String? = null) : Edit(action = "editor.setTitle('${title ?: ""}');")
        sealed class SetContent(override val action: String) : Edit(action = action) {
            data class Html(val content: String?) : SetContent(action = "editor.setContent(${content?.let { JSONObject().apply { put("content", it) }.toString() } ?: DEFAULT_CONTENT});")
            data class Json(val content: String?) : SetContent(action = "editor.setContent(${content ?: DEFAULT_CONTENT});")
            companion object {
                const val DEFAULT_CONTENT = ""
                fun auto(content: String?) = if (content?.startsWith("{") == true) Json(content = content) else Html(content = content)
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
