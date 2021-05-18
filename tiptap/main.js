const Editor = require('@tiptap/core');
const Document = require('@tiptap/extension-document');
const Text = require('@tiptap/extension-text');

// mark
const Bold = require('@tiptap/extension-bold');
const Italic = require('@tiptap/extension-italic');
const Underline = require('@tiptap/extension-underline');
const Strike = require('@tiptap/extension-strike');
const Superscript = require('@tiptap/extension-superscript');
const Subscript = require('@tiptap/extension-subscript');

const Code = require('@tiptap/extension-code');
const Highlight = require('@tiptap/extension-highlight');
const Link = require('@tiptap/extension-link');

const TextStyle = require('@tiptap/extension-text-style');

// node
const Blockquote = require('@tiptap/extension-blockquote');
const Codeblock = require('@tiptap/extension-code-block-lowlight');
const Hardbreak = require('@tiptap/extension-hard-break');
const Paragraph = require('@tiptap/extension-paragraph');
const Heading = require('@tiptap/extension-heading');
const HorizontalRule = require('@tiptap/extension-horizontal-rule');
const Image = require('@tiptap/extension-image');
// const Mention = require('@tiptap/extension-mention');

// List
const ListItem = require('@tiptap/extension-list-item');
const BulletList = require('@tiptap/extension-bullet-list');
const OrderedList = require('@tiptap/extension-ordered-list');
const TaskList = require('@tiptap/extension-task-list');
const TaskItem = require('@tiptap/extension-task-item');

// functionality
const TextAlign = require('@tiptap/extension-text-align');
const CharacterCount = require('@tiptap/extension-character-count');
const Color = require('@tiptap/extension-color');
const FontFamily = require('@tiptap/extension-font-family');
const GapCursor = require('@tiptap/extension-gapcursor');
const History = require('@tiptap/extension-history');
const Placeholder = require('@tiptap/extension-placeholder');
const Typography = require('@tiptap/extension-typography');

// import './styles.scss'


const editor = new Editor.Editor({
    element: document.querySelector('.tiptap'),
    editorProps: {
        attributes: {
            class: 'prose prose-sm sm:prose lg:prose-lg xl:prose-2xl m-5 focus:outline-none',
        },
    },
    extensions: [
        Document.Document,
        Text.Text,

        Bold.Bold.configure({
            HTMLAttributes: {
                class: 'bold-style',
            },
        }),
        Italic.Italic.configure({
            HTMLAttributes: {
                class: 'italic-style',
            },
        }),
        Underline.Underline.configure({
            HTMLAttributes: {
                class: 'underline-style',
            },
        }),
        Strike.Strike.configure({
            HTMLAttributes: {
                class: 'strike-style',
            },
        }),
        Superscript.Superscript.configure({
            HTMLAttributes: {
                class: 'superscript-style',
            },
        }),
        Subscript.Subscript.configure({
            HTMLAttributes: {
                class: 'subscript-style',
            },
        }),

        Code.Code.configure({
            HTMLAttributes: {
                class: 'code-style'
            }
        }),
        Highlight.Highlight.configure({
            multicolor: true,
            HTMLAttributes: {
                class: 'highlight-style'
            }
        }),
        Link.Link.configure({
            autolink: true,
            openOnClick: false,
            linkOnPaste: true,
            HTMLAttributes: {
                class: 'link-style'
            }
        }),

        TextStyle.TextStyle,

        // node
        Blockquote.Blockquote.configure({
            HTMLAttributes: {
                class: 'blockquote-style'
            }
        }),
        Codeblock.CodeBlockLowlight.configure({
            exitOnTripleEnter: true,
            HTMLAttributes: {
                class: 'codeblock-style'
            }
        }),
        Hardbreak.HardBreak.configure({
            keepMarks: true,
            HTMLAttributes: {
                class: 'hardbreak-style'
            }
        }),
        Paragraph.Paragraph.configure({
            HTMLAttributes: {
                class: 'paragraph-style'
            }
        }),
        Heading.Heading.configure({
            levels: [1, 2, 3, 4, 5, 6],
            HTMLAttributes: {
                class: 'heading-style'
            }
        }),
        HorizontalRule.HorizontalRule.configure({
            HTMLAttributes: {
                class: 'horizontal-rule-style'
            }
        }),

        // List
        ListItem.ListItem,
        BulletList.BulletList.configure({
            itemTypeName: 'listItem',
            HTMLAttributes: {
                class: 'bullet-list-style'
            }
        }),
        OrderedList.OrderedList.configure({
            itemTypeName: 'listItem',
            HTMLAttributes: {
                class: 'ordered-list-style'
            }
        }),
        TaskItem.TaskItem.configure({
            nested: true,
            HTMLAttributes: {
                class: 'task-item-style',
            },
        }),
        TaskList.TaskList.configure({
            itemTypeName: 'taskItem',
            HTMLAttributes: {
                class: 'task-list-class'
            }
        }),

        // Functionality
        TextAlign.TextAlign.configure({
            types: ['heading', 'paragraph'],
            alignments: ['left', 'center', 'right', 'justify'],
            defaultAlignment: 'left',
        }),
        CharacterCount.CharacterCount,
        Color.Color,
        FontFamily.FontFamily.configure({
            types: ['textStyle'],
        }),
        GapCursor.Gapcursor,
        History.History.configure({
            depth: 20,
            newGroupDelay: 40,
        }),
        Placeholder.Placeholder.configure({
            placeholder: 'What story did you bring today?',
            showOnlyWhenEditable: true,
        }),
        Typography.Typography,

    ],
    autofocus: true,
    editable: true,
    injectCSS: false,
    onCreate: onCreate,
    editable: true,
});

