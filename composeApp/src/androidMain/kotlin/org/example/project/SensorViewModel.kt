package org.example.project

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.juul.kable.Peripheral
import com.juul.kable.peripheral
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import peripheralScope

class SensorViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val scope = CoroutineScope(peripheralScope.coroutineContext + Job(peripheralScope.coroutineContext.job))
    private val autoConnect = MutableStateFlow(false)

    fun getConnection(bluetoothAddress: String) {
        val peripheral = scope.peripheral(bluetoothAddress) {
            autoConnectIf(autoConnect::value)
        }

        viewModelScope.connect(peripheral)
    }

    private fun CoroutineScope.connect(peripheral : Peripheral) {
        launch {
            peripheral.connect()
        }
    }













}