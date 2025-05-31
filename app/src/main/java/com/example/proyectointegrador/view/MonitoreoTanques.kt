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
import androidx.compose.ui.platform.LocalContext
import com.example.proyectointegrador.util.showTankLowNotification
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import android.graphics.Color
import android.util.Log


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoreoTanques() {
    var sensorData by remember { mutableStateOf(SensorData()) }
    val context = LocalContext.current
    var notified by remember { mutableStateOf(false) }

    val datosReutilizados = listOf(5f, 8f, 6.5f, 9f, 10f, 7.3f, 8.8f) // datos simulados

    LaunchedEffect(Unit) {
        Log.d("Vista", "Iniciando observador...") // Debug
        observeSensorData { newData ->
            Log.d("Vista", "Datos recibidos: $newData") // Debug
            sensorData = newData

            if (sensorData.nivelAgua <= 15 && !notified) {
                Log.d("Notificación", "Nivel bajo detectado") // Debug
                showTankLowNotification(context)
                notified = true
            } else if (sensorData.nivelAgua > 15) {
                notified = false
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
            Text("🌊 Nivel de Agua: ${sensorData.nivelAgua}%", style = MaterialTheme.typography.headlineSmall)
            println("🌊 Nivel de Agua: ${sensorData.nivelAgua}%")
            Spacer(modifier = Modifier.height(20.dp))
            LinearProgressIndicator(progress = (sensorData.nivelAgua / 100f).coerceIn(0f, 1f))
            Spacer(modifier = Modifier.height(10.dp))
            Text("💧 Flujo: ${sensorData.flujoAgua} L/min", style = MaterialTheme.typography.bodyLarge)
            println("💧 Flujo: ${sensorData.flujoAgua} L/min")
            Spacer(modifier = Modifier.height(30.dp))
            Text("📈 Reutilización de agua", style = MaterialTheme.typography.titleMedium)
            ReutilizacionAguaChart(datosReutilizados)
        }
    }
}

@Composable
fun ReutilizacionAguaChart(datos: List<Float>) {
    AndroidView(factory = { context ->
        val chart = LineChart(context)

        val entries = datos.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }

        val dataSet = LineDataSet(entries, "Litros reutilizados").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(true)
        }

        chart.data = LineData(dataSet)
        chart.description = Description().apply { text = "Últimos días" }
        chart.axisRight.isEnabled = false
        chart.setTouchEnabled(true)
        chart.invalidate()

        chart
    },
        modifier = Modifier
            .fillMaxSize()
            .height(250.dp)
    )
}