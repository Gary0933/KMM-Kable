import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.juul.kable.Filter
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging

//val SERVICE_UUID = sensorTagUuid("0xF0001130");
//val CHARACTERISTIC_UUID = sensorTagUuid("0xF0001132");

val scanner = Scanner {
    logging {
        level = Logging.Level.Events
    }
    filters = listOf(Filter.NamePrefix("D10-T1F"))
}

//private fun sensorTagUuid(shortUuid: String): Uuid = uuidFrom(shortUuid)