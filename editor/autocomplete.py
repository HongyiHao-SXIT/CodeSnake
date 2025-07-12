from PyQt6.QtWidgets import QListWidget, QListWidgetItem, QTextEdit
from PyQt6.QtCore import Qt

class AutoCompleter(QListWidget):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.setWindowFlags(Qt.WindowType.Popup)
        self.setFocusPolicy(Qt.FocusPolicy.NoFocus)
        self.setFocusProxy(parent)
        self.setMouseTracking(True)
        self.hide()
        self.itemClicked.connect(self.complete)

    def show_completions(self, word_list, editor: QTextEdit):
        self.clear()
        for word in word_list:
            self.addItem(QListWidgetItem(word))
        self.editor = editor
        self.setCurrentRow(0)
        self.resize(200, 150)
        self.move(editor.mapToGlobal(editor.cursorRect().bottomRight()))
        self.show()

    def keyPressEvent(self, event):
        if event.key() == Qt.Key.Key_Escape:
            self.hide()
        elif event.key() in (Qt.Key.Key_Return, Qt.Key.Key_Enter):
            self.complete()
        else:
            super().keyPressEvent(event)

    def complete(self):
        item = self.currentItem()
        if item:
            cursor = self.editor.textCursor()
            cursor.select(cursor.SelectionType.WordUnderCursor)
            cursor.removeSelectedText()
            cursor.insertText(item.text())
            self.editor.setTextCursor(cursor)
        self.hide()
