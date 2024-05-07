package org.example.project

import android.app.Application
import com.juul.tuulbox.logging.Log
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
        viewModelScope.enableAutoReconnect()
    }

    private fun CoroutineScope.enableAutoReconnect() {
        state.filter { (bluetoothAvailability, connectionState) ->
            bluetoothAvailability == Bluetooth.Availability.Available && connectionState is State.Disconnected
        }.onEach {
            ensureActive()
            Log.info { "Waiting $reconnectDelay to reconnect..." }
            delay(reconnectDelay)
            connect()
        }.launchIn(this)
    }

    private fun CoroutineScope.connect() {
        launch {
            Log.debug { "Connecting" }
            try {
                if (!manualDisconnect.value) {
                    peripheral.connect()
                    autoConnect.value = true
                }
            } catch (e: ConnectionLostException) {
                autoConnect.value = false
                Log.warn(e) { "Connection attempt failed" }
            }
        }
    }

    private fun CoroutineScope.disconnect() {
        launch {

            Log.debug { "Disconnect the bluetooth" }
            try {
                peripheral.disconnect()

            } catch (e: ConnectionLostException) {
                Log.warn(e) { "Disconnection attempt failed" }
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
        autoConnect.value = false
        manualDisconnect.value = true
        viewModelScope.disconnect()
        return currentBluetoothAddress
    }

    /*
    fun clearedJob() {
        peripheralScope.launch {
            viewModelScope.coroutineContext.job.join()
            peripheral.disconnect()
            scope.cancel()
        }
    }
    */

}