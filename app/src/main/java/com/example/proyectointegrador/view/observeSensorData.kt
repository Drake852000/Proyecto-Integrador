package com.example.proyectointegrador.view

import android.util.Log
import com.example.proyectointegrador.model.SensorData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

fun observeSensorData(onDataChange: (SensorData) -> Unit) {
    val nivelRef = FirebaseDatabase.getInstance().getReference("nivel_agua")
    val flujoRef = FirebaseDatabase.getInstance().getReference("flujo_agua")

    nivelRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val nivel = snapshot.getValue(Int::class.java) ?: 0
            Log.d("Firebase", "Nivel de agua: $nivel")

            val flujo = snapshot.getValue(Float::class.java)
            Log.d("Firebase", "Flujo de agua: $flujo")
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Error (nivel_agua): ${error.message}")
        }
    })
}


