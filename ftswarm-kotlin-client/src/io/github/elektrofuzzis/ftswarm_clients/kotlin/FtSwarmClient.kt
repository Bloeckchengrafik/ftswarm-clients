package io.github.elektrofuzzis.ftswarm_clients.kotlin

import io.github.elektrofuzzis.ftswarm_clients.kotlin.transport.FtSwarmSerialTransport
import io.github.elektrofuzzis.ftswarm_clients.kotlin.transport.FtSwarmTransport
import io.github.elektrofuzzis.ftswarm_clients.kotlin.transport.SerialPortIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

suspend fun FtSwarmClient(port: SerialPortIdentifier, parent: CoroutineScope? = null): FtSwarmClient {
    val parentContext = parent?.coroutineContext ?: currentCoroutineContext()
    val job = SupervisorJob(parentContext[Job])

    return FtSwarmClient(
        FtSwarmSerialTransport(port),
        parentContext + job,
    )
}

class FtSwarmClient(private val transport: FtSwarmTransport, coroutineContext: CoroutineContext) : Closeable {
    private val job = coroutineContext[Job]!!
    internal val context = FtSwarmTransactionContextImpl(
        transport,
        coroutineContext
    )

    override fun close() {
        job.cancel()
        transport.close()
    }
}