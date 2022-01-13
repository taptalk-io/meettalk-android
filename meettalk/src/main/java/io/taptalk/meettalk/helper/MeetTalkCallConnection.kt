package io.taptalk.meettalk.helper

import android.os.Build
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log
import androidx.annotation.RequiresApi
import io.taptalk.meettalk.BuildConfig
import io.taptalk.meettalk.manager.MeetTalkCallManager

@RequiresApi(Build.VERSION_CODES.M)
class MeetTalkCallConnection : Connection() {

    companion object {
        private var instance: MeetTalkCallConnection? = null

        fun newInstance() : MeetTalkCallConnection {
            instance = MeetTalkCallConnection()
            return instance!!
        }

        fun getInstance() : MeetTalkCallConnection {
            if (null == instance) {
                instance = MeetTalkCallConnection()
            }
            return instance!!
        }
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectionProperties = PROPERTY_SELF_MANAGED
        }
        audioModeIsVoip = true
    }

    override fun onCallAudioStateChanged(state: CallAudioState?) {
        if (BuildConfig.DEBUG) {
            Log.e(">>>> TapCallConnection", "onCallAudioStateChanged:" + state?.toString())
        }
    }

    override fun onDisconnect() {
        super.onDisconnect()
        MeetTalkCallManager.clearPendingIncomingCall()

        // Trigger listener callback
        for (meetTalkListener in MeetTalk.getMeetTalkListeners(MeetTalkCallManager.activeCallInstanceKey)) {
            meetTalkListener.onIncomingCallDisconnected()
        }

        if (BuildConfig.DEBUG) {
            Log.e(">>>> TapCallConnection", "onDisconnect")
        }

        setDisconnected(DisconnectCause(DisconnectCause.MISSED))
    }

    override fun onAnswer() {
        super.onAnswer()

        // Trigger listener callback
        for (meetTalkListener in MeetTalk.getMeetTalkListeners(MeetTalkCallManager.activeCallInstanceKey)) {
            meetTalkListener.onIncomingCallAnswered()
        }

        if (BuildConfig.DEBUG) {
            Log.e(">>>> TapCallConnection", "onAnswer:")
        }

        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
    }

    override fun onReject() {
        super.onReject()

        // Trigger listener callback
        for (meetTalkListener in MeetTalk.getMeetTalkListeners(MeetTalkCallManager.activeCallInstanceKey)) {
            meetTalkListener.onIncomingCallRejected()
        }

        if (BuildConfig.DEBUG) {
            Log.e(">>>> TapCallConnection", "onReject: ")
        }

        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
    }

    override fun onShowIncomingCallUi() {
        super.onShowIncomingCallUi()
        if (BuildConfig.DEBUG) {
            Log.e(">>>> TapCallConnection", "onShowIncomingCallUi: ")
        }
    }
}
