package io.taptalk.meettalk.helper

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.INSTANCE_KEY
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.MESSAGE
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.meettalk.R
import io.taptalk.meettalk.activity.MeetTalkIncomingCallActivity
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_ANSWERED
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_NOTIFICATION_CONTENT
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_NOTIFICATION_TITLE
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_REJECTED
import io.taptalk.meettalk.constant.MeetTalkConstant.IncomingCallNotification.INCOMING_CALL_NOTIFICATION_CHANNEL_ID
import io.taptalk.meettalk.constant.MeetTalkConstant.RequestCode.PENDING_INTENT_INCOMING_CALL_ANSWER
import io.taptalk.meettalk.constant.MeetTalkConstant.RequestCode.PENDING_INTENT_INCOMING_CALL_NOTIFICATION
import io.taptalk.meettalk.constant.MeetTalkConstant.RequestCode.PENDING_INTENT_INCOMING_CALL_REJECT
import io.taptalk.meettalk.manager.MeetTalkCallManager
import android.os.PowerManager
import org.jitsi.meet.sdk.BuildConfig

class MeetTalkIncomingCallService : Service() {

    private val binder = Binder()

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (BuildConfig.DEBUG) {
            Log.e(">>>>>>>", "onStartCommand: ")
        }
        val context = MeetTalk.appContext
        val instanceKey = MeetTalkCallManager.activeCallInstanceKey ?: ""
        val activeCallMessage = MeetTalkCallManager.activeCallMessage

        // Set notification title and content
        val notificationTitle = intent?.getStringExtra(INCOMING_CALL_NOTIFICATION_TITLE) ?: TapTalk.getClientAppName(instanceKey)
        val notificationContent = intent?.getStringExtra(INCOMING_CALL_NOTIFICATION_CONTENT) ?: context.getString(R.string.meettalk_incoming_call_ellipsis)

        val incomingCallNotificationView = RemoteViews(packageName, R.layout.meettalk_notification_incoming_call)

        incomingCallNotificationView.setTextViewText(R.id.tv_notification_title, notificationTitle)
        incomingCallNotificationView.setTextViewText(R.id.tv_notification_content, notificationContent)

        // Set notification and action button intent
        val notificationIntent = Intent(this, MeetTalkIncomingCallActivity::class.java)
        notificationIntent.putExtra(INSTANCE_KEY, instanceKey)
        notificationIntent.putExtra(MESSAGE, activeCallMessage)
        notificationIntent.putExtra(INCOMING_CALL_NOTIFICATION_TITLE, notificationTitle)
        notificationIntent.putExtra(INCOMING_CALL_NOTIFICATION_CONTENT, notificationContent)
        val answerIntent = Intent(this, MeetTalkIncomingCallActivity::class.java)
        answerIntent.putExtra(INSTANCE_KEY, instanceKey)
        answerIntent.putExtra(MESSAGE, activeCallMessage)
        answerIntent.putExtra(INCOMING_CALL_NOTIFICATION_TITLE, notificationTitle)
        answerIntent.putExtra(INCOMING_CALL_NOTIFICATION_CONTENT, notificationContent)
        answerIntent.putExtra(INCOMING_CALL_ANSWERED, true)
        val rejectIntent = Intent(this, MeetTalkIncomingCallActivity::class.java)
        rejectIntent.putExtra(INSTANCE_KEY, instanceKey)
        rejectIntent.putExtra(MESSAGE, activeCallMessage)
        rejectIntent.putExtra(INCOMING_CALL_NOTIFICATION_TITLE, notificationTitle)
        rejectIntent.putExtra(INCOMING_CALL_NOTIFICATION_CONTENT, notificationContent)
        rejectIntent.putExtra(INCOMING_CALL_REJECTED, true)

        val notificationPendingIntent = PendingIntent.getActivity(
            this,
            PENDING_INTENT_INCOMING_CALL_NOTIFICATION,
            notificationIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val answerPendingIntent = PendingIntent.getActivity(
            this,
            PENDING_INTENT_INCOMING_CALL_ANSWER,
            answerIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val rejectPendingIntent = PendingIntent.getActivity(
            this,
            PENDING_INTENT_INCOMING_CALL_REJECT,
            rejectIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        incomingCallNotificationView.setOnClickPendingIntent(R.id.iv_button_answer_call, answerPendingIntent)
        incomingCallNotificationView.setOnClickPendingIntent(R.id.iv_button_reject_call, rejectPendingIntent)

        // Build notification
        val notificationBuilder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = NotificationCompat.Builder(
                context,
                INCOMING_CALL_NOTIFICATION_CHANNEL_ID
            )
            notificationBuilder.setFullScreenIntent(notificationPendingIntent, true)
            notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            notificationBuilder.setCustomContentView(incomingCallNotificationView)
            notificationBuilder.setCustomBigContentView(incomingCallNotificationView)
        }
        else {
            notificationBuilder = NotificationCompat.Builder(context)
            notificationBuilder.setContentIntent(notificationPendingIntent)
            val answerActionString = HtmlCompat.fromHtml(
                "<font color=\"" +
                        ContextCompat.getColor(context, R.color.meetTalkIncomingCallAnswerButtonBackgroundColor) +
                        "\">" + context.getString(R.string.meettalk_answer) +
                        "</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            val rejectActionString = HtmlCompat.fromHtml(
                "<font color=\"" +
                        ContextCompat.getColor(context, R.color.meetTalkIncomingCallRejectButtonBackgroundColor) +
                        "\">" + context.getString(R.string.meettalk_reject) +
                        "</font>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            val answerCallAction = NotificationCompat.Action.Builder(
                R.drawable.meettalk_ic_answer_call_green,
                answerActionString,
                answerPendingIntent
            ).build()
            val rejectCallAction = NotificationCompat.Action.Builder(
                R.drawable.meettalk_ic_hang_up_red,
                rejectActionString,
                rejectPendingIntent
            ).build()
            notificationBuilder.addAction(answerCallAction)
            notificationBuilder.addAction(rejectCallAction)
        }

        notificationBuilder.setContentTitle(notificationTitle)
        notificationBuilder.setContentText(notificationContent)
        notificationBuilder.setTicker(notificationTitle)
        notificationBuilder.setSmallIcon(TapTalk.getClientAppIcon(instanceKey))
        notificationBuilder.setShowWhen(false)
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
        notificationBuilder.setCategory(NotificationCompat.CATEGORY_CALL)
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationBuilder.setOngoing(true)
        notificationBuilder.priority = NotificationCompat.PRIORITY_MAX

        val notification = notificationBuilder.build()
        notification.flags = Notification.FLAG_INSISTENT

        // Show notification
        startForeground(1124, notification)

        // Wake lock
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = powerManager.isInteractive
        if (!isScreenOn) {
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "myApp:notificationLock"
            )
            wakeLock.acquire(5000)
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
