package com.ritwik.photex

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class Notifications() : FirebaseMessagingService() {

    private val CHANNEL_ID = "1024"
    var builder = NotificationCompat.Builder(this, CHANNEL_ID)
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
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(searchString: String?) {
        val manager = NotificationManagerCompat.from(this)
        manager.let {
            // notificationId is a unique int for each notification that you must define
            searchString?.let { string ->
                Log.d(TAG, "showNotification: $string")
                builder.setContentTitle("noyo")
                val intent = Intent(this, MainActivity::class.java)
                val bundle = Bundle()
                bundle.putString("TEMPLATE_STRING", string)
                intent.putExtras(bundle)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP and Intent.FLAG_ACTIVITY_SINGLE_TOP
                val pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                builder.setContentIntent(pendingIntent)
            }
            if (searchString == null) {
                builder.setContentTitle("yoyo")

            }


        }
        val notificationId = generateRandom()
        manager.notify(notificationId, builder.build())
    }

    fun generateRandom(): Int {
        val random = Random(5)
        return random.nextInt()

    }

    override fun onMessageReceived(p0: RemoteMessage) {
        val notificationManager: NotificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                createNotificationChannel()
            }
        }
        val title = p0.notification?.title ?: ""
        val message = p0.notification?.body ?: ""
        val icon = p0.notification?.icon
        var searchString: String? = null
        builder.setContentTitle(title)
        builder.setContentText(message)
        builder.setSmallIcon(R.drawable.notification_icon)
        Log.d(TAG, "onMessageReceived: icon = $icon")
        val data = p0.data
        data.let {
            Log.d(TAG, "onMessageReceived: data not null")
            if (it.containsKey("SEARCH_STRING")) {
                searchString = it.get("SEARCH_STRING")
            } else {
                Log.d(TAG, "onMessageReceived: does not contain search string")
            }
        }
        showNotification(searchString)


    }

    companion object {
        private const val TAG = "Notifications"
    }
}