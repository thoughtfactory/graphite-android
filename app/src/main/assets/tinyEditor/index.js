var editor;

tinymce.init({
	selector: 'textarea#editor',
	menubar: false,
	statusbar: true,
	toolbar: false,
	lists_indent_on_tab: true,
	plugins: [
		'advlist checklist autolink lists link image charmap print preview anchor',
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
		console.log(e);
	});

	editor.on('ScrollIntoView', function (e) {
		e.preventDefault();
		e.elm.scrollIntoView({
			behavior: 'smooth',
			block: 'nearest'
		});
	});

	recurseSaveData();
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

		currentFormat.link = undefined;


		var node = editor.selection.getNode();

		while (node != null) {
			if (node.nodeName === 'A') {
				currentFormat.link = node.getAttribute('href');
			}

			if (/^(LI|UL|OL|DL)$/.test(node.nodeName)) {
				var element = editor.selection.getNode();
				if (element.nodeName === 'LI') {
					element = editor.dom.getParent(element, 'ol,ul');
				}

				if (element.nodeName === 'OL') {
					currentFormat.orderedList = true;
				} else {
					if (element.className === 'tox-checklist') {
						currentFormat.checkList = true;
					} else {
						currentFormat.unorderedList = true;
					}
				}
			}

			node = node.parentElement;
		}

		const style = editor.selection.getNode().style;
		if (style.fontFamily == '') {

		} else {
			currentFormat.fontFamily = style.fontFamily;
		}

		if (style.fontSize == '') {
			currentFormat.fontSize = '12px';
		} else {
			currentFormat.fontSize = style.fontSize;
		}

		const selectionRange = editor.selection.getRng();
		var range = {};
		range.startOffset = selectionRange.startOffset;
		range.startOffset = selectionRange.endOffset;

		if (selectionRange != undefined) {
			currentFormat.startOffset = selectionRange.startOffset;
			currentFormat.endOffset = selectionRange.endOffset;
		}

		console.log(JSON.stringify(currentFormat));
		bridge.format(JSON.stringify(currentFormat));

	}, 50);
};

function saveData(callback) {
	var content = {
		"html": editor.getContent({
			format: 'html'
		}),
		"text": editor.getContent({
			format: 'text'
		}),
	};
	console.log(content);
	if (callback) {
		bridge.dataCallback(JSON.stringify(content));
	} else {
		bridge.data(JSON.stringify(content));
	}
};

function recurseSaveData() {
	saveData(false);
	setTimeout(recurseSaveData, 2000);
};