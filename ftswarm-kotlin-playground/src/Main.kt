import io.github.elektrofuzzis.ftswarm_clients.kotlin.FtSwarmClient
import io.github.elektrofuzzis.ftswarm_clients.kotlin.objects.button
import io.github.elektrofuzzis.ftswarm_clients.kotlin.transport.getFtSwarmPort
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.logging.LogManager

private val logger = KotlinLogging.logger {}

class Main

fun loadLoggingConfig() {
    Main::class.java.getResourceAsStream("/logging.properties").use {
        LogManager.getLogManager().readConfiguration(it)
    }
}

suspend fun main() {
    loadLoggingConfig()

    FtSwarmClient(getFtSwarmPort()).use { client ->
        client.button("S1").value.collect {
            logger.info { "Switch value: $it" }
        }
    }
}
