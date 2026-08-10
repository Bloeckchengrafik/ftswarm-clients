package io.github.elektrofuzzis.ftswarm_clients.kotlin.transport

import com.fazecast.jSerialComm.SerialPort
import github.elektrofuzzis.ftswarm_clients.kotlin.codegen.vendorIds
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filter
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}
private val serialLogger = KotlinLogging.logger("io.github.elektrofuzzis.ftswarm_clients.kotlin.transport.Serial")
private val swarmLogger = KotlinLogging.logger("io.github.elektrofuzzis.ftswarm_clients.kotlin.transport.FtSwarm")

@JvmInline
value class SerialPortIdentifier(val port: String)

fun discoverPorts(): List<SerialPortIdentifier> =
    SerialPort.getCommPorts()
        .filter { port ->
            vendorIds.any {
                port.vendorID == it.vid &&
                        port.productID == it.pid
            }
        }
        .map { SerialPortIdentifier(it.systemPortName) }

fun getFtSwarmPort(): SerialPortIdentifier = discoverPorts().firstOrNull()
    ?: error("No FTSwarm device found")

suspend fun FtSwarmSerialTransport(
    port: SerialPortIdentifier,
): FtSwarmSerialTransport {
    val serial = withContext(Dispatchers.IO) {
        SerialPort.getCommPort(port.port).also {
            it.baudRate = 115200

            check(it.openPort()) {
                "Failed to open serial port ${port.port}"
            }

            it.setComPortTimeouts(
                SerialPort.TIMEOUT_NONBLOCKING,
                0,
                0,
            )
        }
    }

    return try {
        FtSwarmSerialTransport(serial).also {
            it.initialize()
        }
    } catch (t: Throwable) {
        serial.closePort()
        throw t
    }
}

