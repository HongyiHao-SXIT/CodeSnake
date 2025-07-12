from PyQt6.QtGui import QFileSystemModel
from PyQt6.QtWidgets import QTreeView, QVBoxLayout, QWidget

class ProjectBrowser(QWidget):
    def __init__(self, parent=None):
        super().__init__(parent)
        layout = QVBoxLayout(self)
        self.tree = QTreeView()
        self.model = QFileSystemModel()
        self.model.setRootPath("")
        self.tree.setModel(self.model)
        self.tree.setRootIndex(self.model.index("."))
        layout.addWidget(self.tree)
        self.setLayout(layout)
