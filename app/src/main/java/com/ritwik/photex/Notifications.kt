package com.ritwik.photex

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class Notifications(val context: Context) : FirebaseMessagingService() {

    private val CHANNEL_ID = "1024"

    var builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle("Test")
        .setContentText("Notification")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)


    fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Cloud"
            val descriptionText = "Notifications from the cloud"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification() {
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            val notificationId = generateRandom()
            notify(notificationId, builder.build())
        }
    }

    fun generateRandom(): Int {
        val random = Random(5)
        return random.nextInt()

    }
        override fun onMessageReceived(p0: RemoteMessage) {
            super.onMessageReceived(p0)
            val title = p0.notification?.title ?: ""
            val message = p0.notification?.body ?: ""
            val icon = p0.notification?.icon
            builder.setContentTitle(title)
            builder.setContentText(message)
            builder.setSmallIcon(R.drawable.notification_icon)
            Log.d(TAG, "onMessageReceived: icon = $icon")
            showNotification()

        }



        companion object {
            private const val TAG = "Notifications"
        }
    }