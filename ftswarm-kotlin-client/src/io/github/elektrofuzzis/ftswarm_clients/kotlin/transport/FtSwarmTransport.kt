package io.github.elektrofuzzis.ftswarm_clients.kotlin.transport

import kotlinx.coroutines.flow.Flow
import java.io.Closeable


interface FtSwarmTransport : Closeable {
    /**
     * Executes a stream of commands provided as a flow, returning a flow of results for each command.
     *
     * @param commands The flow of command strings to be executed.
     * @return A flow of results where each result is of type [RawCommandResult], indicating success or failure for the respective command.
     */
    fun commands(commands: Flow<String>): Flow<RawCommandResult>

    /**
     * Executes a single command and returns the result.
     */
    suspend fun command(command: String): RawCommandResult

    /**
     * Retrieves a stream of subscriptions for the specified port.
     *
     * @param port The port for which subscriptions are to be fetched.
     * @return A flow of [RawSubscription] objects, each representing a subscription related to the specified port.
     */
    fun subscriptionsFor(port: String): Flow<RawSubscription>
}