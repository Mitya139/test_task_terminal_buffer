package terminalbuffer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TerminalBufferInitializationTest {

    @Test
    fun constructorCreatesEmptyScreenOfRequestedSize() {
        val buffer = TerminalBuffer(width = 4, height = 3, scrollbackMaxSize = 10)

        assertEquals(0, buffer.cursorColumn)
        assertEquals(0, buffer.cursorRow)
        assertEquals(TextAttributes.DEFAULT, buffer.currentAttributes)

        assertScreenContent(
            buffer,
            "    ",
            "    ",
            "    "
        )
    }

    @Test
    fun constructorRejectsInvalidArguments() {
        assertFailsWith<IllegalArgumentException> {
            TerminalBuffer(width = 0, height = 3, scrollbackMaxSize = 10)
        }

        assertFailsWith<IllegalArgumentException> {
            TerminalBuffer(width = 4, height = 0, scrollbackMaxSize = 10)
        }

        assertFailsWith<IllegalArgumentException> {
            TerminalBuffer(width = 4, height = 3, scrollbackMaxSize = -1)
        }
    }

    @Test
    fun getCharAtReturnsNullForEmptyCell() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMaxSize = 10)

        assertNull(buffer.getCharAt(globalRow = 0, column = 0))
        assertNull(buffer.getCharAt(globalRow = 1, column = 3))
    }

    @Test
    fun getAttributesAtReturnsDefaultForEmptyCell() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMaxSize = 10)

        assertEquals(TextAttributes.DEFAULT, buffer.getAttributesAt(globalRow = 0, column = 0))
        assertEquals(TextAttributes.DEFAULT, buffer.getAttributesAt(globalRow = 1, column = 3))
    }

    @Test
    fun contentAccessRejectsOutOfBoundsCoordinates() {
        val buffer = TerminalBuffer(width = 4, height = 2, scrollbackMaxSize = 10)

        assertFailsWith<IllegalArgumentException> {
            buffer.getCharAt(globalRow = 0, column = -1)
        }

        assertFailsWith<IllegalArgumentException> {
            buffer.getCharAt(globalRow = 0, column = 4)
        }

        assertFailsWith<IllegalArgumentException> {
            buffer.getLineAsString(globalRow = -1)
        }

        assertFailsWith<IllegalArgumentException> {
            buffer.getLineAsString(globalRow = 2)
        }
    }
}