from PyQt6.QtWidgets import (
    QMainWindow, QTextEdit, QFileDialog, QSplitter,
    QVBoxLayout, QWidget, QHBoxLayout
)
from PyQt6.QtGui import QAction
from PyQt6.QtCore import Qt
import subprocess
import sys
import os
from editor.highlighter import PythonHighlighter
from editor.autocomplete import AutoCompleter
from editor.file_browser import ProjectBrowser

class CodeSnakeMainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("CodeSnake IDE")
        self.resize(1200, 700)

        self.editor = QTextEdit()
        self.output = QTextEdit()
        self.output.setReadOnly(True)
        self.highlighter = PythonHighlighter(self.editor.document())
        self.completer = AutoCompleter(self)
        self.project_browser = ProjectBrowser()

        editor_output_split = QSplitter(Qt.Orientation.Vertical)
        editor_output_split.addWidget(self.editor)
        editor_output_split.addWidget(self.output)

        main_split = QSplitter(Qt.Orientation.Horizontal)
        main_split.addWidget(self.project_browser)
        main_split.addWidget(editor_output_split)

        layout = QVBoxLayout()
        layout.addWidget(main_split)
        central = QWidget()
        central.setLayout(layout)
        self.setCentralWidget(central)

        self._init_menu()
        self.editor.textChanged.connect(self.handle_autocomplete)
        # 获取内置函数和对象的名称作为补全建议
        self.suggestions = dir(__builtins__)

    def _init_menu(self):
        menu_bar = self.menuBar()
        file_menu = menu_bar.addMenu("File")
        run_menu = menu_bar.addMenu("Run")

        open_act = QAction("Open", self)
        open_act.triggered.connect(self.open_file)
        file_menu.addAction(open_act)

        save_act = QAction("Save", self)
        save_act.triggered.connect(self.save_file)
        file_menu.addAction(save_act)

        run_act = QAction("Run", self)
        run_act.triggered.connect(self.run_code)
        run_menu.addAction(run_act)

    def open_file(self):
        path, _ = QFileDialog.getOpenFileName(self, "Open File", "", "Python Files (*.py)")
        if path:
            with open(path, 'r', encoding='utf-8') as f:
                self.editor.setPlainText(f.read())

    def save_file(self):
        path, _ = QFileDialog.getSaveFileName(self, "Save File", "", "Python Files (*.py)")
        if path:
            with open(path, 'w', encoding='utf-8') as f:
                f.write(self.editor.toPlainText())

    def run_code(self):
        code = self.editor.toPlainText()
        temp_file = os.path.join(os.getcwd(), "temp.py")
        with open(temp_file, 'w', encoding='utf-8') as f:
            f.write(code)

        result = subprocess.run([sys.executable, temp_file],
                                capture_output=True, text=True)

        output = result.stdout + "\n" + result.stderr
        self.output.setPlainText(output)

    def handle_autocomplete(self):
        cursor = self.editor.textCursor()
        cursor.select(cursor.SelectionType.WordUnderCursor)
        word = cursor.selectedText()

        matches = [w for w in self.suggestions if w.startswith(word)]
        if matches and word:
            self.completer.show_completions(matches, self.editor)
        else:
            self.completer.hide()