package com.example.proyectointegrador.view

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.proyectointegrador.model.SystemState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlRemoto() {
    var state by remember { mutableStateOf(SystemState()) }

    LaunchedEffect(Unit) {
        observeSystemState { newState ->
            state = newState
        }
    }

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
                checked = state.state,
                onCheckedChange = { newValue ->
                    // Update Firebase when the switch is toggled
                    updateSystemState(newValue)
                }
            )
            Text(if (state.state) "Encendida" else "Apagada")
        }
    }
}
