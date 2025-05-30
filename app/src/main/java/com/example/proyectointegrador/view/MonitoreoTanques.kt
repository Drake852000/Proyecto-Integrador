package com.example.proyectointegrador.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyectointegrador.model.SensorData
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.proyectointegrador.util.showTankLowNotification



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoreoTanques() {
    var sensorData by remember { mutableStateOf(SensorData()) }
    val context = LocalContext.current
    var notified by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        observeSensorData {
            sensorData = it

            if (sensorData.nivelAgua <= 15 && !notified) {
                showTankLowNotification(context)
                notified = true
            } else if (sensorData.nivelAgua > 15) {
                notified = false // se reinicia si vuelve a subir
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Monitoreo de Tanques") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🌊 Nivel de Agua: ${sensorData.nivelAgua}%" , style = MaterialTheme.typography.headlineSmall)
            Text("💧 Flujo: ${sensorData.flujoAgua} L/min", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(20.dp))
            LinearProgressIndicator(progress = (sensorData.nivelAgua / 100f).coerceIn(0f, 1f))
            Spacer(modifier = Modifier.height(10.dp))
            Text("${sensorData.nivelAgua}%")
        }
    }
}

