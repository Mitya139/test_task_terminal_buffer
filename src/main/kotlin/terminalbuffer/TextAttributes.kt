package terminalbuffer

data class TextAttributes(
    val foreground: TerminalColor = TerminalColor.DEFAULT,
    val background: TerminalColor = TerminalColor.DEFAULT,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false
) {
    companion object {
        val DEFAULT = TextAttributes()
    }
}