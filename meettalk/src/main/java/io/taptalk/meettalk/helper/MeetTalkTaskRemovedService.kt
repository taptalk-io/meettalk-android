package io.taptalk.meettalk.helper

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.TapTalk.Listener.TapCommonListener
import io.taptalk.meettalk.BuildConfig
import io.taptalk.meettalk.manager.MeetTalkCallManager
import java.util.*
import kotlin.system.exitProcess

class MeetTalkTaskRemovedService : Service() {

    companion object {
        var instanceKey = ""
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
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
