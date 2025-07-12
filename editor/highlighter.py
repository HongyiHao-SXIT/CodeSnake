from PyQt6.QtGui import QSyntaxHighlighter, QTextCharFormat, QColor
from PyQt6.QtCore import QRegularExpression

class PythonHighlighter(QSyntaxHighlighter):
    def __init__(self, parent):
        super().__init__(parent)
        self.highlighting_rules = []

        keyword_format = QTextCharFormat()
        keyword_format.setForeground(QColor("blue"))
        keywords = [
            "def", "class", "if", "elif", "else", "while", "for", "in",
            "try", "except", "finally", "with", "as", "import", "from",
            "return", "yield", "pass", "break", "continue", "and", "or", "not", "is", "lambda"
        ]
        for word in keywords:
            pattern = QRegularExpression(f"\\b{word}\\b")
            self.highlighting_rules.append((pattern, keyword_format))

        comment_format = QTextCharFormat()
        comment_format.setForeground(QColor("darkGreen"))
        self.highlighting_rules.append((QRegularExpression("#[^\n]*"), comment_format))

        string_format = QTextCharFormat()
        string_format.setForeground(QColor("darkRed"))
        self.highlighting_rules.append((QRegularExpression("'[^']*'"), string_format))
        self.highlighting_rules.append((QRegularExpression('\"[^\"]*\"'), string_format))

    def highlightBlock(self, text):
        for pattern, fmt in self.highlighting_rules:
            it = pattern.globalMatch(text)
            while it.hasNext():
                match = it.next()
                self.setFormat(match.capturedStart(), match.capturedLength(), fmt)
