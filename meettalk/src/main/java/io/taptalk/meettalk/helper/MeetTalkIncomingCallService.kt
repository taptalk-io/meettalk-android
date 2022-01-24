package io.taptalk.meettalk.helper

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.meettalk.R
import io.taptalk.meettalk.activity.MeetTalkIncomingCallActivity
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_ANSWERED
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_NOTIFICATION_CONTENT
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_NOTIFICATION_TITLE
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_REJECTED
import io.taptalk.meettalk.constant.MeetTalkConstant.IncomingCallNotification.INCOMING_CALL_NOTIFICATION_CHANNEL_ID
import io.taptalk.meettalk.manager.MeetTalkCallManager

class MeetTalkIncomingCallService : Service() {

    private val binder = Binder()

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(">>>>>>>", "onStartCommand: ")
        val context = MeetTalk.appContext
        val instanceKey = MeetTalkCallManager.activeCallInstanceKey ?: ""

        // Set notification title and content
        val notificationTitle = intent?.getStringExtra(INCOMING_CALL_NOTIFICATION_TITLE) ?: context.getString(R.string.meettalk_incoming_call)
        val notificationContent = intent?.getStringExtra(INCOMING_CALL_NOTIFICATION_CONTENT) ?: TapTalk.getClientAppName(instanceKey)

        val incomingCallNotificationView = RemoteViews(packageName, R.layout.meettalk_notification_incoming_call)

        incomingCallNotificationView.setTextViewText(R.id.tv_notification_title, notificationTitle)
        incomingCallNotificationView.setTextViewText(R.id.tv_notification_content, notificationContent)

        // Set container and button intent
        val notificationIntent = Intent(this, MeetTalkIncomingCallActivity::class.java)
        notificationIntent.putExtra(INCOMING_CALL_ANSWERED, false)
        notificationIntent.putExtra(INCOMING_CALL_REJECTED, false)
        val answerIntent = Intent(this, MeetTalkIncomingCallActivity::class.java)
        answerIntent.putExtra(INCOMING_CALL_ANSWERED, true)
        answerIntent.putExtra(INCOMING_CALL_REJECTED, false)
        val rejectIntent = Intent(this, MeetTalkIncomingCallActivity::class.java)
        rejectIntent.putExtra(INCOMING_CALL_ANSWERED, false)
        rejectIntent.putExtra(INCOMING_CALL_REJECTED, true)

        val notificationPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val answerPendingIntent = PendingIntent.getActivity(this, 1, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val rejectPendingIntent = PendingIntent.getActivity(this, 2, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        incomingCallNotificationView.setOnClickPendingIntent(R.id.iv_button_answer_call, answerPendingIntent)
        incomingCallNotificationView.setOnClickPendingIntent(R.id.iv_button_reject_call, rejectPendingIntent)

        // Build notification
        val notification: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = NotificationCompat.Builder(
                context,
                INCOMING_CALL_NOTIFICATION_CHANNEL_ID
            )
            notification.setFullScreenIntent(notificationPendingIntent, true)
            notification.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            notification.setCustomContentView(incomingCallNotificationView)
            notification.setCustomBigContentView(incomingCallNotificationView)
        }
        else {
            notification = NotificationCompat.Builder(context)
            notification.setContentIntent(notificationPendingIntent)
            val answerCallAction = NotificationCompat.Action.Builder(
                android.R.drawable.sym_action_chat,
                context.getString(R.string.meettalk_answer),
                answerPendingIntent
            ).build()
            val rejectCallAction = NotificationCompat.Action.Builder(
                android.R.drawable.sym_action_chat,
                context.getString(R.string.meettalk_reject),
                rejectPendingIntent
            ).build()
            notification.addAction(answerCallAction)
            notification.addAction(rejectCallAction)
        }

        notification.setContentTitle(notificationTitle)
        notification.setContentText(notificationContent)
        notification.setTicker(notificationTitle)
        notification.setSmallIcon(TapTalk.getClientAppIcon(instanceKey))
        notification.setLargeIcon(BitmapFactory.decodeResource(this.resources, TapTalk.getClientAppIcon(instanceKey)))
        notification.setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
        notification.setCategory(NotificationCompat.CATEGORY_CALL)
        notification.setOngoing(true)
        notification.priority = NotificationCompat.PRIORITY_MAX

        startForeground(1124, notification.build())

        return super.onStartCommand(intent, flags, startId)
    }
}
