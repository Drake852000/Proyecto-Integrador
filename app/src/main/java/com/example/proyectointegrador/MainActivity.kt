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

    // Colores del tema para la NavigationBar
    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val colorSurface = MaterialTheme.colorScheme.surface
    val colorOnSurface = MaterialTheme.colorScheme.onSurface
    val colorBackground = MaterialTheme.colorScheme.background


    Scaffold(
        bottomBar = {
            NavigationBar(
                // Fondo de la barra de navegación inferior
                containerColor = colorSurface,
                // Elevación sutil para que se "levante" del fondo
                tonalElevation = 8.dp
            ) {
                items.forEach { screen ->
                    val selected = navController.currentBackStackEntryAsState().value?.destination?.route == screen
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                "tanques" -> Icon(Icons.Default.Water, contentDescription = null, modifier = Modifier.padding(bottom = 2.dp))
                                "filtrado" -> Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.padding(bottom = 2.dp))
                                "control" -> Icon(Icons.Default.Power, contentDescription = null, modifier = Modifier.padding(bottom = 2.dp))
                                "mantenimiento" -> Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.padding(bottom = 2.dp))
                                "config" -> Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.padding(bottom = 2.dp))
                            }
                        },
                        label = {
                            Text(
                                text = screen.replaceFirstChar { it.uppercaseChar() },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        selected = selected,
                        onClick = {
                            if (navController.currentBackStackEntry?.destination?.route != screen) {
                                navController.navigate(screen) {
                                    // Evita múltiples instancias de la misma pantalla
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true // Guarda el estado de la pantalla actual si quieres volver a ella
                                    }
                                    // Asegura que solo una copia del destino dado esté en la pila
                                    launchSingleTop = true
                                    // Restaura el estado cuando se vuelve a seleccionar un elemento
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            // Color del icono y texto cuando el elemento está seleccionado
                            selectedIconColor = colorPrimary,
                            selectedTextColor = colorPrimary,
                            // Color del icono y texto cuando el elemento NO está seleccionado
                            unselectedIconColor = colorOnSurfaceVariant,
                            unselectedTextColor = colorOnSurfaceVariant,
                            // Color del indicador (la pastilla) cuando el elemento está seleccionado
                            indicatorColor = colorPrimary.copy(alpha = 0.1f) // Un color Primary translúcido
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "tanques",
            modifier = Modifier.padding(padding)
        ) {
            composable("tanques") { MonitoreoTanques() }
            composable("filtrado") { SistemaFiltrado() }
            composable("control") { ControlRemoto() }
            composable("mantenimiento") { MantenimientoView() }
            composable("config") { ConfiguracionView() }
        }
    }
}


