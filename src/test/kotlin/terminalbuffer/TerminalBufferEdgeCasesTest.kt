package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals

class TerminalBufferEdgeCasesTest {

    @Test
    fun scrollbackIsLimitedByScrollbackMaxSize() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 2)

        buffer.writeText("abcdefghijklmno")

        assertEquals(2, buffer.getScrollbackSize())

        assertScreenContent(
            buffer,
            "jkl",
            "mno"
        )

        assertAllContent(
            buffer,
            "def",
            "ghi",
            "jkl",
            "mno"
        )
    }

    @Test
    fun zeroScrollbackDisablesHistoryStorage() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 0)

        buffer.writeText("abcdefg")

        assertEquals(0, buffer.getScrollbackSize())

        assertScreenContent(
            buffer,
            "def",
            "g  "
        )

        assertAllContent(
            buffer,
            "def",
            "g  "
        )
    }

    @Test
    fun cursorMovementResetsPendingWrapBeforeNextWrite() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("abc")
        buffer.moveCursorLeft(2)
        buffer.writeText("Z")

        assertScreenContent(
            buffer,
            "Zbc",
            "   "
        )

        assertEquals(1, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)
        assertEquals(0, buffer.getScrollbackSize())
    }

    @Test
    fun writeTextWorksCorrectlyOnOneByOneBuffer() {
        val buffer = TerminalBuffer(width = 1, height = 1, scrollbackMaxSize = 10)

        buffer.writeText("ab")

        assertScreenContent(
            buffer,
            "b"
        )

        assertAllContent(
            buffer,
            "a",
            "b"
        )

        assertEquals(1, buffer.getScrollbackSize())
        assertEquals(0, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)
    }


    @Test
    fun insertTextClipsOverflowOnOneByOneBuffer() {
        val buffer = TerminalBuffer(width = 1, height = 1, scrollbackMaxSize = 10)

        buffer.insertText("a")

        assertScreenContent(
            buffer,
            "a"
        )

        buffer.setCursorPosition(0, 0)

        buffer.insertText("b")

        assertAllContent(
            buffer,
            "b"
        )

        assertEquals(0, buffer.getScrollbackSize())
        assertEquals(0, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)
    }


    @Test
    fun setCursorPositionResetsPendingWrapBeforeNextWrite() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("abc")
        buffer.setCursorPosition(column = 1, row = 1)
        buffer.writeText("Z")

        assertScreenContent(
            buffer,
            "abc",
            " Z "
        )

        assertEquals(2, buffer.cursorColumn)
        assertEquals(1, buffer.cursorRow)
        assertEquals(0, buffer.getScrollbackSize())
    }


    @Test
    fun insertTextStopsOnCellWithNullCharEvenIfAttributesAreNotDefault() {
        val buffer = TerminalBuffer(width = 4, height = 1, scrollbackMaxSize = 10)

        buffer.setForeground(TerminalColor.RED)
        buffer.fillLine(char = null)

        buffer.setCursorPosition(column = 0, row = 0)
        buffer.insertText("A")

        assertScreenContent(
            buffer,
            "A   "
        )

        assertEquals('A', buffer.getCharAt(globalRow = 0, column = 0))
        assertEquals(null, buffer.getCharAt(globalRow = 0, column = 1))
    }


    @Test
    fun clearScreenResetsPendingWrapBeforeNextWrite() {
        val buffer = TerminalBuffer(width = 3, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("abc")
        buffer.clearScreen()
        buffer.writeText("Z")

        assertScreenContent(
            buffer,
            "Z  ",
            "   "
        )

        assertEquals(1, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)
        assertEquals(0, buffer.getScrollbackSize())
    }


    @Test
    fun tabCanCauseWrapAndScrollOnNarrowBuffer() {
        val buffer = TerminalBuffer(width = 1, height = 2, scrollbackMaxSize = 10)

        buffer.writeText("\ta")

        assertScreenContent(
            buffer,
            " ",
            "a"
        )

        assertEquals(7, buffer.getScrollbackSize())
        assertEquals(0, buffer.cursorColumn)
        assertEquals(1, buffer.cursorRow)
    }
}