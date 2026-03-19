package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TerminalBufferCursorAndAttributesTest {

    @Test
    fun setCursorPositionSetsExactCoordinates() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMaxSize = 10)

        buffer.setCursorPosition(column = 4, row = 2)

        assertEquals(4, buffer.cursorColumn)
        assertEquals(2, buffer.cursorRow)
    }

    @Test
    fun setCursorPositionRejectsOutOfBoundsCoordinates() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMaxSize = 10)

        assertFailsWith<IllegalArgumentException> {
            buffer.setCursorPosition(column = -1, row = 0)
        }

        assertFailsWith<IllegalArgumentException> {
            buffer.setCursorPosition(column = 5, row = 0)
        }

        assertFailsWith<IllegalArgumentException> {
            buffer.setCursorPosition(column = 0, row = -1)
        }

        assertFailsWith<IllegalArgumentException> {
            buffer.setCursorPosition(column = 0, row = 3)
        }
    }

    @Test
    fun cursorMovementStaysWithinScreenBounds() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMaxSize = 10)

        buffer.moveCursorLeft(10)
        buffer.moveCursorUp(10)
        assertEquals(0, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)

        buffer.moveCursorRight(10)
        buffer.moveCursorDown(10)
        assertEquals(4, buffer.cursorColumn)
        assertEquals(2, buffer.cursorRow)
    }

    @Test
    fun cursorMovementRejectsNegativeCounts() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMaxSize = 10)

        assertFailsWith<IllegalArgumentException> { buffer.moveCursorUp(-1) }
        assertFailsWith<IllegalArgumentException> { buffer.moveCursorDown(-1) }
        assertFailsWith<IllegalArgumentException> { buffer.moveCursorLeft(-1) }
        assertFailsWith<IllegalArgumentException> { buffer.moveCursorRight(-1) }
    }

    @Test
    fun attributeSettersUpdateCurrentAttributes() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMaxSize = 10)

        buffer.setForeground(TerminalColor.RED)
        buffer.setBackground(TerminalColor.BLUE)
        buffer.setBold(true)
        buffer.setItalic(true)
        buffer.setUnderline(true)

        assertEquals(
            TextAttributes(
                foreground = TerminalColor.RED,
                background = TerminalColor.BLUE,
                bold = true,
                italic = true,
                underline = true
            ),
            buffer.currentAttributes
        )
    }

    @Test
    fun setCurrentAttributesReplacesWholeAttributeSet() {
        val buffer = TerminalBuffer(width = 5, height = 3, scrollbackMaxSize = 10)

        val attrs = TextAttributes(
            foreground = TerminalColor.GREEN,
            background = TerminalColor.BLACK,
            bold = true,
            italic = false,
            underline = true
        )

        buffer.setCurrentAttributes(attrs)

        assertEquals(attrs, buffer.currentAttributes)
    }

    @Test
    fun fillLineUsesCurrentAttributes() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMaxSize = 10)

        buffer.setForeground(TerminalColor.YELLOW)
        buffer.setBackground(TerminalColor.BLUE)
        buffer.setBold(true)

        buffer.fillLine(row = 1, char = '#')

        assertEquals("####", buffer.getLineAsString(1))

        for (column in 0 until 4) {
            assertEquals('#', buffer.getCharAt(globalRow = 1, column = column))
            assertEquals(
                TextAttributes(
                    foreground = TerminalColor.YELLOW,
                    background = TerminalColor.BLUE,
                    bold = true,
                    italic = false,
                    underline = false
                ),
                buffer.getAttributesAt(globalRow = 1, column = column)
            )
        }
    }
}