function onCreate() {
    bridge.onCreate();
};

editor.on('transaction', ({
    editor,
    transaction
}) => {

    var currentFormat = {};

    currentFormat.bold = editor.isActive('bold');
    currentFormat.italic = editor.isActive('italic');
    currentFormat.underline = editor.isActive('underline');
    currentFormat.strike = editor.isActive('strike');
    currentFormat.superscript = editor.isActive('superscript');
    currentFormat.subscript = editor.isActive('subscript');

    currentFormat.link = editor.getAttributes('link').href;

    currentFormat.blockquote = editor.isActive('blockquote');
    currentFormat.code = editor.isActive('code');
    currentFormat.codeBlock = editor.isActive('codeBlock');

    currentFormat.paragraph = editor.isActive('paragraph');
    currentFormat.heading = editor.isActive('heading');
    currentFormat.heading1 = editor.isActive('heading', {
        level: 1
    });
    currentFormat.heading2 = editor.isActive('heading', {
        level: 2
    });
    currentFormat.heading3 = editor.isActive('heading', {
        level: 3
    });
    currentFormat.heading4 = editor.isActive('heading', {
        level: 4
    });
    currentFormat.heading5 = editor.isActive('heading', {
        level: 5
    });
    currentFormat.heading6 = editor.isActive('heading', {
        level: 6
    });

    currentFormat.alignLeft = editor.isActive({
        textAlign: 'left'
    });
    currentFormat.alignCenter = editor.isActive({
        textAlign: 'center'
    });
    currentFormat.alignRight = editor.isActive({
        textAlign: 'right'
    });
    currentFormat.alignJustify = editor.isActive({
        textAlign: 'justify'
    });

    currentFormat.bulletList = editor.isActive('bulletList');
    currentFormat.orderedList = editor.isActive('orderedList');
    currentFormat.taskList = editor.isActive('taskList');

    currentFormat.characterCount = editor.storage.characterCount.characters();
    currentFormat.wordCount = editor.storage.characterCount.words();
    currentFormat.textColor = editor.getAttributes('textStyle').color;
    currentFormat.highlightColor = editor.getAttributes('highlight').color;
    currentFormat.fontFamily = editor.getAttributes('textStyle').FontFamily;

    currentFormat.currentSelection = editor.state.selection.$anchor.pos;
    currentFormat.canIndent = editor.can().sinkListItem('listItem');
    currentFormat.canOutdent = editor.can().liftListItem('listItem');
    // editor.isActive('textStyle', { fontFamily: 'serif' })

    bridge.format(JSON.stringify(currentFormat));
});

editor.getData = () => {
    data = {};
    data.dataJson = editor.getJSON();
    data.dataText = editor.getText();

    bridge.getData(JSON.stringify(data));
};

editor.setBaseFontFamily = (fontFamily) => {
    document.getElementById("base").style.fontFamily = fontFamily;
};

editor.setBaseFontColor = (fontColor) => {
    document.getElementById("base").style.color = fontColor;
};

editor.tryFocus = () => {
    
}

module.exports = editor;