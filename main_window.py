from PyQt6.QtWidgets import QMainWindow, QTextEdit, QFileDialog, QMenuBar, QSplitter, QVBoxLayout, QWidget, QAction
from PyQt6.QtCore import Qt
import subprocess
import sys
import os

class CodeSnakeMainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("CodeSnake IDE")
        self.resize(1000, 600)

        self.editor = QTextEdit()
        self.output = QTextEdit()
        self.output.setReadOnly(True)

        splitter = QSplitter(Qt.Orientation.Vertical)
        splitter.addWidget(self.editor)
        splitter.addWidget(self.output)

        layout = QVBoxLayout()
        layout.addWidget(splitter)
        central = QWidget()
        central.setLayout(layout)
        self.setCentralWidget(central)

        self._init_menu()

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
