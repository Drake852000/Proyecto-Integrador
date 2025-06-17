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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.proyectointegrador.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

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
                RetrofitClient.api.obtenerTop10Caudales()
            }

            Log.d("DatosReutilizados", "Valores desde API: $flujos")

            datosReutilizados.clear()
            datosReutilizados.addAll(flujos.map { it.caudal })
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
            TanqueConNivel(nivelAgua = sensorData.nivelAgua.toFloat())


            Spacer(modifier = Modifier.height(10.dp))
            Text("Flujo: ${sensorData.flujoAgua} L/min", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(30.dp))
            Text("Reutilización de agua", style = MaterialTheme.typography.titleMedium)

            ReutilizacionAguaChart(datosReutilizados)
        }
    }
}

@Composable
fun TanqueConNivel(nivelAgua: Float) {
    val containerWidthDp = 200.dp
    val containerHeightDp = 250.dp

    // Dimensiones y posición del *interior* del tanque ahora rectangular, en unidades del viewport.
    // Basado en el nuevo pathData: M 50 20 H 150 V 230 H 50 Z
    val innerTankLeftPx = 50f        // X del borde izquierdo del rectángulo
    val innerTankTopPx = 20f         // Y del borde superior del rectángulo
    val innerTankWidthPx = 100f      // Ancho del rectángulo (150 - 50 = 100)
    val innerTankHeightPx = 210f     // Alto del rectángulo (230 - 20 = 210)

    // Calculamos las proporciones en Float primero
    val widthScaleFactor = innerTankWidthPx / 200f // 200f es viewportWidth
    val heightScaleFactor = innerTankHeightPx / 250f // 250f es viewportHeight

    // Ahora calculamos las dimensiones en Dp usando las proporciones Float
    val waterAreaWidthDp = containerWidthDp * widthScaleFactor
    val waterAreaHeightDp = containerHeightDp * heightScaleFactor

    val currentWaterLevelRatio = (nivelAgua / 100f).coerceIn(0f, 1f)
    val actualWaterHeightDp = waterAreaHeightDp * currentWaterLevelRatio

    Box(
        modifier = Modifier.size(width = containerWidthDp, height = containerHeightDp)
    ) {
        // --- EL AGUA (Box con color de fondo) ---
        // Calcula la posición Y superior del agua dentro del contenedor.
        // Posición Y del tope del área de llenado (en Dp)
        val waterAreaTopInContainerDp = containerHeightDp * (innerTankTopPx / 250f)

        // Calculamos la posición Y del TOP del agua, ajustando para el nivel actual
        val waterTopYPositionInContainerDp = waterAreaTopInContainerDp + (waterAreaHeightDp - actualWaterHeightDp)

        Box(
            modifier = Modifier
                // Offset horizontal: Posición X del agua desde el borde izquierdo del contenedor
                .offset(x = containerWidthDp * (innerTankLeftPx / 200f),
                    // Offset vertical: Posición Y del agua desde el borde superior del contenedor
                    y = waterTopYPositionInContainerDp)
                .width(waterAreaWidthDp)
                .height(actualWaterHeightDp)
                .background(
                    color = Color(0xFF2196F3).copy(alpha = 0.5f), // Color del agua
                    // Aquí puedes decidir si quieres el agua perfectamente rectangular (RoundedCornerShape(0.dp))
                    // o si quieres una muy ligera redondez para la superficie superior del agua (ej: RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    shape = RoundedCornerShape(0.dp) // Agua perfectamente rectangular
                )
        )

        // --- El CONTORNO del Tanque (Vector Asset) ---
        // Se dibuja *después* del agua para que el contorno sea visible.
        Image(
            painter = painterResource(id = R.drawable.ic_tank_outline), // ¡Confirma que este ID sea correcto!
            contentDescription = "Contorno del tanque",
            modifier = Modifier.fillMaxSize()
        )

        // --- El TEXTO del Porcentaje ---
        Text(
            text = "${nivelAgua.toInt()}%",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black,
            modifier = Modifier.align(Alignment.Center)
        )
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

