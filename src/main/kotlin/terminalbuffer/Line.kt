package terminalbuffer

class Line(val width: Int) {

    private val cells: MutableList<Cell>

    init {
        require(width > 0) { "Line width must be greater than 0" }
        cells = MutableList(width) { Cell.empty() }
    }

}