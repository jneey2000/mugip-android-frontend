package com.giftmusic.mugip.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.giftmusic.mugip.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d("Firebase Token", token)
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(remoteMessage.data.isNotEmpty()){
            sendNotification(remoteMessage)
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val channel = "Mugip Notification channel"
        val channelName = "Mugip Notification channel"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(channel, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationChannel.description = "Need for using notification"
        notificationChannel.enableLights(true)
        notificationChannel.enableVibration(true)
        notificationChannel.setShowBadge(false)
        notificationChannel.vibrationPattern = listOf<Long>(100, 200, 100, 200).toLongArray()

        val notificationBuilder = NotificationCompat.Builder(this, channel)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(remoteMessage.data["title"])
            .setContentText(remoteMessage.data["message"])
            .setChannelId(channel)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_SOUND)
        notificationManager.notify(9999, notificationBuilder.build())
    }
}