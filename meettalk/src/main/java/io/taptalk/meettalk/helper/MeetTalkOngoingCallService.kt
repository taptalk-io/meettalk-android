package io.taptalk.meettalk.helper

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import io.taptalk.TapTalk.Const.TAPDefaultConstant
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.TapTalk.Listener.TapCommonListener
import io.taptalk.meettalk.BuildConfig
import io.taptalk.meettalk.R
import io.taptalk.meettalk.activity.MeetTalkCallActivity
import io.taptalk.meettalk.constant.MeetTalkConstant
import io.taptalk.meettalk.constant.MeetTalkConstant.IncomingCallNotification.INCOMING_CALL_NOTIFICATION_CHANNEL_ID
import io.taptalk.meettalk.constant.MeetTalkConstant.RequestCode.PENDING_INTENT_ONGOING_CALL_NOTIFICATION
import io.taptalk.meettalk.manager.MeetTalkCallManager
import java.util.*
import kotlin.system.exitProcess

/**
 * MeetTalkOngoingCallService
 * • Shows ongoing call notification when started
 * • Handles sending call notification when onTaskRemoved() is called
 */

class MeetTalkOngoingCallService : Service() {

    companion object {
        var instanceKey = ""
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Generate ongoing call notification
        val context = MeetTalk.appContext
        val instanceKey = MeetTalkCallManager.activeCallInstanceKey ?: ""
        val activeCallMessage = MeetTalkCallManager.activeCallMessage

        // Set notification title and content
        val notificationTitle = TapTalk.getClientAppName(instanceKey)
        val notificationContent = context.getString(R.string.meettalk_ongoing_call_notification_description)

        // Set notification and action button intent
        val notificationIntent = Intent(this, MeetTalkCallActivity::class.java)
        notificationIntent.putExtra(TAPDefaultConstant.Extras.INSTANCE_KEY, instanceKey)
        notificationIntent.putExtra(TAPDefaultConstant.Extras.MESSAGE, activeCallMessage)
        notificationIntent.putExtra(MeetTalkConstant.Extra.INCOMING_CALL_NOTIFICATION_TITLE, notificationTitle)
        notificationIntent.putExtra(MeetTalkConstant.Extra.INCOMING_CALL_NOTIFICATION_CONTENT, notificationContent)

        val notificationPendingIntent = PendingIntent.getActivity(
            this,
            PENDING_INTENT_ONGOING_CALL_NOTIFICATION,
            notificationIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notificationBuilder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = NotificationCompat.Builder(
                context,
                INCOMING_CALL_NOTIFICATION_CHANNEL_ID
            )
            notificationBuilder.setFullScreenIntent(notificationPendingIntent, true)
            notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        }
        else {
            notificationBuilder = NotificationCompat.Builder(context)
            notificationBuilder.setContentIntent(notificationPendingIntent)
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
        notificationBuilder.setSilent(true)
        notificationBuilder.priority = NotificationCompat.PRIORITY_MIN

        val notification = notificationBuilder.build()
        notification.flags = Notification.FLAG_INSISTENT

        // Show notification
        startForeground(1124, notification)

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        handleAppExiting()
    }

    private fun handleAppExiting() {
        if (MeetTalk.appContext != null) {
            MeetTalkCallManager.closeIncomingCallNotification(MeetTalk.appContext)
        }
        if (MeetTalkCallManager.pendingCallNotificationMessages.isNotEmpty() ||
            (MeetTalkCallManager.activeCallMessage != null &&
            MeetTalkCallManager.activeConferenceInfo != null &&
            MeetTalkCallManager.activeConferenceInfo!!.callEndedTime == 0L)
        ) {
            /**
             * Note: if incoming call / call activity was running, onDestroy() will be called
             * before onTaskRemoved() and active conference data will be removed, send notification
             * will be executed from activity instead.
             */
            if (MeetTalkCallManager.callState == MeetTalkCallManager.Companion.CallState.RINGING) {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>>", "handleAppExiting: sendRejectedCallNotification")
                }
                MeetTalkCallManager.sendRejectedCallNotification(
                    instanceKey,
                    MeetTalkCallManager.activeCallMessage!!.room
                )
            }
            else {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>>", "handleAppExiting: handleSendNotificationOnLeavingConference")
                }
                MeetTalkCallManager.handleSendNotificationOnLeavingConference()
            }
        }
        TapTalk.connect(instanceKey, object : TapCommonListener() {
            override fun onSuccess(successMessage: String?) {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>>", "handleAppExiting: connect success, send notification")
                }
                handleDisconnectAndExit(instanceKey, true)
            }

            override fun onError(errorCode: String?, errorMessage: String?) {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>>", "handleAppExiting: connect error $errorMessage")
                }
                handleDisconnectAndExit(instanceKey, true)
            }
        })
    }

    private fun handleDisconnectAndExit(instanceKey: String, shouldExit: Boolean) {
        Timer("Exit", false).schedule(object : TimerTask() {
            override fun run() {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>>", "handleAppExiting: timer countdown finished, disconnect and exit")
                }
                TapTalk.disconnect(instanceKey)
                stopSelf()
                if (shouldExit) {
                    exitProcess(0)
                }
            }
        }, 3000L)
    }
}
