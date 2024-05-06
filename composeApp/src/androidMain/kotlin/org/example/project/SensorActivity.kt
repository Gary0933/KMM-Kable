package org.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.juul.sensortag.AppTheme

class SensorActivity : ComponentActivity()  {

    private val viewModel by viewModels<SensorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val advertisement = intent.getStringExtra("Advertisement")

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
                            if (advertisement != null) {
                                Text(advertisement)
                                viewModel.getConnection(advertisement)
                            }
                        }
                    }

                }
            }
        }


    }










}