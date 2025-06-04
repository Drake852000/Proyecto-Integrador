package com.example.proyectointegrador.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.proyectointegrador.R
import android.Manifest

fun showTankLowNotification(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Notificación", "Permiso de notificaciones denegado")
            return
        }
    }

    val builder = NotificationCompat.Builder(context, "tank_channel_id")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Nivel bajo del tanque")
        .setContentText("El tanque está al 15%. Por favor, rellénalo.")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    with(NotificationManagerCompat.from(context)) {
        notify(1001, builder.build())
    }

    Log.d("Notificación", "✅ Notificación enviada correctamente")
}
