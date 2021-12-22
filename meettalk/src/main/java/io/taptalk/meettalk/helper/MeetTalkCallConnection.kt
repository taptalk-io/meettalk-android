package io.taptalk.meettalk.helper

import android.os.Build
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log
import androidx.annotation.RequiresApi
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
        Log.e(">>>> TapCallConnection", "onCallAudioStateChanged:" + state?.toString())
    }

    override fun onDisconnect() {
        super.onDisconnect()
        setDisconnected(DisconnectCause(DisconnectCause.MISSED))
        MeetTalkCallManager.clearPendingIncomingCall()
        Log.e(">>>> TapCallConnection","onDisconnect")
    }

    override fun onAnswer() {
        super.onAnswer()
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        MeetTalkCallManager.joinPendingIncomingConferenceCall()
        Log.e(">>>> TapCallConnection","onAnswer:")
    }

    override fun onReject() {
        super.onReject()
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
        MeetTalkCallManager.rejectPendingIncomingConferenceCall()
        Log.e(">>>> TapCallConnection", "onReject: " )
    }

//    override fun onShowIncomingCallUi() {
//        super.onShowIncomingCallUi()
//    }
}