package com.example.proyectointegrador.view

import android.util.Log
import com.example.proyectointegrador.model.SensorData
import com.example.proyectointegrador.model.SystemState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

fun observeSensorData(onDataChange: (SensorData) -> Unit) {
    val nivelRef = FirebaseDatabase.getInstance().getReference("sensor_data/nivel_agua")
    val flujoRef = FirebaseDatabase.getInstance().getReference("sensor_data/flujo_agua")

    // Variable para mantener ambos valores
    var currentData = SensorData()

    nivelRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            currentData = currentData.copy(
                nivelAgua = snapshot.getValue(Int::class.java) ?: 0
            )
            Log.d("Firebase", "Nivel de agua: ${currentData.nivelAgua}")
            onDataChange(currentData)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Error (nivel_agua): ${error.message}")
        }
    })

    flujoRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            currentData = currentData.copy(
                flujoAgua = snapshot.getValue(Float::class.java) ?: 0f
            )
            Log.d("Firebase", "Flujo de agua: ${currentData.flujoAgua}")
            onDataChange(currentData)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Error (flujo_agua): ${error.message}")
        }
    })
}

fun observeSystemState(onDataChange: (SystemState) -> Unit) {
    val stateRef = FirebaseDatabase.getInstance().getReference("sistema/estado")
    var currentState = SystemState()

    stateRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            currentState = currentState.copy(
                state = snapshot.getValue(Boolean::class.java) ?: false
            )
            onDataChange(currentState)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Error (estado): ${error.message}")
        }
    })
}

fun updateSystemState(newState: Boolean) {
    val stateRef = FirebaseDatabase.getInstance().getReference("sistema/estado")
    stateRef.setValue(newState)
        .addOnSuccessListener {
            Log.d("Firebase", "Estado actualizado correctamente a: $newState")
        }
        .addOnFailureListener { e ->
            Log.e("Firebase", "Error al actualizar estado: ${e.message}")
        }
}

