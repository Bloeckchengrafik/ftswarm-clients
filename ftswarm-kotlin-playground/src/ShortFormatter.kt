import java.util.logging.Formatter
import java.util.logging.LogRecord

class ShortFormatter : Formatter() {

    override fun format(record: LogRecord): String {
        val logger = record.loggerName
            ?.split('.')
            ?.let { parts ->
                if (parts.size <= 1) {
                    parts.joinToString(".")
                } else {
                    parts.dropLast(1)
                        .joinToString(".") { it.take(1) } +
                            "." + parts.last()
                }
            }
            ?: ""
        val lengthConstrainedLogger = logger.takeLast(32).padEnd(32)

        return "${record.level.name.padEnd(7)} $lengthConstrainedLogger | ${formatMessage(record)}\n"
    }
}