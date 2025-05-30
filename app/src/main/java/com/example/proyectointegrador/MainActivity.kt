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
                "tank_channel_id",
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

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                "tanques" -> Icon(Icons.Default.Water, contentDescription = null)
                                "filtrado" -> Icon(Icons.Default.FilterAlt, contentDescription = null)
                                "control" -> Icon(Icons.Default.Power, contentDescription = null)
                                "mantenimiento" -> Icon(Icons.Default.Build, contentDescription = null)
                                "config" -> Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        },
                        label = { Text(screen.replaceFirstChar { it.uppercaseChar() }) },
                        selected = navController.currentBackStackEntryAsState().value?.destination?.route == screen,
                        onClick = {
                            navController.navigate(screen) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
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
