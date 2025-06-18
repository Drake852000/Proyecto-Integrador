package com.example.proyectointegrador.view

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectointegrador.network.RetrofitClient
import com.example.proyectointegrador.model.SensorData
import com.example.proyectointegrador.view.observeSensorData
import com.example.proyectointegrador.util.showTankNotification
import com.example.proyectointegrador.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.charts.LineChart
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoreoTanques() {
    var sensorData by remember { mutableStateOf(SensorData()) }
    val context = LocalContext.current
    var notified by remember { mutableStateOf(false) }
    val datosReutilizados = remember { mutableStateListOf<Float>() }
    var isLoadingChartData by remember { mutableStateOf(true) } // Estado para el indicador de carga del gráfico

    // Paleta de colores de MaterialTheme para una mejor integración.
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorOnPrimary = MaterialTheme.colorScheme.onPrimary
    val colorSurface = MaterialTheme.colorScheme.surface // Color de fondo de las Cards
    val colorOnSurface = MaterialTheme.colorScheme.onSurface // Color del texto sobre las Cards
    val colorBackground = MaterialTheme.colorScheme.background // Color de fondo de la pantalla

    // Tus colores personalizados, si quieres usarlos directamente o como parte del tema.
    // Aunque tealOscuro ya no se usa para el flujo, se mantiene aquí si lo necesitas para otras cosas.
    val azulClaro = Color(0xFFB3E5FC)
    val azulProfundo = Color(0xFF0288D1)
    val turquesa = Color(0xFF4DD0E1)
    val tealOscuro = Color(0xFF00796B)


    // Observa los datos del sensor (Firebase o simulación)
    LaunchedEffect(Unit) {
        observeSensorData { newData ->
            sensorData = newData
            // Lógica de notificación (sin cambios funcionales)
            if (sensorData.nivelAgua <= 15 && !notified) {
                showTankNotification(context, "Nivel de agua bajo", "El agua no está fluyendo correctamente")
                notified = true
            } else if (sensorData.flujoAgua >= 90 && !notified) {
                showTankNotification(context, "Nivel de agua alto", "El agua puede desbordarse")
                notified = true
            } else if (sensorData.flujoAgua < 90 && sensorData.nivelAgua > 15) {
                notified = false
            }
        }
    }

    // Llama a la API para datos del gráfico (flujos más altos de la última hora)
    LaunchedEffect(Unit) {
        isLoadingChartData = true
        try {
            val responseList = withContext(Dispatchers.IO) {
                RetrofitClient.api.obtenerTop10Caudales()
            }
            datosReutilizados.clear()
            datosReutilizados.addAll(responseList.map { it.caudal })
        } catch (e: Exception) {
            Log.e("API Error", "Error al obtener flujos para gráfico: ${e.message}", e)
        } finally {
            isLoadingChartData = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Monitoreo de Tanques",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colorOnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Implementar navegación hacia atrás */ }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorOnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorBackground,
                    titleContentColor = colorOnSurface,
                    actionIconContentColor = colorOnSurface,
                    navigationIconContentColor = colorOnSurface
                )
            )
        },
        containerColor = colorBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(colorBackground),
            verticalArrangement = Arrangement.spacedBy(16.dp), // Espacio entre las tarjetas
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta de Nivel de Agua
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(containerColor = colorSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "🌊 Nivel de Agua",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colorOnSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    TanqueConNivel(nivelAgua = sensorData.nivelAgua.toFloat())
                }
            }

            // --- SE REMOVIÓ LA TARJETA DE FLUJO DE AGUA COMPLETA ---

            // Tarjeta de Gráfico de Reutilización de Agua
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colorSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "📈 Historial de Caudal (Última Hora)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colorOnSurface
                    )
                    Spacer(Modifier.height(12.dp))

                    // Indicador de carga o gráfico
                    if (isLoadingChartData) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colorPrimary)
                            Text(
                                "Cargando datos del gráfico...",
                                modifier = Modifier.padding(top = 60.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorOnSurface.copy(alpha = 0.7f)
                            )
                        }
                    } else if (datosReutilizados.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No hay datos disponibles para el gráfico en la última hora.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorOnSurface.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        ReutilizacionAguaChart(datosReutilizados)
                    }
                }
            }
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
