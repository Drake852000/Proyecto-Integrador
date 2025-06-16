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
import androidx.compose.ui.graphics.Color
import com.example.proyectointegrador.util.showTankNotification
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import com.example.proyectointegrador.network.RetrofitClient
import com.github.mikephil.charting.components.XAxis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoreoTanques() {
    var sensorData by remember { mutableStateOf(SensorData()) }
    val context = LocalContext.current
    var notified by remember { mutableStateOf(false) }

    val datosReutilizados = remember { mutableStateListOf<Float>() }

    //  Obtener datos de Firebase (porcentaje y flujo)
    LaunchedEffect(Unit) {
        Log.d("Vista", "Iniciando observador de Firebase...") // Debug
        observeSensorData { newData ->
            Log.d("Vista", "Datos recibidos: $newData") // Debug
            sensorData = newData

            if (sensorData.nivelAgua <= 15 && !notified) {
                Log.d("Notificación", "Nivel bajo detectado") // Debug
                showTankNotification(context, "Nivel de agua bajo", "El agua no esta fluyendo correctamente")
                notified = true
            } else if(sensorData.flujoAgua >= 90 && !notified) {
                Log.d("Notificación", "Nivel alto detectado")
                showTankNotification(context, "Nivel de agua alto", "El agua puede desbordarse")
            } else if (sensorData.flujoAgua < 90 && sensorData.nivelAgua > 15) {
                notified = false
            }
        }
    }

    //  Obtener datos de Retrofit para la gráfica
    LaunchedEffect(Unit) {
        try {
            val flujos = withContext(Dispatchers.IO) {
                RetrofitClient.api.obtenerUltimosFlujos()
            }

            Log.d("DatosReutilizados", "Valores desde API: $flujos")
            datosReutilizados.clear()
            datosReutilizados.addAll(flujos.map { it.toFloat() })
        } catch (e: Exception) {
            Log.e("API Error", "Error al obtener flujos: ${e.message}")
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
            Spacer(modifier = Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = {
                    (sensorData.nivelAgua / 100f).coerceIn(0f, 1f)
                   },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF4CAF50),
                trackColor = Color.LightGray,
                strokeCap = StrokeCap.Round,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Flujo: ${sensorData.flujoAgua} L/min", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(30.dp))
            Text("Reutilización de agua", style = MaterialTheme.typography.titleMedium)

            ReutilizacionAguaChart(datosReutilizados)
        }
    }
}

@Composable
fun ReutilizacionAguaChart(datos: List<Float>) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description = Description().apply { text = "" }
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                setTouchEnabled(false)
                setScaleEnabled(false)
                setNoDataText("Sin datos disponibles")
            }
        },
        update = { chart ->
            val entries = datos.mapIndexed { index, value ->
                Entry(index.toFloat(), value)
            }

            Log.d("Chart", "Entradas en gráfica: $entries")

            if (entries.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val dataSet = LineDataSet(entries, "").apply {
                color = Color.Cyan.toArgb()
                lineWidth = 3f
                setDrawCircles(true)
                setDrawValues(false) // Oculta números
                setCircleColor(Color.Blue.toArgb())
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}

