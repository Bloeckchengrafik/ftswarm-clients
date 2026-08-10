package io.github.elektrofuzzis.ftswarm_clients.kotlin.transport

sealed interface RawCommandResult {
    data class Success(
        val value: String,
    ) : RawCommandResult

    data class Error(
        val message: String,
    ) : RawCommandResult
}