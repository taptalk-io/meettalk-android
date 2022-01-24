package io.taptalk.meettalk.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.taptalk.meettalk.R
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_ANSWERED
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_REJECTED
import io.taptalk.meettalk.manager.MeetTalkCallManager
import kotlinx.android.synthetic.main.meettalk_activity_incoming_call.*

class MeetTalkIncomingCallActivity : AppCompatActivity() {

    private var isNotificationSent = false
    private var receivedCallCancelledNotification = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.meettalk_activity_incoming_call)

        MeetTalkCallManager.closeIncomingCallNotification(this)
        MeetTalkCallManager.activeMeetTalkIncomingCallActivity = this

        Log.e(">>>>", "MeetTalkIncomingCallActivity onCreate: INCOMING_CALL_ANSWERED ${intent.getBooleanExtra(INCOMING_CALL_ANSWERED, false)}")
        Log.e(">>>>", "MeetTalkIncomingCallActivity onCreate: INCOMING_CALL_REJECTED ${intent.getBooleanExtra(INCOMING_CALL_REJECTED, false)}")
        if (intent.getBooleanExtra(INCOMING_CALL_ANSWERED, false)) {
            Log.e(">>>>", "MeetTalkIncomingCallActivity onCreate: answerCall")
            answerCall()
        }
        else if (intent.getBooleanExtra(INCOMING_CALL_REJECTED, false)) {
            Log.e(">>>>", "MeetTalkIncomingCallActivity onCreate: rejectCall")
            rejectCall()
        }

        iv_button_answer_call.setOnClickListener { answerCall() }
        iv_button_reject_call.setOnClickListener { rejectCall() }
    }

    override fun onDestroy() {
        MeetTalkCallManager.activeMeetTalkIncomingCallActivity = null
        super.onDestroy()
    }

    override fun finish() {
        if (!isNotificationSent && !receivedCallCancelledNotification) {
            Log.e(">>>>", "MeetTalkIncomingCallActivity finish: rejectCall $isNotificationSent $receivedCallCancelledNotification")
            rejectCall()
        }
        else {
            Log.e(">>>>", "MeetTalkIncomingCallActivity finish")
            super.finish()
        }
    }

    private fun answerCall() {
        MeetTalkCallManager.answerIncomingCall()
        isNotificationSent = true
        finish()
    }

    private fun rejectCall() {
        // Trigger listener callback
        MeetTalkCallManager.rejectIncomingCall()
        isNotificationSent = true
        finish()
    }

    fun closeIncomingCall() {
        receivedCallCancelledNotification = true
        finish()
    }
}
