package com.example.labexam3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat

object BudgetNotificationHelper {

    private const val CHANNEL_ID = "budget_alert_channel"
    private const val CHANNEL_NAME = "Budget Alerts"
    private const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when budget limit is exceeded"
                enableVibration(true)
                setShowBadge(true)
                lightColor = Color.RED
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showBudgetExceededNotification(context: Context) {
        createChannel(context) // Ensure channel exists

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        // Check if notifications are enabled for our channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
                return // Notifications are disabled
            }
        }

        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Budget Limit Exceeded!")
            .setContentText("You've gone over your spending limit")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(notificationSound)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setLights(Color.RED, 3000, 3000)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}