package com.app.yajari.data.service

import android.util.Log
import com.app.yajari.utils.NotificationUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Locale

class YajariService : FirebaseMessagingService() {


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("Firebase Token $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        println("From: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            println("Notification body ${it.body}")
        }

        if (remoteMessage.data.isNotEmpty()) {
            Log.i("Notification:-- data", "" + remoteMessage.data)
            val message = remoteMessage.data["push_message"]?.capitalize(Locale.getDefault())
            val pushType = remoteMessage.data["push_type"]

            message?.let { pushMessage ->
                NotificationUtils.showGeneralNotification(
                    applicationContext,
                    pushMessage,
                    (pushType ?: ""),
                    remoteMessage.data
                )
            }
        }
    }
}