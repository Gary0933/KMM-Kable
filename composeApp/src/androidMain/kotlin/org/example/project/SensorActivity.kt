package org.example.project

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juul.sensortag.AppTheme


class SensorActivity : ComponentActivity()  {

    private lateinit var viewModel: SensorViewModel // 延迟创建SensorViewModel的实例

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val advertisement = intent.getStringExtra("Advertisement").toString()
            viewModel = SensorViewModel(application,advertisement) // 传入参数再实例化SensorViewModel

            AppTheme {
                Column(
                    Modifier
                        .background(color = MaterialTheme.colors.background)
                        .fillMaxSize()
                ) {
                    TopAppBar(title = { Text("SensorTag Example") })

                    ProvideTextStyle(
                        TextStyle(color = contentColorFor(backgroundColor = MaterialTheme.colors.background))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            val viewState = viewModel.viewState.collectAsState(ViewState.Disconnected).value

                            Text(viewState.label, fontSize = 18.sp)
                            Spacer(Modifier.size(10.dp))
                            Text(advertisement)
                            Spacer(Modifier.size(10.dp))
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.connectBluetooth()
                        },
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text("Connect Bluetooth")
                    }

                    Button(
                        onClick = {
                            Toast.makeText(
                                this@SensorActivity,
                                viewModel.disconnectBluetooth(),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text("Disconnect Bluetooth")
                    }

                    Button(
                        onClick = {
                            Toast.makeText(
                                this@SensorActivity,
                                viewModel.getConnectionStatus(),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text("Check Bluetooth Connection Status")
                    }

                    Button(
                        onClick = {
                            viewModel.getBluetoothData()
                        },
                        Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Text("Get Bluetooth Data")
                    }

                }
            }
        }


    }

    override fun onDestroy() {
        android.util.Log.d("K-Test", "Activity Destroy")
        super.onDestroy()

    }








}