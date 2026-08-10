package io.github.elektrofuzzis.ftswarm_clients.kotlin.transport

data class RawSubscription(
    val port: String,
    val entry: String
)

private val subscriptionRegex = Regex("""^S: (\S+) (.*)$""")

fun String.toRawSubscription(): Result<RawSubscription> {
    val match = subscriptionRegex.matchEntire(this)
        ?: return Result.failure(IllegalArgumentException("Invalid subscription: $this"))
    return Result.success(RawSubscription(match.groupValues[1], match.groupValues[2]))
}