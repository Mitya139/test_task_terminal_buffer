package terminalbuffer

class Line(val width: Int) {

    private val cells: MutableList<Cell>

    init {
        require(width > 0) { "Line width must be greater than 0" }
        cells = MutableList(width) { Cell.empty() }
    }

    fun getCell(column: Int): Cell {
        validateColumn(column)
        return cells[column]
    }

    fun setCell(column: Int, cell: Cell) {
        validateColumn(column)
        cells[column] = cell
    }

    fun setChar(column: Int, char: Char?, attributes: TextAttributes) {
        validateColumn(column)
        cells[column] = Cell(char, attributes)
    }

    fun fill(char: Char?, attributes: TextAttributes) {
        for (i in 0 until width) {
            cells[i] = Cell(char, attributes)
        }
    }

    fun clear() {
        fill(null, TextAttributes.DEFAULT)
    }

    fun asString(): String {
        return cells.joinToString(separator = "") { it.renderedChar().toString() }
    }

    fun copyOf(): Line {
        val copy = Line(width)
        for (i in 0 until width) {
            copy.setCell(i, cells[i].copy())
        }
        return copy
    }

    private fun validateColumn(column: Int) {
        require(column in 0 until width) {
            "Column $column is out of bounds for line width $width"
        }
    }
}