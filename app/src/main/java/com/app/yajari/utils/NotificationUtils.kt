package com.app.yajari.utils
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.app.yajari.MainActivity
import com.app.yajari.R
import com.app.yajari.ui.announcement_details.AnnouncementDetailsActivity
import com.app.yajari.ui.chat_detail.ChatDetailActivity
import com.app.yajari.ui.my_announcement.MyAnnouncementActivity
import com.app.yajari.ui.notification.NotificationActivity
import com.app.yajari.ui.rate_review.RateReviewActivity
import java.util.*

object NotificationUtils {

    private const val GENERAL_CHANNEL_ID = "10001"
    private var mNotificationManager: NotificationManager? = null

    fun showGeneralNotification(
        context: Context,
        message: String,
        pushType: String,
        data: MutableMap<String, String>
    ) {

        val resultIntent: Intent?
        when (pushType) {
            "1" -> {
                resultIntent = Intent(context, AnnouncementDetailsActivity::class.java).apply {
                    action = Constant.CHAT
                    putExtra(Constant.FROM,Constant.PUSH)
                    putExtra(Constant.ANNOUNCEMENT_ID, data["object_id"].toString())
                    putExtra(Constant.PUSH_TYPE, data["push_type"].toString())
                    putExtra(Constant.NOTIFICATION_C, data["notification_count"].toString())
                    putExtra(Constant.MSG_C, data["message_count"].toString())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent)

            }
            "2" -> {
                resultIntent = Intent(context, AnnouncementDetailsActivity::class.java).apply {
                    action = Constant.CHAT
                    putExtra(Constant.FROM,Constant.PUSH)
                    putExtra(Constant.ANNOUNCEMENT_ID, data["object_id"].toString())
                    putExtra(Constant.PUSH_TYPE, data["push_type"].toString())
                    putExtra(Constant.NOTIFICATION_C, data["notification_count"].toString())
                    putExtra(Constant.MSG_C, data["message_count"].toString())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent)
            }
            "3" -> {
                resultIntent = Intent(context, ChatDetailActivity::class.java).apply {
                    action =Constant.CHAT
                    putExtra(Constant.FROM,Constant.PUSH)
                    putExtra(Constant.THREAD_ID, data["object_id"].toString())
                    putExtra(Constant.PUSH_TYPE, data["push_type"].toString())
                    putExtra(Constant.NOTIFICATION_C, data["notification_count"].toString())
                    putExtra(Constant.MSG_C, data["message_count"].toString())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent)
            }
            "4" -> {
                resultIntent = Intent(context, AnnouncementDetailsActivity::class.java).apply {
                    action =Constant.CHAT
                    putExtra(Constant.FROM,Constant.PUSH)
                    putExtra(Constant.ANNOUNCEMENT_ID, data["object_id"].toString())
                    putExtra(Constant.PUSH_TYPE, data["push_type"].toString())
                    putExtra(Constant.NOTIFICATION_C, data["notification_count"].toString())
                    putExtra(Constant.MSG_C, data["message_count"].toString())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent)
            }
            "5" ->{
                resultIntent = Intent(context, MainActivity::class.java).apply {
                    action =Constant.CHAT
                    putExtra(Constant.FROM,Constant.PUSH)
                    putExtra(Constant.THREAD_ID, data["object_id"].toString())
                    putExtra(Constant.PUSH_TYPE, data["push_type"].toString())
                    putExtra(Constant.NOTIFICATION_C, data["notification_count"].toString())
                    putExtra(Constant.MSG_C, data["message_count"].toString())

                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent)
            }

            "6" ->{
                resultIntent = Intent(context, RateReviewActivity::class.java).apply {
                    action =Constant.CHAT
                    putExtra(Constant.FROM,Constant.PUSH)
                    putExtra(Constant.PUSH_TYPE, data["push_type"].toString())
                    putExtra(Constant.NOTIFICATION_C, data["notification_count"].toString())
                    putExtra(Constant.MSG_C, data["message_count"].toString())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent)
            }
            "7" -> {
                resultIntent = Intent(context, AnnouncementDetailsActivity::class.java).apply {
                    action =Constant.CHAT
                    putExtra(Constant.FROM,Constant.PUSH)
                    putExtra(Constant.ANNOUNCEMENT_ID, data["object_id"].toString())
                    putExtra(Constant.PUSH_TYPE, data["push_type"].toString())
                    putExtra(Constant.NOTIFICATION_C, data["notification_count"].toString())
                    putExtra(Constant.MSG_C, data["message_count"].toString())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent)
            }
            "8" -> {
                resultIntent = Intent(context, AnnouncementDetailsActivity::class.java).apply {
                    action =Constant.CHAT
                    putExtra(Constant.FROM,Constant.PUSH)
                    putExtra(Constant.ANNOUNCEMENT_ID, data["object_id"].toString())
                    putExtra(Constant.PUSH_TYPE, data["push_type"].toString())
                    putExtra(Constant.NOTIFICATION_C, data["notification_count"].toString())
                    putExtra(Constant.MSG_C, data["message_count"].toString())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent)
            }
            else -> {

                resultIntent = Intent(context, MainActivity::class.java).apply {
                    action = Constant.CHAT
                    putExtra(Constant.FROM,Constant.PUSH)
                    putExtra(Constant.OBJECT_ID, data["object_id"].toString())
                    putExtra(Constant.PUSH_TYPE, data["push_type"].toString())
                    putExtra(Constant.NOTIFICATION_C, data["notification_count"].toString())
                    putExtra(Constant.MSG_C, data["message_count"].toString())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent)
            }
        }


        sendNotification(context, message, resultIntent, data,pushType)
    }


    @SuppressLint("UnspecifiedImmutableFlag")
    private fun sendNotification(
        context: Context,
        message: String,
        resultIntent: Intent,
        data: MutableMap<String, String>,
        pushType: String
    ) {
        val notificationId = Random().nextInt()
        val resultPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, notificationId, resultIntent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(context, notificationId, resultIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        }
        if (pushType=="5" || pushType!="6" && Constant.IS_IN_CHAT)
        {
            return
        }
        mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel: NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            notificationChannel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                context.getString(R.string.app_name),
                importance
            )
            notificationChannel.setShowBadge(false)
            mNotificationManager!!.createNotificationChannel(notificationChannel)
        }

        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(context, defaultSoundUri)
        r.play()

        val mBuilder = NotificationCompat.Builder(context, GENERAL_CHANNEL_ID)
            .setSmallIcon(getNotificationIcon())
            .setContentTitle(HtmlCompat.fromHtml(data["push_title"]?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
                .toString(),HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setContentText(message)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setColor(ContextCompat.getColor(context, android.R.color.transparent))
            .setContentIntent(resultPendingIntent)
            .setSound(defaultSoundUri)


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mBuilder.setSmallIcon(R.mipmap.ic_launcher)
//        } else {
//            mBuilder.setSmallIcon(R.mipmap.ic_launcher)
//        }

        mBuilder.setLargeIcon(
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        )
        mNotificationManager!!.notify(System.currentTimeMillis().toInt(), mBuilder.build())
    }
    @SuppressLint("ObsoleteSdkInt")
    private fun getNotificationIcon(): Int {
        val useWhiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        return if (useWhiteIcon) R.mipmap.ic_launcher else R.mipmap.ic_launcher
    }

}
