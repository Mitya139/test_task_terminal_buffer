package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals

class TerminalBufferWriteTextTest {

    @Test
    fun writeTextWritesPrintableCharactersAndMovesCursor() {
        val buffer = TerminalBuffer(width = 5, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("abc")

        assertScreenContent(
            buffer,
            "abc  ",
            "     "
        )
        assertEquals(3, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)
    }

    @Test
    fun writeTextWrapsToNextLineAfterLastColumn() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("abcd")

        assertScreenContent(
            buffer,
            "abc",
            "d  "
        )
        assertEquals(1, buffer.cursorColumn)
        assertEquals(1, buffer.cursorRow)
    }

    @Test
    fun writeTextDoesNotScrollImmediatelyAfterWritingBottomRightCell() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("abcdef")

        assertScreenContent(
            buffer,
            "abc",
            "def"
        )
        assertEquals(0, buffer.getScrollbackSize())
        assertEquals(2, buffer.cursorColumn)
        assertEquals(1, buffer.cursorRow)

        buffer.writeText("g")

        assertScreenContent(
            buffer,
            "def",
            "g  "
        )
        assertAllContent(
            buffer,
            "abc",
            "def",
            "g  "
        )
        assertEquals(1, buffer.getScrollbackSize())
        assertEquals(1, buffer.cursorColumn)
        assertEquals(1, buffer.cursorRow)
    }

    @Test
    fun writeTextSupportsLineFeed() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("ab\ncd")

        assertScreenContent(
            buffer,
            "ab  ",
            "cd  "
        )
        assertEquals(2, buffer.cursorColumn)
        assertEquals(1, buffer.cursorRow)
    }

    @Test
    fun writeTextSupportsCarriageReturn() {
        val buffer = TerminalBuffer(width = 4, height = 1, scrollbackMaxSize = 10)

        buffer.writeText("ab\rc")

        assertScreenContent(
            buffer,
            "cb  "
        )
        assertEquals(1, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)
    }

    @Test
    fun writeTextSupportsTabUsingTabStopsOfEightColumns() {
        val buffer = TerminalBuffer(width = 10, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("a\tb")

        assertScreenContent(
            buffer,
            "a       b ",
            "          "
        )
        assertEquals(9, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)
    }

    @Test
    fun writeTextAppliesCurrentAttributesToWrittenCells() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMaxSize = 10)

        buffer.setForeground(TerminalColor.BRIGHT_CYAN)
        buffer.setBackground(TerminalColor.BLACK)
        buffer.setUnderline(true)

        buffer.writeText("ab")

        val expectedAttrs = TextAttributes(
            foreground = TerminalColor.BRIGHT_CYAN,
            background = TerminalColor.BLACK,
            bold = false,
            italic = false,
            underline = true
        )

        assertEquals(expectedAttrs, buffer.getAttributesAt(globalRow = 0, column = 0))
        assertEquals(expectedAttrs, buffer.getAttributesAt(globalRow = 0, column = 1))
    }
}