package io.taptalk.meettalk.helper

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.meettalk.R
import io.taptalk.meettalk.activity.MeetTalkIncomingCallActivity
import io.taptalk.meettalk.constant.MeetTalkConstant
import io.taptalk.meettalk.constant.MeetTalkConstant.Broadcast.ANSWER_INCOMING_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.Broadcast.REJECT_INCOMING_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.IncomingCallNotification.INCOMING_CALL_NOTIFICATION_CHANNEL_DESCRIPTION
import io.taptalk.meettalk.constant.MeetTalkConstant.IncomingCallNotification.INCOMING_CALL_NOTIFICATION_CHANNEL_ID
import io.taptalk.meettalk.constant.MeetTalkConstant.IncomingCallNotification.INCOMING_CALL_NOTIFICATION_CHANNEL_NAME
import io.taptalk.meettalk.manager.MeetTalkCallManager

class MeetTalkIncomingCallService : Service() {

    private val binder = Binder()

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.e(">>>>>>>", "onStartCommand: ")

        val incomingCallNotificationView = RemoteViews(packageName, R.layout.meettalk_notification_incoming_call)

        val notificationIntent = Intent(this, MeetTalkIncomingCallActivity::class.java)
//        val answerIntent = Intent(this, MeetTalkCallActivity::class.java)
        val answerIntent = Intent(ANSWER_INCOMING_CALL)
        val rejectIntent = Intent(REJECT_INCOMING_CALL)
        // TODO: BROADCAST INTENT NOT SENT

        val notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val answerPendingIntent = PendingIntent.getBroadcast(this, 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val rejectPendingIntent = PendingIntent.getBroadcast(this, 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        incomingCallNotificationView.setOnClickPendingIntent(R.id.notification_container, notificationPendingIntent)
        incomingCallNotificationView.setOnClickPendingIntent(R.id.iv_button_answer_call, answerPendingIntent)
        incomingCallNotificationView.setOnClickPendingIntent(R.id.iv_button_reject_call, rejectPendingIntent)

        val context = MeetTalk.appContext
        val instanceKey = MeetTalkCallManager.activeCallInstanceKey ?: ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationChannel = NotificationChannel(
                INCOMING_CALL_NOTIFICATION_CHANNEL_ID,
                INCOMING_CALL_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            // TODO: CUSTOM RINGTONE URI
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            notificationChannel.setSound(
                ringtoneUri,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            notificationChannel.description = INCOMING_CALL_NOTIFICATION_CHANNEL_DESCRIPTION
            notificationChannel.setShowBadge(true)
            notificationChannel.enableLights(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationChannel.lightColor = context.getColor(R.color.tapColorPrimary)

            notificationManager.createNotificationChannel(notificationChannel)

            val notification = NotificationCompat.Builder(
                context,
                INCOMING_CALL_NOTIFICATION_CHANNEL_ID
            )
            notification.setContentTitle(TapTalk.getClientAppName(instanceKey))
            notification.setContentText("CONTENT TEXT TEST")
            notification.setTicker("TEST TICKER")
            notification.setSmallIcon(TapTalk.getClientAppIcon(instanceKey))
            notification.setLargeIcon(BitmapFactory.decodeResource(this.resources, TapTalk.getClientAppIcon(instanceKey)))
            notification.setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            notification.setCategory(NotificationCompat.CATEGORY_CALL)
            notification.setOngoing(true)
            notification.setFullScreenIntent(notificationPendingIntent, true)
            notification.priority = NotificationCompat.PRIORITY_MAX
            notification.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            notification.setCustomContentView(incomingCallNotificationView)
            notification.setCustomBigContentView(incomingCallNotificationView)

            startForeground(1124, notification.build())

            Log.e(">>>>>>>", "onStartCommand: O+")
        }
        else {
            val notification = NotificationCompat.Builder(this)
            notification.setContentTitle(TapTalk.getClientAppName(instanceKey))
            notification.setContentText("CONTENT TEXT TEST")
            notification.setTicker("TEST TICKER")
            notification.setSmallIcon(TapTalk.getClientAppIcon(instanceKey))
            notification.setLargeIcon(BitmapFactory.decodeResource(this.resources, TapTalk.getClientAppIcon(instanceKey)))
            notification.setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            notification.setContentIntent(notificationPendingIntent)
            notification.setOngoing(true)
            notification.setCategory(NotificationCompat.CATEGORY_CALL)
            notification.priority = NotificationCompat.PRIORITY_MAX
//            val rejectCallAction = NotificationCompat.Action.Builder(android.R.drawable.sym_action_chat, "HANG UP", rejectPendingIntent)
//                .build()
//            notification.addAction(rejectCallAction)
            startForeground(1124, notification.build())

            Log.e(">>>>>>>", "onStartCommand: < O")
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
