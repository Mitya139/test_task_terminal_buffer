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
        // TODO: overwrite mode with wrapping and scrolling
    }

    fun insertText(text: String) {
        // TODO: insert mode with shifting and wrapping
    }

    fun fillLine(row: Int, char: Char?) {
        validateScreenRow(row)
        screen[row].fill(char, currentAttributes)
    }

    fun insertEmptyLineAtBottom() {
        appendToScrollback(screen.first())
        screen.removeAt(0)
        screen.add(Line(width))
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