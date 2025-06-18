package com.example.proyectointegrador.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.proyectointegrador.R
import com.example.proyectointegrador.model.SensorData
import com.example.proyectointegrador.network.RetrofitClient
import com.example.proyectointegrador.ui.theme.PrimaryBlueDark
import com.example.proyectointegrador.ui.theme.PrimaryBlueLight
import com.example.proyectointegrador.util.showTankNotification
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoreoTanques() {
    var sensorData by remember { mutableStateOf(SensorData()) }
    val context = LocalContext.current
    var notified by remember { mutableStateOf(false) }
    val datosReutilizados = remember { mutableStateListOf<Float>() }
    var isLoadingChartData by remember { mutableStateOf(true) }

    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorOnPrimary = MaterialTheme.colorScheme.onPrimary
    val colorSurface = MaterialTheme.colorScheme.surface
    val colorOnSurface = MaterialTheme.colorScheme.onSurface
    val colorBackground = MaterialTheme.colorScheme.background

    val secondaryBackgroundColor = PrimaryBlueLight
    val tertiaryAccentColor = PrimaryBlueDark

    LaunchedEffect(Unit) {
        observeSensorData { newData ->
            sensorData = newData
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
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            // <--- ¡CAMBIO CLAVE AQUÍ! Añadimos sombra al estilo del texto
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f), // Color de la sombra (negro semi-transparente)
                                offset = Offset(x = 2f, y = 2f),       // Desplazamiento de la sombra (horizontal, vertical)
                                blurRadius = 4f                       // Grado de desenfoque de la sombra
                            )
                        ),
                        color = colorOnSurface // El color del texto sigue siendo el mismo
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
                    containerColor = secondaryBackgroundColor,
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .wrapContentWidth()
                    .widthIn(max = 300.dp)
                    .fillMaxHeight(0.5f),
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
// ... (Tus imports existentes para MonitoreoTanques y otros) ...

@Composable
fun TanqueConNivel(nivelAgua: Float) {
    // Reducimos las dimensiones generales del contenedor del tanque
    val containerWidthDp = 160.dp // Antes 200.dp
    val containerHeightDp = 200.dp // Antes 250.dp

    // Las dimensiones del *interior* del tanque en coordenadas de tu SVG (si es un vector asset)
    // Es CRÍTICO que estas coordenadas coincidan con el viewport y el pathData de tu ic_tank_outline.
    // Si tu SVG original tiene un viewport de 200x250 y el tanque interno va de 50,20 a 150,230,
    // estas proporciones son correctas RELATIVO al VIEWPORT original del SVG.
    val innerTankLeftPx = 50f
    val innerTankTopPx = 20f
    val innerTankWidthPx = 100f
    val innerTankHeightPx = 210f

    // Estos factores de escala se calculan en base al *viewport original del SVG* (200x250).
    // Si tu SVG `ic_tank_outline` tiene un `viewportWidth="200"` y `viewportHeight="250"`,
    // estos factores son los que te aseguran que el agua se posicione correctamente
    // DENTRO de ese contorno, sin importar el tamaño final del Composable.
    val viewportWidthSvg = 200f // Ancho del viewport de tu SVG
    val viewportHeightSvg = 250f // Alto del viewport de tu SVG

    val widthScaleFactor = innerTankWidthPx / viewportWidthSvg
    val heightScaleFactor = innerTankHeightPx / viewportHeightSvg

    // Ahora calculamos las dimensiones en Dp usando las proporciones FLOAT y el NUEVO container size
    val waterAreaWidthDp = containerWidthDp * widthScaleFactor
    val waterAreaHeightDp = containerHeightDp * heightScaleFactor

    val currentWaterLevelRatio = (nivelAgua / 100f).coerceIn(0f, 1f)
    val actualWaterHeightDp = waterAreaHeightDp * currentWaterLevelRatio

    Box(
        modifier = Modifier.size(width = containerWidthDp, height = containerHeightDp)
    ) {
        // --- EL AGUA (Box con color de fondo) ---
        val waterAreaTopInContainerDp = containerHeightDp * (innerTankTopPx / viewportHeightSvg)
        val waterTopYPositionInContainerDp = waterAreaTopInContainerDp + (waterAreaHeightDp - actualWaterHeightDp)

        Box(
            modifier = Modifier
                .offset(
                    x = containerWidthDp * (innerTankLeftPx / viewportWidthSvg),
                    y = waterTopYPositionInContainerDp
                )
                .width(waterAreaWidthDp)
                .height(actualWaterHeightDp)
                .background(
                    color = Color(0xFF2196F3).copy(alpha = 0.5f), // Color del agua
                    shape = RoundedCornerShape(0.dp)
                )
        )

        // --- El CONTORNO del Tanque (Vector Asset) ---
        Image(
            painter = painterResource(id = R.drawable.ic_tank_outline), // ¡Confirma que este ID sea correcto!
            contentDescription = "Contorno del tanque",
            modifier = Modifier.fillMaxSize()
        )

        // --- El TEXTO del Porcentaje ---
        Text(
            text = "${nivelAgua.toInt()}%",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black, // Podrías usar MaterialTheme.colorScheme.onSurface para mejor coherencia
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
