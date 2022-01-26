package io.taptalk.meettalk.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.INSTANCE_KEY
import io.taptalk.meettalk.R
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_ANSWERED
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_REJECTED
import io.taptalk.meettalk.helper.MeetTalk
import io.taptalk.meettalk.manager.MeetTalkCallManager
import kotlinx.android.synthetic.main.meettalk_activity_incoming_call.*

class MeetTalkIncomingCallActivity : AppCompatActivity() {

    private lateinit var instanceKey: String

    private var isNotificationSent = false
    private var shouldNotSendRejectCallNotification = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.meettalk_activity_incoming_call)

        MeetTalkCallManager.activeMeetTalkIncomingCallActivity = this

        handleIntent(intent)

        iv_button_answer_call.setOnClickListener { answerCall() }
        iv_button_reject_call.setOnClickListener { rejectCall() }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onDestroy() {
        MeetTalkCallManager.closeIncomingCallNotification(this)
        MeetTalkCallManager.clearPendingIncomingCall()
        MeetTalkCallManager.activeMeetTalkIncomingCallActivity = null

        // Trigger listener callback
        for (meetTalkListener in MeetTalk.getMeetTalkListeners(MeetTalkCallManager.activeCallInstanceKey)) {
            meetTalkListener.onIncomingCallDisconnected()
        }

        super.onDestroy()
    }

    override fun finish() {
        if (isFinishing) {
            return
        }
        if (!isNotificationSent && !shouldNotSendRejectCallNotification) {
            rejectCall()
        }
        else {
            super.finish()
        }
    }

    private fun handleIntent(intent: Intent?) {
        instanceKey = intent?.getStringExtra(INSTANCE_KEY) ?: ""

        if (intent?.getBooleanExtra(INCOMING_CALL_ANSWERED, false) == true) {
            answerCall()
        }
        else if (intent?.getBooleanExtra(INCOMING_CALL_REJECTED, false) == true) {
            rejectCall()
        }
    }

    private fun answerCall() {
        MeetTalkCallManager.answerIncomingCall()
        isNotificationSent = true
        finish()
    }

    private fun rejectCall() {
        MeetTalkCallManager.rejectIncomingCall()
        isNotificationSent = true
        finish()
    }

    fun closeIncomingCall() {
        shouldNotSendRejectCallNotification = true
        finish()
    }
}
