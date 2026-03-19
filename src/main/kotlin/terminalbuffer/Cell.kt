package terminalbuffer

data class Cell(
    val char: Char? = null,
    val attributes: TextAttributes = TextAttributes.DEFAULT
) {
    fun renderedChar(): Char = char ?: ' '

    companion object {
        fun empty(): Cell = Cell()
    }
}