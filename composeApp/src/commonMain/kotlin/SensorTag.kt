import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.juul.kable.Filter
import com.juul.kable.Scanner
import com.juul.kable.logs.Logging

val SERVICE_UUID = sensorTagUuid("f0001130-0451-4000-b000-000000000000")
val CHARACTERISTIC_UUID = sensorTagUuid("f0001133-0451-4000-b000-000000000000")

val scanner = Scanner {
    logging {
        level = Logging.Level.Events
    }
    filters = listOf(Filter.NamePrefix("D10-T1F"))
}

private fun sensorTagUuid(UuidFor16Bit: String): Uuid = uuidFrom(UuidFor16Bit)

private fun characteristicOf(service: Uuid, characteristic: Uuid) =
    com.juul.kable.characteristicOf(service.toString(), characteristic.toString())

val bluetoothCharacteristic = characteristicOf(
    service = SERVICE_UUID,
    characteristic = CHARACTERISTIC_UUID
)