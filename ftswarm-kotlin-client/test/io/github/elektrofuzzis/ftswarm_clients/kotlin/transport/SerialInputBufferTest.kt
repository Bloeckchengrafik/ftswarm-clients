package io.github.elektrofuzzis.ftswarm_clients.kotlin.transport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SerialInputBufferTest {
    @Test
    fun `initialization marker does not need a line ending`() {
        val input = SerialInputBuffer()

        assertNull(input.accept('@'.code.toByte(), detectInitialization = true))
        assertNull(input.accept('@'.code.toByte(), detectInitialization = true))
        assertEquals(
            SerialInputEvent.Initialized,
            input.accept('@'.code.toByte(), detectInitialization = true),
        )
    }

    @Test
    fun `lines are framed across individual bytes`() {
        val input = SerialInputBuffer()
        var event: SerialInputEvent? = null

        for (byte in "R: Ok\r\n".encodeToByteArray()) {
            event = input.accept(byte, detectInitialization = false) ?: event
        }

        assertEquals(SerialInputEvent.Line("R: Ok"), event)
    }

    @Test
    fun `return values before initialization remain ordinary lines`() {
        val input = SerialInputBuffer()
        var event: SerialInputEvent? = null

        for (byte in "R: stale\n".encodeToByteArray()) {
            event = input.accept(byte, detectInitialization = true) ?: event
        }

        assertEquals(SerialInputEvent.Line("R: stale"), event)
    }

    @Test
    fun `serial debug output escapes line endings and binary bytes`() {
        val bytes = byteArrayOf('A'.code.toByte(), '\r'.code.toByte(), '\n'.code.toByte(), 0)

        assertEquals("A\\r\\n\\x00", bytes.serialDebugString())
    }

    @Test
    fun `commands use the CLI CRLF line ending`() {
        assertEquals("startCLI\r\n", serialCommandBytes("startCLI\r\n").decodeToString())
    }
}
