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

// Table
const Table = require('@tiptap/extension-table');
const TableHeader = require('@tiptap/extension-table-header');
const TableRow = require('@tiptap/extension-table-row');
const TableCell = require('@tiptap/extension-table-cell');

// functionality
const TextAlign = require('@tiptap/extension-text-align');
const CharacterCount = require('@tiptap/extension-character-count');
const Color = require('@tiptap/extension-color');
const FontFamily = require('@tiptap/extension-font-family');
const GapCursor = require('@tiptap/extension-gapcursor');
const History = require('@tiptap/extension-history');
const Placeholder = require('@tiptap/extension-placeholder');
const Typography = require('@tiptap/extension-typography');



const editor = new Editor.Editor({
    element: document.querySelector('.element'),
    extensions: [
        Document.Document,
        Text.Text,

        Bold.Bold.configure({
            HTMLAttributes: {
                class: 'bold-style',
            },
        }),
        Italic.Italic,
        Underline.Underline,
        Strike.Strike,
        Superscript.Superscript,
        Subscript.Subscript,

        Code.Code,
        Highlight.Highlight.configure({
            multicolor: true,
        }),
        Link.Link.configure({
            autolink: false,
            openOnClick: false,
            linkOnPaste: false,
        }),

        TextStyle.TextStyle,

        // node
        Blockquote.Blockquote,
        Codeblock.CodeBlockLowlight,
        Hardbreak.HardBreak,
        Paragraph.Paragraph,
        Heading.Heading.configure({
            levels: [1, 2, 3, 4, 5, 6],
        }),
        HorizontalRule.HorizontalRule,
        Image.Image.configure({
            inline: true,
        }),
        // Mention.Mention.configure({
        //     renderLabel({
        //         options,
        //         node
        //     }) {
        //         return `${options.suggestion.char}${node.attrs.label ?? node.attrs.id}`
        //     },
        //     suggestion: ,
        // }),


        // List
        ListItem.ListItem,
        BulletList.BulletList.configure({
            itemTypeName: 'listItem',
        }),
        OrderedList.OrderedList.configure({
            itemTypeName: 'listItem',
        }),
        TaskItem.TaskItem.configure({
            HTMLAttributes: {
                class: 'my-custom-class',
            },
        }),
        TaskList.TaskList.configure({
            itemTypeName: 'taskItem',
        }),

        // Table
        Table.Table.configure({
            resizable: true,
            handleWidth: 5,
            cellMinWidth: 25,
            lastColumnResizable: true,
            lastColumnResizable: true,
            allowTableNodeSelection: true
        }),
        TableRow.TableRow.extend({
            content: '(tableCell | tableHeader)*',
        }),
        TableHeader.TableHeader,
        TableCell.TableCell,

        // Functionality
        TextAlign.TextAlign.configure({
            types: ['heading', 'paragraph'],
            defaultAlignment: 'left',
        }),
        CharacterCount.CharacterCount,
        Color.Color,
        FontFamily.FontFamily.configure({
            types: ['textStyle'],
        }),
        GapCursor.Gapcursor,
        History.History.configure({
            depth: 10,
            newGroupDelay: 1000,
        }),
        Placeholder.Placeholder.configure({
            placeholder: 'My Custom Placeholder',
        }),
        Typography.Typography,

    ],
    editorProps: {
        attributes: {
            class: 'prose prose-sm sm:prose lg:prose-lg xl:prose-2xl m-5 focus:outline-none',
        },
    },
    autofocus: true,
    editable: true,
    injectCSS: false
});

editor.on('transaction', ({
    editor,
    transaction
}) => {
    // console.log(editor.isActive('bold'));
});


module.exports = editor;