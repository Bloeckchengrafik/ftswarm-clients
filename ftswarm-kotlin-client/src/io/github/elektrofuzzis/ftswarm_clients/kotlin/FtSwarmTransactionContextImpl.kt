package io.github.elektrofuzzis.ftswarm_clients.kotlin

import io.github.elektrofuzzis.ftswarm_clients.kotlin.domain.CommandRequest
import io.github.elektrofuzzis.ftswarm_clients.kotlin.domain.SubscriptionParser
import io.github.elektrofuzzis.ftswarm_clients.kotlin.domain.SucceedingCommandReturnValue
import io.github.elektrofuzzis.ftswarm_clients.kotlin.transport.FtSwarmTransport
import io.github.elektrofuzzis.ftswarm_clients.kotlin.transport.RawCommandResult
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlin.coroutines.CoroutineContext

private val logger = KotlinLogging.logger { }

interface FtSwarmTransactionContext {
    suspend fun <T : SucceedingCommandReturnValue> command(request: CommandRequest<T>): Result<T>
    fun <T : Any> getSubscriptions(port: String, parser: SubscriptionParser<T>): Flow<T>
}

class LocalFtSwarmTransactionContext(
    private val cx: FtSwarmTransactionContext,
    override val coroutineContext: CoroutineContext
) : FtSwarmTransactionContext by cx, CoroutineScope

class FtSwarmTransactionContextImpl(
    private val transport: FtSwarmTransport,
    private val coroutineContext: CoroutineContext
) : FtSwarmTransactionContext {
    fun child(name: String) = LocalFtSwarmTransactionContext(
        cx = this,
        coroutineContext = coroutineContext + CoroutineName(name)
    )

    override suspend fun <T : SucceedingCommandReturnValue> command(
        request: CommandRequest<T>
    ): Result<T> {
        return when (val result = transport.command(request.toString())) {
            is RawCommandResult.Error -> Result.failure(FtSwarmException(result.message))
            is RawCommandResult.Success -> request.parser.parse(result.value)
        }
    }

    override fun <T : Any> getSubscriptions(
        port: String,
        parser: SubscriptionParser<T>
    ): Flow<T> {
        return transport.subscriptionsFor(port)
            .mapNotNull {
                val result = parser.parse(it.entry)
                result.getOrNull() ?: run {
                    logger.warn("Error in subscription parser ($port)", result.exceptionOrNull())
                    return@mapNotNull null
                }
            }
    }
}