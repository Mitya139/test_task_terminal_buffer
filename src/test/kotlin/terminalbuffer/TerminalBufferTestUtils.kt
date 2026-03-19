package terminalbuffer

import kotlin.test.assertEquals

internal fun assertScreenContent(buffer: TerminalBuffer, vararg expectedLines: String) {
    assertEquals(
        expectedLines.joinToString("\n"),
        buffer.getScreenContentAsString()
    )
}

internal fun assertAllContent(buffer: TerminalBuffer, vararg expectedLines: String) {
    assertEquals(
        expectedLines.joinToString("\n"),
        buffer.getAllContentAsString()
    )
}