package com.example.proyectointegrador.view

import android.util.Log
import com.example.proyectointegrador.model.EstadoBoton
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

fun observarEstado(onDataChange: (EstadoBoton) -> Unit) {
    val estadoRef = FirebaseDatabase.getInstance().getReference("estado_boton")

    estadoRef.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val estado = snapshot.getValue(EstadoBoton::class.java)
            if (estado != null) {
                onDataChange(estado)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Error (estado_boton): ${error.message}")
        }
    })
}