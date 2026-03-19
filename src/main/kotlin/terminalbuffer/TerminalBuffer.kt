package terminalbuffer

class TerminalBuffer(
    val width: Int,
    val height: Int,
    val scrollbackMaxSize: Int
) {
    private val screen: MutableList<Line>
    private val scrollback: MutableList<Line> = mutableListOf()

    var cursorColumn: Int = 0
        private set

    var cursorRow: Int = 0
        private set

    var currentAttributes: TextAttributes = TextAttributes.DEFAULT
        private set

    init {
        require(width > 0) { "Width must be greater than 0" }
        require(height > 0) { "Height must be greater than 0" }
        require(scrollbackMaxSize >= 0) { "Scrollback max size must be >= 0" }
        screen = MutableList(height) { Line(width) }
    }

    // Attributes

    fun setCurrentAttributes(attributes: TextAttributes) {
        currentAttributes = attributes
    }

    fun setForeground(color: TerminalColor) {
        currentAttributes = currentAttributes.copy(foreground = color)
    }

    fun setBackground(color: TerminalColor) {
        currentAttributes = currentAttributes.copy(background = color)
    }

    fun setBold(enabled: Boolean) {
        currentAttributes = currentAttributes.copy(bold = enabled)
    }

    fun setItalic(enabled: Boolean) {
        currentAttributes = currentAttributes.copy(italic = enabled)
    }

    fun setUnderline(enabled: Boolean) {
        currentAttributes = currentAttributes.copy(underline = enabled)
    }

    // Cursor

    fun setCursorPosition(column: Int, row: Int) {
        cursorColumn = column.coerceIn(0, width - 1)
        cursorRow = row.coerceIn(0, height - 1)
    }

    fun moveCursorUp(count: Int = 1) {
        require(count >= 0) { "Count must be >= 0" }
        cursorRow = (cursorRow - count).coerceAtLeast(0)
    }

    fun moveCursorDown(count: Int = 1) {
        require(count >= 0) { "Count must be >= 0" }
        cursorRow = (cursorRow + count).coerceAtMost(height - 1)
    }

    fun moveCursorLeft(count: Int = 1) {
        require(count >= 0) { "Count must be >= 0" }
        cursorColumn = (cursorColumn - count).coerceAtLeast(0)
    }

    fun moveCursorRight(count: Int = 1) {
        require(count >= 0) { "Count must be >= 0" }
        cursorColumn = (cursorColumn + count).coerceAtMost(width - 1)
    }

    // Editing

    fun writeText(text: String) {
        if (text.isEmpty()) return

        for (ch in text) {
            screen[cursorRow].setChar(cursorColumn, ch, currentAttributes)
            advanceCursorForWrite()
        }
    }

    fun insertText(text: String) {
        if (text.isEmpty()) return

        var row = cursorRow
        var column = cursorColumn

        for (ch in text) {
            val scrolled = insertCellAt(row, column, Cell(ch, currentAttributes))

            if (scrolled && row > 0) {
                row--
            }

            val nextPosition = getNextCursorPosition(row, column)
            row = nextPosition.first
            column = nextPosition.second
        }

        cursorRow = row
        cursorColumn = column
    }

    fun fillLine(row: Int, char: Char?) {
        validateScreenRow(row)
        screen[row].fill(char, currentAttributes)
    }

    fun insertEmptyLineAtBottom() {
        scrollScreenUp()
    }

    fun clearScreen() {
        for (line in screen) {
            line.clear()
        }
        setCursorPosition(0, 0)
    }

    fun clearScreenAndScrollback() {
        clearScreen()
        scrollback.clear()
    }

    // Content access

    fun getCharAt(globalRow: Int, column: Int): Char? {
        validateColumn(column)
        return resolveLine(globalRow).getCell(column).char
    }

    fun getAttributesAt(globalRow: Int, column: Int): TextAttributes {
        validateColumn(column)
        return resolveLine(globalRow).getCell(column).attributes
    }

    fun getLineAsString(globalRow: Int): String {
        return resolveLine(globalRow).asString()
    }

    fun getScreenContentAsString(): String {
        return screen.joinToString(separator = "\n") { it.asString() }
    }

    fun getAllContentAsString(): String {
        return (scrollback + screen).joinToString(separator = "\n") { it.asString() }
    }

    fun getScrollbackSize(): Int {
        return scrollback.size
    }

    // Helpers

    private fun advanceCursorForWrite() {
        if (cursorColumn < width - 1) {
            cursorColumn++
            return
        }

        cursorColumn = 0

        if (cursorRow < height - 1) {
            cursorRow++
        } else {
            scrollScreenUp()
            cursorRow = height - 1
        }
    }

    private fun insertCellAt(row: Int, column: Int, cell: Cell): Boolean {
        var currentRow = row
        var currentColumn = column
        var carry = cell

        while (true) {
            val displaced = screen[currentRow].getCell(currentColumn)
            screen[currentRow].setCell(currentColumn, carry)
            carry = displaced

            if (carry == Cell.empty()) {
                return false
            }

            if (currentColumn < width - 1) {
                currentColumn++
            } else {
                currentColumn = 0

                if (currentRow < height - 1) {
                    currentRow++
                } else {
                    scrollScreenUp()
                    screen[height - 1].setCell(0, carry)
                    return true
                }
            }
        }
    }

    private fun getNextCursorPosition(row: Int, column: Int): Pair<Int, Int> {
        return when {
            column < width - 1 -> Pair(row, column + 1)
            row < height - 1 -> Pair(row + 1, 0)
            else -> Pair(height - 1, width - 1)
        }
    }


    private fun scrollScreenUp() {
        appendToScrollback(screen.first())
        screen.removeAt(0)
        screen.add(Line(width))
    }

    private fun resolveLine(globalRow: Int): Line {
        val totalLines = scrollback.size + screen.size
        require(globalRow in 0 until totalLines) {
            "Row $globalRow is out of bounds for total line count $totalLines"
        }

        return if (globalRow < scrollback.size) {
            scrollback[globalRow]
        } else {
            screen[globalRow - scrollback.size]
        }
    }

    private fun appendToScrollback(line: Line) {
        if (scrollbackMaxSize == 0) {
            return
        }

        if (scrollback.size == scrollbackMaxSize) {
            scrollback.removeAt(0)
        }

        scrollback.add(line.copyOf())
    }

    private fun validateScreenRow(row: Int) {
        require(row in 0 until height) {
            "Screen row $row is out of bounds for screen height $height"
        }
    }

    private fun validateColumn(column: Int) {
        require(column in 0 until width) {
            "Column $column is out of bounds for width $width"
        }
    }
}