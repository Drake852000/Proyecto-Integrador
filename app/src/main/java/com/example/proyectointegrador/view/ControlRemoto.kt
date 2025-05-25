package com.example.proyectointegrador.view

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlRemoto() {
    var bombaActiva by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Control Remoto") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bomba de agua")
            Switch(
                checked = bombaActiva,
                onCheckedChange = { bombaActiva = it }
            )
            Text(if (bombaActiva) "Encendida" else "Apagada")
        }
    }
}
