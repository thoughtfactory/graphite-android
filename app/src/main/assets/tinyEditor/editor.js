function undo() {
    editor.execCommand('undo');
};

function redo() {
    editor.execCommand('redo');
};

function removeFormat() {
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
    editor.execCommand('formatBlock', false, align);
};

function heading(value) {
    editor.execCommand('formatBlock', false, value);
};

function blockquote() {
    editor.execCommand('formatBlock', false, "blockquote");
};

function hashtag() {
    editor.execCommand('formatBlock', false, "code");
};

function insertUnorderedList() {
    editor.execCommand('InsertUnorderedList', false, {});
};

function insertOrderedList() {
    editor.execCommand('InsertOrderedList', false, {});
};

function insertCheckbox() {
    editor.execCommand('mceInsertRawHTML', false, "<input type='checkbox'>");
};

function indent() {
    editor.execCommand('indent');
};

function outdent() {
    editor.execCommand('outdent');
};

function link(url) {
    editor.execCommand('mceInsertLink', false, url);
};

function insertTable(nRows, nCols) {
    editor.execCommand('mceInsertTable', false, { rows: nRows, columns: nCols });
};

function applyTextColor(color) {
    editor.execCommand('ForeColor', false, color);
};

function applyHighlightColor(color) {
    editor.execCommand('HiliteColor', false, color);
};

function setFontSize(size) {
    editor.execCommand('FontSize', false, size);
};
