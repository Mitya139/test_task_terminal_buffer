package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals

class TerminalBufferInsertAndHistoryTest {

    @Test
    fun insertTextShiftsCellsToTheRightOnSameLine() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("ab")
        buffer.setCursorPosition(column = 1, row = 0)

        buffer.insertText("XY")

        assertScreenContent(
            buffer,
            "aXYb",
            "    "
        )
        assertEquals(3, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)
    }

    @Test
    fun insertTextCarriesOverflowToNextLine() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("abcd")
        buffer.setCursorPosition(column = 1, row = 0)

        buffer.insertText("X")

        assertScreenContent(
            buffer,
            "aXb",
            "cd "
        )
        assertEquals(2, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)
    }

    @Test
    fun insertTextAtLastColumnUsesPendingWrapForNextCharacter() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 10)

        buffer.setCursorPosition(column = 2, row = 0)
        buffer.insertText("XY")

        assertScreenContent(
            buffer,
            "  X",
            "Y  "
        )
        assertEquals(1, buffer.cursorColumn)
        assertEquals(1, buffer.cursorRow)
    }

    @Test
    fun insertTextClipsOverflowPastBottomRightCorner() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("abcdef")
        buffer.setCursorPosition(column = 2, row = 1)

        buffer.insertText("X")

        assertScreenContent(
            buffer,
            "abc",
            "deX"
        )
        assertEquals(2, buffer.cursorColumn)
        assertEquals(1, buffer.cursorRow)
    }

    @Test
    fun insertTextSupportsControlCharactersToo() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMaxSize = 10)

        buffer.insertText("ab\nc\rZ")

        assertScreenContent(
            buffer,
            "ab  ",
            "Zc  "
        )
        assertEquals(1, buffer.cursorColumn)
        assertEquals(1, buffer.cursorRow)
    }

    @Test
    fun insertEmptyLineAtBottomScrollsScreenIntoScrollback() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("abcdef")
        buffer.insertEmptyLineAtBottom()

        assertScreenContent(
            buffer,
            "def",
            "   "
        )
        assertAllContent(
            buffer,
            "abc",
            "def",
            "   "
        )
        assertEquals(1, buffer.getScrollbackSize())
    }

    @Test
    fun clearScreenKeepsScrollbackButClearsVisibleScreen() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("abcdefg")
        buffer.clearScreen()

        assertScreenContent(
            buffer,
            "   ",
            "   "
        )
        assertAllContent(
            buffer,
            "abc",
            "   ",
            "   "
        )
        assertEquals(1, buffer.getScrollbackSize())
        assertEquals(0, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)
    }

    @Test
    fun clearScreenAndScrollbackClearsEverything() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("abcdefg")
        buffer.clearScreenAndScrollback()

        assertScreenContent(
            buffer,
            "   ",
            "   "
        )
        assertAllContent(
            buffer,
            "   ",
            "   "
        )
        assertEquals(0, buffer.getScrollbackSize())
        assertEquals(0, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)
    }

    @Test
    fun contentAccessCanReadScrollbackLines() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 10)

        buffer.setForeground(TerminalColor.RED)
        buffer.writeText("abc")

        buffer.setForeground(TerminalColor.GREEN)
        buffer.writeText("defg")

        assertEquals(1, buffer.getScrollbackSize())

        assertEquals('a', buffer.getCharAt(globalRow = 0, column = 0))
        assertEquals(TerminalColor.RED, buffer.getAttributesAt(globalRow = 0, column = 0).foreground)

        assertEquals('d', buffer.getCharAt(globalRow = 1, column = 0))
        assertEquals(TerminalColor.GREEN, buffer.getAttributesAt(globalRow = 1, column = 0).foreground)

        assertEquals('g', buffer.getCharAt(globalRow = 2, column = 0))
        assertEquals(TerminalColor.GREEN, buffer.getAttributesAt(globalRow = 2, column = 0).foreground)
    }
}