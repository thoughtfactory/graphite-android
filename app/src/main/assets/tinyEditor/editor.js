function undeo() {
    editor.execCommand('undo');
};

function redo() {
    editor.execCommand('redo');
};

function clearFormatting() {
    editor.execCommand('removeFormat');
};

function bold() {
    editor.execCommand('bold');
};

function italic() {
    editor.execCommand('italic');
};

function underline() {
    editor.execCommand('underline');
};

function strikethrough() {
    editor.execCommand('strikethrough');
};

function subscript() {
    editor.execCommand('subscript');
};

function superscript() {
    editor.execCommand('superscript');
};

function alignment(align) {
    editor.execCommand(align);
};

function heading(value) {
    editor.execCommand('formatBlock', false, value);
};

function bulletList() {
    editor.execCommand();
};

function numberedList(value) {
    editor.execCommand();
};

function indent() {
    editor.execCommand('indent');
};

function outdent() {
    editor.execCommand('outdent');
};

function link(url) {
    editor.execCommand('mceInsertLink', true, 'https://www.tiny.cloud');
};
