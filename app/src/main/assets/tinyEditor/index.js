var editor;

tinymce.init({
	selector: 'textarea#editor',
	menubar: false,
	statusbar: true,
	toolbar: false,
	lists_indent_on_tab: true,
	plugins: [
		'advlist autolink lists link image charmap print preview anchor',
		'searchreplace visualblocks code fullscreen',
		'insertdatetime media table paste code help wordcount'
	],
	content_style: 'body { font-family:Helvetica,Arial,sans-serif; font-size:14px }',
	setup: function (editorInstance) {
		editor = editorInstance;


		editor.on('init', function (e) {
			initEditor();
		});

	}
});

function initEditor() {
	editor.execCommand('mceFullScreen');

	editor.on('SelectionChange', function (e) {
		getCurrentFormat();
	});
}

var triggerTimer = null

function getCurrentFormat() {
	clearTimeout(triggerTimer);
	triggerTimer = null;

	triggerTimer = setTimeout(function () {

		const formats = Object.keys(editor.formatter.get());

		var currentFormat = {};
		editor.formatter.matchAll(formats).forEach(function (format) {
			currentFormat[format] = true;
		});

		// bridge.format(JSON.stringify(currentFormat));

	}, 200);
};

function recurseGetCurrentFormat() {
	getCurrentFormat();
	setTimeout(recurseGetCurrentFormat, 500);
}

recurseGetCurrentFormat();