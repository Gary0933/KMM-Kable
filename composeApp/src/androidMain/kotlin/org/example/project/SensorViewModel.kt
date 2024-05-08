package org.example.project

//import CHARACTERISTIC_UUID
//import SERVICE_UUID
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Bluetooth
import com.juul.kable.ConnectionLostException
import com.juul.kable.State
import com.juul.kable.peripheral
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import peripheralScope
import kotlin.time.Duration.Companion.seconds


private val reconnectDelay = 1.seconds

sealed class ViewState {
    data object BluetoothUnavailable : ViewState()
    data object Connecting : ViewState()
    data object Connected : ViewState()
    data object Disconnecting : ViewState()
    data object Disconnected : ViewState()
}

val ViewState.label: String
    get() = when (this) {
        ViewState.BluetoothUnavailable -> "Bluetooth unavailable"
        ViewState.Connecting -> "Connecting"
        ViewState.Connected -> "Connected"
        ViewState.Disconnecting -> "Disconnecting"
        ViewState.Disconnected -> "Disconnected"
    }

class SensorViewModel(
    application: Application,
    bluetoothAddress: String
) : AndroidViewModel(application) {
    private val currentBluetoothAddress = bluetoothAddress
    private val scope = CoroutineScope(peripheralScope.coroutineContext + Job(peripheralScope.coroutineContext.job))
    private val autoConnect = MutableStateFlow(false)
    private val manualDisconnect = MutableStateFlow(false)
    private val peripheral = scope.peripheral(bluetoothAddress) {
        autoConnectIf(autoConnect::value)
    }
    private val state = combine(Bluetooth.availability, peripheral.state, ::Pair)

    init {
        //android.util.Log.d("K-Test", "Manual Disconnect Flag : ${manualDisconnect.value}")
        //getConnectionStatus()
        //viewModelScope.enableAutoReconnect()
    }

    private fun CoroutineScope.enableAutoReconnect() {
        state.filter { (bluetoothAvailability, connectionState) ->
            bluetoothAvailability == Bluetooth.Availability.Available && connectionState is State.Disconnected
        }.onEach {
            ensureActive()
            //Log.info { "Waiting $reconnectDelay to reconnect..." }
            delay(reconnectDelay)
            connect()
        }.launchIn(this)
    }

    private fun CoroutineScope.connect() {
        launch {
            //Log.debug { "Connecting" }
            android.util.Log.d("K-Test", "Start to connect, current status : ${getConnectionStatus()}")

            try {
                android.util.Log.d("K-Test", "Current Manual Disconnect Flag : ${manualDisconnect.value}")
                if (!manualDisconnect.value) {
                    peripheral.connect()
                    autoConnect.value = true
                    getConnectionStatus()
                }
            } catch (e: ConnectionLostException) {
                autoConnect.value = false
                //Log.warn(e) { "Connection attempt failed" }
            }
        }
    }

    private fun CoroutineScope.disconnect() {
        launch {
            autoConnect.value = false
            manualDisconnect.value = true
            //Log.debug { "Disconnect the bluetooth" }
            try {
                android.util.Log.d("K-Test", "Start to disconnect")
                peripheral.disconnect()
                //scope.cancel()
                getConnectionStatus()
            } catch (e: ConnectionLostException) {
                //Log.warn(e) { "Disconnection attempt failed" }
                android.util.Log.d("K-Test", "Disconnection attempt failed")
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val viewState: Flow<ViewState> = state.flatMapLatest { (bluetoothAvailability, state) ->
        if (bluetoothAvailability is Bluetooth.Availability.Unavailable) {
            return@flatMapLatest flowOf(ViewState.BluetoothUnavailable)
        }
        when (state) {
            is State.Connecting -> flowOf(ViewState.Connecting)
            is State.Connected -> flowOf(ViewState.Connected)
            is State.Disconnecting -> flowOf(ViewState.Disconnecting)
            is State.Disconnected -> flowOf(ViewState.Disconnected)
        }
    }

    fun disconnectBluetooth(): String {
        getConnectionStatus()
        viewModelScope.disconnect()
        return currentBluetoothAddress
    }

    fun connectBluetooth() {
        manualDisconnect.value = false
        android.util.Log.d("K-Test", "Manual Disconnect Flag : ${manualDisconnect.value}")
        getConnectionStatus()
        viewModelScope.enableAutoReconnect()
    }

    fun getConnectionStatus(): String {
        val connectionStatus = when (peripheral.state.value) {
            is State.Connecting -> "Connecting"
            State.Connected -> "Connected"
            State.Disconnecting -> "Disconnecting"
            is State.Disconnected -> "Disconnected"
            else -> return "Unknown Status"
        }
        android.util.Log.d("K-Test", "BLE Connection Status : ${connectionStatus}")
        return connectionStatus
    }

  fun getBluetoothData() {
        val services = peripheral.services ?: android.util.Log.d("K-Test", "Services have not been discovered")
        android.util.Log.d("K-Test", "123")
        //val observation = peripheral.observe(bluetoothCharacteristic)
        //val data = peripheral.read(bluetoothCharacteristic)
        //android.util.Log.d("K-Test", "BLE data : $data")
    }

    /*
    private fun characteristicOf(service: Uuid, characteristic: Uuid) =
        com.juul.kable.characteristicOf(service.toString(), characteristic.toString())

    private val bluetoothCharacteristic = characteristicOf(
        service = SERVICE_UUID,
        characteristic = CHARACTERISTIC_UUID
    )
     */

    /*
    fun clearedJob() {
        peripheralScope.launch {
            viewModelScope.coroutineContext.job.join()
            peripheral.disconnect()
            scope.cancel()
        }
    }
    */

    override fun onCleared() {
        android.util.Log.d("K-Test", "onCleared")
    }

}