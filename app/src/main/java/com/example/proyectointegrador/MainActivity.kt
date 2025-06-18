package com.example.proyectointegrador

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import com.example.proyectointegrador.ui.theme.ProyectoIntegradorTheme
import com.example.proyectointegrador.view.MonitoreoTanques
import com.example.proyectointegrador.view.SistemaFiltrado
import com.example.proyectointegrador.view.ControlRemoto
import com.example.proyectointegrador.view.MantenimientoView
import com.example.proyectointegrador.view.ConfiguracionView
import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.BeyondBoundsLayout
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.unit.LayoutDirection

class MainActivity : ComponentActivity() {

    // Lanzador para pedir permiso en runtime
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("Permisos", "Permiso de notificaciones concedido")
        } else {
            Log.d("Permisos", "Permiso de notificaciones DENEGADO")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()

        // Pedir permiso runtime solo si API >= 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            ProyectoIntegradorTheme {
                MainScreen()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "tank_channel_id", // Este ID debe coincidir
                "Alerta de Tanque",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para niveles bajos del tanque"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf("tanques", "filtrado", "control", "mantenimiento", "config")

    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorOnPrimary = MaterialTheme.colorScheme.onPrimary
    val colorOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val colorSurface = MaterialTheme.colorScheme.surface
    val colorOnSurface = MaterialTheme.colorScheme.onSurface
    val colorBackground = MaterialTheme.colorScheme.background

    val extraBottomPadding = 60.dp // Puedes ajustar este valor si necesitas más/menos espacio

    Scaffold(
        bottomBar = {
            // <--- ¡CAMBIO CLAVE AQUÍ! Se envuelve NavigationBar en un Surface
            Surface(
                color = colorSurface, // Usa el color de superficie de tu tema
                shadowElevation = 8.dp, // Una sombra sutil para "levantarla"
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) // Esquinas superiores redondeadas
            ) {
                NavigationBar(
                    containerColor = Color.Transparent, // Fondo transparente para que el Surface maneje el color
                    tonalElevation = 0.dp // No elevación tonal en NavigationBar, el Surface lo gestiona
                ) {
                    items.forEach { screen ->
                        val selected = navController.currentBackStackEntryAsState().value?.destination?.route == screen
                        NavigationBarItem(
                            icon = {
                                val iconSize = if (selected) 28.dp else 24.dp
                                when (screen) {
                                    "tanques" -> Icon(Icons.Default.Water, contentDescription = null, modifier = Modifier.size(iconSize))
                                    "filtrado" -> Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(iconSize))
                                    "control" -> Icon(Icons.Default.Power, contentDescription = null, modifier = Modifier.size(iconSize))
                                    "mantenimiento" -> Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(iconSize))
                                    "config" -> Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(iconSize))
                                }
                            },
                            label = {
                                Text(
                                    text = screen.replaceFirstChar { it.uppercaseChar() },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            selected = selected,
                            onClick = {
                                if (navController.currentBackStackEntry?.destination?.route != screen) {
                                    navController.navigate(screen) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colorOnPrimary,
                                selectedTextColor = colorPrimary,
                                unselectedIconColor = colorOnSurfaceVariant,
                                unselectedTextColor = colorOnSurfaceVariant,
                                indicatorColor = colorPrimary
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "tanques",
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                // Asegúrate de que el padding inferior sea suficiente para tu NavigationBar + extra
                // El `extraBottomPadding` es el espacio adicional que quieres *debajo* del contenido del NavHost,
                // que la NavigationBar y su posible altura extra (si la defines) llenarán.
                // Si la NavigationBar crece en altura, el paddingValues.calculateBottomPadding() crecerá automáticamente.
                // El `extraBottomPadding` es para ese espacio *adicional* debajo de la barra normal.
                bottom = paddingValues.calculateBottomPadding() + extraBottomPadding,
                start = paddingValues.calculateStartPadding(layoutDirection = LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(layoutDirection = LayoutDirection.Ltr)
            )
        ) {
            composable("tanques") { MonitoreoTanques() }
            composable("filtrado") { SistemaFiltrado() }
            composable("control") { ControlRemoto() }
            composable("mantenimiento") { MantenimientoView() }
            composable("config") { ConfiguracionView() }
        }
    }
}