class FtSwarmSerialTransport internal constructor(
    private val serial: SerialPort,
) : FtSwarmTransport {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private val writes = Channel<WriteRequest>(
        capacity = Channel.UNLIMITED,
    )

    private val pending = Channel<CompletableDeferred<RawCommandResult>>(
        capacity = Channel.UNLIMITED,
    )

    private val initialized = CompletableDeferred<Unit>()

    private val closed = AtomicBoolean(false)

    private val subscriptionFlow = MutableSharedFlow<RawSubscription>(
        extraBufferCapacity = 128,
    )

    init {
        scope.launch { writerLoop() }
        scope.launch { readerLoop() }
    }

    /**
     * Performs initialization sequence
     * and waits for an @@@ response.
     */
    internal suspend fun initialize() {
        writeRaw("\r\n")
        drainOutput()

        withTimeout(5.seconds) {
            writeRaw(serialCommandBytes("startCLI"))
            initialized.await()
        }
    }

    /**
     * Send one command and suspend until its corresponding R: or error result.
     *
     * Multiple callers may invoke this concurrently. Commands and results are
     * correlated FIFO.
     */
    override suspend fun command(command: String): RawCommandResult {
        return enqueueCommand(command).await()
    }

    /**
     * Pipeline a stream of commands.
     *
     * Commands may be in flight concurrently on the wire; responses are
     * matched FIFO according to the protocol guarantee.
     */
    override fun commands(
        commands: Flow<String>,
    ): Flow<RawCommandResult> = channelFlow {
        val results = Channel<CompletableDeferred<RawCommandResult>>(
            capacity = Channel.UNLIMITED,
        )

        launch {
            try {
                commands.collect { command ->
                    results.send(enqueueCommand(command))
                }
            } finally {
                results.close()
            }
        }

        for (result in results) {
            send(result.await())
        }
    }

    /**
     * Retrieves a stream of subscriptions for the specified port.
     *
     * @param port The port for which subscriptions are to be fetched.
     * @return A flow of [RawSubscription] objects, each representing a subscription related to the specified port.
     */
    override fun subscriptionsFor(port: String): Flow<RawSubscription> = subscriptionFlow
        .filter { it.port == port }

    override fun close() {
        if (!closed.compareAndSet(false, true)) {
            return
        }

        val cause = CancellationException(
            "Serial transport closed"
        )

        writes.close(cause)
        pending.close(cause)

        while (true) {
            val result = pending.tryReceive().getOrNull()
                ?: break

            result.completeExceptionally(cause)
        }

        scope.cancel(cause)
        serial.closePort()
    }

    private suspend fun writerLoop() {
        try {
            for (request in writes) {
                when (request) {
                    is WriteRequest.Raw -> {
                        writeBytes(request.bytes)
                        request.completed.complete(Unit)
                    }

                    is WriteRequest.Command -> {
                        // Register first so a fast response cannot overtake us.
                        pending.send(request.result)

                        try {
                            writeBytes(request.bytes)
                        } catch (t: Throwable) {
                            request.result.completeExceptionally(t)
                            throw t
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            failTransport(t)
        }
    }

    private suspend fun readerLoop() {
        val buffer = ByteArray(1024)
        val input = SerialInputBuffer()

        try {
            while (currentCoroutineContext().isActive) {
                val available = serial.bytesAvailable()

                if (available < 0) {
                    error("Serial port is unavailable")
                }

                if (available == 0) {
                    delay(10.milliseconds)
                    continue
                }

                val count = serial.readBytes(
                    buffer,
                    minOf(buffer.size, available),
                )

                if (count < 0) {
                    error("Serial read failed")
                }

                if (count == 0) {
                    continue
                }

                traceCommunicationMessage("RX", buffer, count)

                for (i in 0 until count) {
                    when (
                        val event = input.accept(
                            byte = buffer[i],
                            detectInitialization = !initialized.isCompleted,
                        )
                    ) {
                        SerialInputEvent.Initialized -> {
                            serialLogger.trace { "Serial: ${"CLI initialized"}" }
                            initialized.complete(Unit)
                        }

                        is SerialInputEvent.Line -> handleLine(event.value)
                        null -> Unit
                    }
                }
            }
        } catch (t: Throwable) {
            if (currentCoroutineContext().isActive) {
                failTransport(t)
            }
        }
    }

    private fun handleLine(line: String) {
        if (!initialized.isCompleted) {
            if (line.startsWith("@@@")) {
                initialized.complete(Unit)
            } else {
                swarmLog(line)
            }

            return
        }

        when {
            line.startsWith("R: ") -> {
                completeNext(
                    RawCommandResult.Success(
                        line.removePrefix("R: ")
                    )
                )
            }

            ERROR_LINE.matches(line) -> {
                completeNext(
                    RawCommandResult.Error(
                        line.trimStart()
                            .removePrefix("^")
                            .trimStart()
                    )
                )
            }

            line.startsWith("S: ") -> {
                onSubscription(line)
            }

            else -> {
                swarmLog(line)
            }
        }
    }

    private fun completeNext(
        result: RawCommandResult,
    ) {
        val waiter = pending.tryReceive().getOrNull()

        if (waiter == null) {
            logger.warn { "Unexpected command result with no pending command: $result" }
            return
        }

        serialLogger.trace { "Serial: ${"Command completed: $result"}" }
        waiter.complete(result)
    }

    private suspend fun enqueueCommand(command: String): CompletableDeferred<RawCommandResult> {
        initialized.await()

        val result = CompletableDeferred<RawCommandResult>()

        writes.send(
            WriteRequest.Command(
                bytes = serialCommandBytes(command),
                result = result,
            )
        )

        return result
    }

    private suspend fun writeRaw(text: String) {
        writeRaw(text.toByteArray(StandardCharsets.UTF_8))
    }

    private suspend fun writeRaw(bytes: ByteArray) {
        val completed = CompletableDeferred<Unit>()

        writes.send(
            WriteRequest.Raw(
                bytes = bytes,
                completed = completed,
            )
        )

        completed.await()
    }

    private suspend fun writeBytes(bytes: ByteArray) = withContext(Dispatchers.IO) {
        traceCommunicationMessage("TX", bytes, bytes.size)
        var offset = 0

        while (offset < bytes.size) {
            val written = serial.writeBytes(
                bytes,
                bytes.size - offset,
                offset,
            )

            check(written > 0) {
                "Serial write failed: $written"
            }

            offset += written
        }
    }

    private suspend fun drainOutput() {
        while (serial.bytesAwaitingWrite() > 0) {
            delay(1.milliseconds)
        }
    }

    private fun failTransport(t: Throwable) {
        if (!closed.compareAndSet(false, true)) {
            return
        }

        logger.error(t) { "Serial transport failed" }

        if (!initialized.isCompleted) {
            initialized.completeExceptionally(t)
        }

        while (true) {
            val result = pending.tryReceive().getOrNull()
                ?: break

            result.completeExceptionally(t)
        }

        scope.cancel(CancellationException("Serial transport failed", t))

        serial.closePort()
    }

    private fun swarmLog(line: String) {
        if (line.isEmpty()) return
        swarmLogger.info(line)
    }

    private fun onSubscription(line: String) {
        line.toRawSubscription()
            .onSuccess { subscription ->
                if (!subscriptionFlow.tryEmit(subscription)) {
                    logger.warn { "Subscription dropped: $subscription" }
                }
            }
            .onFailure { t ->
                logger.error(t) { "Failed to parse subscription: $line" }
            }
    }

    private fun traceCommunicationMessage(direction: String, bytes: ByteArray, length: Int) {
        serialLogger.trace {
            "Serial: $direction $length bytes: ${bytes.serialDebugString(length)}"
        }
    }

    private sealed interface WriteRequest {
        class Raw(
            val bytes: ByteArray,
            val completed: CompletableDeferred<Unit>,
        ) : WriteRequest

        class Command(
            val bytes: ByteArray,
            val result: CompletableDeferred<RawCommandResult>,
        ) : WriteRequest
    }

    private companion object {
        val ERROR_LINE = Regex("""^\s*(?:\^|\[ERROR]).*""")
    }
}

internal fun serialCommandBytes(command: String): ByteArray =
    command.trimEnd('\r', '\n')
        .plus("\r\n")
        .toByteArray(StandardCharsets.UTF_8)

internal fun ByteArray.serialDebugString(length: Int = size): String =
    buildString {
        for (index in 0 until length) {
            when (val byte = this@serialDebugString[index].toInt() and 0xff) {
                '\r'.code -> append("\\r")
                '\n'.code -> append("\\n")
                '\t'.code -> append("\\t")
                in 0x20..0x7e -> append(byte.toChar())
                else -> append("\\x${byte.toString(16).uppercase().padStart(2, '0')}")
            }
        }
    }

internal sealed interface SerialInputEvent {
    data object Initialized : SerialInputEvent

    data class Line(
        val value: String,
    ) : SerialInputEvent
}

/**
 * Frames the incoming byte stream without requiring the CLI initialization
 * marker to be newline-terminated.
 */
internal class SerialInputBuffer {
    private val line = StringBuilder()

    fun accept(
        byte: Byte,
        detectInitialization: Boolean,
    ): SerialInputEvent? {
        val character = byte.toInt().toChar()

        if (character == '\n') {
            val value = line.toString().removeSuffix("\r")
            line.clear()
            return SerialInputEvent.Line(value)
        }

        line.append(character)

        if (detectInitialization && line.contentEquals("@@@")) {
            line.clear()
            return SerialInputEvent.Initialized
        }

        return null
    }
}
