package io.taptalk.meettalk.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.taptalk.meettalk.R
import io.taptalk.meettalk.helper.MeetTalk
import io.taptalk.meettalk.helper.MeetTalkIncomingCallService
import io.taptalk.meettalk.manager.MeetTalkCallManager
import kotlinx.android.synthetic.main.meettalk_activity_incoming_call.*

class MeetTalkIncomingCallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.meettalk_activity_incoming_call)

        iv_button_answer_call.setOnClickListener {
            Log.e(">>>>", "MeetTalkIncomingCallActivity: iv_button_answer_call")
            // Trigger listener callback
            for (meetTalkListener in MeetTalk.getMeetTalkListeners(MeetTalkCallManager.activeCallInstanceKey)) {
                meetTalkListener.onIncomingCallAnswered()
            }
            MeetTalkCallManager.clearPendingIncomingCall()
            finish()
        }

        iv_button_reject_call.setOnClickListener {
            Log.e(">>>>", "MeetTalkIncomingCallActivity: iv_button_reject_call")
            // Trigger listener callback
            for (meetTalkListener in MeetTalk.getMeetTalkListeners(MeetTalkCallManager.activeCallInstanceKey)) {
                meetTalkListener.onIncomingCallRejected()
            }
            MeetTalkCallManager.clearPendingIncomingCall()
            finish()
        }
    }

    override fun finish() {
        stopService(Intent(this, MeetTalkIncomingCallService::class.java))
        super.finish()
    }
}
