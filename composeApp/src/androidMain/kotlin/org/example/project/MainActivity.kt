package org.example.project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.juul.kable.AndroidAdvertisement
import com.juul.kable.Bluetooth
import com.juul.kable.Reason
import com.juul.sensortag.AppTheme
import com.juul.sensortag.icons.BluetoothDisabled
import com.juul.sensortag.icons.LocationDisabled
import permissionsNeeded
import com.juul.sensortag.showLocationSettings
import com.juul.sensortag.enableBluetooth
import com.juul.sensortag.openAppDetails


class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<ScanViewModel>()
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Column(Modifier.background(color = MaterialTheme.colors.background)) {
                    val bluetooth = Bluetooth.availability.collectAsState(initial = null).value
                    AppBar(viewModel, bluetooth)
                    Box(Modifier.weight(1f)) {
                        ScanPane(bluetooth)
                        StatusSnackbar(viewModel)
                    }
                }
            }
        }
    }

    @ExperimentalPermissionsApi
    @Composable
    private fun ScanPane(bluetooth: Bluetooth.Availability?) {
        ProvideTextStyle(
            TextStyle(color = contentColorFor(backgroundColor = MaterialTheme.colors.background))
        ){
            val permissionsState = rememberMultiplePermissionsState(Bluetooth.permissionsNeeded)
            var didAskForPermission by remember { mutableStateOf(false) }

            if (!didAskForPermission) {
                didAskForPermission = true
                SideEffect {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }

            if (permissionsState.allPermissionsGranted) {
                PermissionGranted(bluetooth)
            } else {
                if (permissionsState.shouldShowRationale) {
                    BluetoothPermissionsNotGranted(permissionsState)
                } else {
                    BluetoothPermissionsNotAvailable(::openAppDetails)
                }
            }
        }
    }

    @Composable
    private fun PermissionGranted(bluetooth: Bluetooth.Availability?) {
        when (bluetooth) {
            Bluetooth.Availability.Available -> {
                AdvertisementsList(
                    advertisements = viewModel.advertisements.collectAsState().value,
                    onRowClick = ::onAdvertisementClicked
                )
            }
            is Bluetooth.Availability.Unavailable -> when (bluetooth.reason) {
                Reason.LocationServicesDisabled -> LocationServicesDisabled(::showLocationSettings)
                Reason.Off, Reason.TurningOff -> BluetoothDisabled(::enableBluetooth)
                Reason.TurningOn -> Loading()
                null -> BluetoothUnavailable()
            }
            null -> Loading()
        }
    }

    private fun onAdvertisementClicked(advertisement: AndroidAdvertisement) {
        viewModel.stop()

        val intent = Intent(this@MainActivity, SensorActivity::class.java)
        intent.putExtra("Advertisement", advertisement.address)
        startActivity(intent)
    }

    @Composable
    private fun BluetoothUnavailable() {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "Bluetooth unavailable.")
        }
    }



}

@Composable
private fun AppBar(viewModel: ScanViewModel, bluetooth: Bluetooth.Availability?) {
    val status = viewModel.status.collectAsState().value

    TopAppBar(
        title = {
            Text("SensorTag Example")
        },
        actions = {
            if (bluetooth == Bluetooth.Availability.Available) {
                if (status !is ScanStatus.Scanning) {
                    IconButton(onClick = viewModel::start) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
                IconButton(onClick = viewModel::clear) {
                    Icon(Icons.Filled.Delete, contentDescription = "Clear")
                }
            }
        }
    )
}

@Composable
private fun AdvertisementsList(
    advertisements: List<AndroidAdvertisement>,
    onRowClick: (AndroidAdvertisement) -> Unit
) {
    LazyColumn {
        items(advertisements.size) { index ->
            val advertisement = advertisements[index]
            AdvertisementRow(advertisement) { onRowClick(advertisement) }
        }
    }
}

@Composable
private fun AdvertisementRow(advertisement: AndroidAdvertisement, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                fontSize = 22.sp,
                text = advertisement.name ?: "Unknown",
            )
            Text(advertisement.address)
        }

        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = "${advertisement.rssi} dBm",
        )
    }
}

@Composable
private fun LocationServicesDisabled(enableAction: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.LocationDisabled,
        contentDescription = "Location services disabled",
        description = "Location services are disabled.",
        buttonText = "Enable",
        onClick = enableAction,
    )
}

@Composable
private fun ActionRequired(
    icon: ImageVector,
    contentDescription: String?,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            modifier = Modifier.size(150.dp),
            tint = contentColorFor(backgroundColor = MaterialTheme.colors.background),
            imageVector = icon,
            contentDescription = contentDescription,
        )
        Spacer(Modifier.size(8.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = description,
        )
        Spacer(Modifier.size(15.dp))
        Button(onClick) {
            Text(buttonText)
        }
    }
}

@Composable
private fun BluetoothDisabled(enableAction: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.BluetoothDisabled,
        contentDescription = "Bluetooth disabled",
        description = "Bluetooth is disabled.",
        buttonText = "Enable",
        onClick = enableAction,
    )
}

@Composable
private fun Loading() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@ExperimentalPermissionsApi
@Composable
private fun BluetoothPermissionsNotGranted(permissions: MultiplePermissionsState) {
    ActionRequired(
        icon = Icons.Filled.LocationDisabled,
        contentDescription = "Bluetooth permissions required",
        description = "Bluetooth permissions are required for scanning. Please grant the permission.",
        buttonText = "Continue",
        onClick = permissions::launchMultiplePermissionRequest,
    )
}

@Composable
private fun BluetoothPermissionsNotAvailable(openSettingsAction: () -> Unit) {
    ActionRequired(
        icon = Icons.Filled.Warning,
        contentDescription = "Bluetooth permissions required",
        description = "Bluetooth permission denied. Please, grant access on the Settings screen.",
        buttonText = "Open Settings",
        onClick = openSettingsAction,
    )
}

@Composable
private fun BoxScope.StatusSnackbar(viewModel: ScanViewModel) {
    val status = viewModel.status.collectAsState().value

    if (status !is ScanStatus.Stopped) {
        val text = when (status) {
            ScanStatus.Scanning -> "Scanning"
            ScanStatus.Stopped -> "Idle"
            is ScanStatus.Failed -> "Error: ${status.message}"
        }
        Snackbar(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp)
        ) {
            Text(text, style = MaterialTheme.typography.body1)
        }
    }
}
