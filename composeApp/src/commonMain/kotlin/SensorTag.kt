import com.juul.kable.Filter
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging

val scanner = Scanner {
    logging {
        level = Logging.Level.Events
    }
    filters = listOf(Filter.NamePrefix("D10-T1F"))
}