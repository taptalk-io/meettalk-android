package io.taptalk.meettalk.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.MESSAGE
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.TapTalk.Model.TAPMessageModel
import io.taptalk.TapTalk.R
import io.taptalk.meettalk.constant.MeetTalkConstant.BroadcastEvent.ACTIVE_USER_LEAVES_CALL
import io.taptalk.meettalk.manager.TapCallManager
import io.taptalk.meettalk.view.MeetTalkCallView
import kotlinx.android.synthetic.main.meettalk_activity_call.*
import org.jitsi.meet.sdk.*
import java.util.*

class MeetTalkCallActivity : JitsiMeetActivity() {

    lateinit var options: JitsiMeetConferenceOptions
    lateinit var meetTalkCallView: MeetTalkCallView
    lateinit var callInitiatedMessage: TAPMessageModel

    private var isAudioMuted = false
    private var isVideoMuted = false

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onBroadcastReceived(intent)
        }
    }

    companion object {
        fun launch(context: Context, options: JitsiMeetConferenceOptions?, showWaitingScreen: Boolean, callInitiatedMessage: TAPMessageModel) {
            val intent = Intent(context, MeetTalkCallActivity::class.java)
            intent.action = "org.jitsi.meet.CONFERENCE"
            intent.putExtra("JitsiMeetConferenceOptions", options)
            intent.putExtra("showWaitingScreen", showWaitingScreen)
            intent.putExtra(MESSAGE, callInitiatedMessage)
            if (context !is Activity) {
                intent.flags = FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(io.taptalk.meettalk.R.layout.meettalk_activity_call)
//        meetTalkCallView = MeetTalkCallView(this)
//        setContentView(meetTalkCallView)

        callInitiatedMessage = intent.getParcelableExtra(MESSAGE)!!
        options = intent.getParcelableExtra("JitsiMeetConferenceOptions") ?: JitsiMeet.getDefaultConferenceOptions()
        isAudioMuted = options.audioMuted
        isVideoMuted = options.videoMuted

        TapCallManager.activeMeetTalkCallActivity = this
        Log.e(">>>>", "TapExtendedJitsiMeetActivity onCreate: ${TapCallManager.activeMeetTalkCallActivity}")

        initView()
        registerForBroadcastMessages()
    }

    override fun onResume() {
        super.onResume()
        Log.e(">>>>", "TapExtendedJitsiMeetActivity onResume: ")

//        JitsiMeetActivityDelegate.onHostResume(this)
    }

    override fun onPause() {
        super.onPause()
        Log.e(">>>>", "TapExtendedJitsiMeetActivity onPause: ")

//        JitsiMeetActivityDelegate.onHostPause(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.e(">>>>", "TapExtendedJitsiMeetActivity onDestroy: ${TapCallManager.activeMeetTalkCallActivity}")

//        JitsiMeetActivityDelegate.onHostDestroy(this)

        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTIVE_USER_LEAVES_CALL))

        TapCallManager.activeMeetTalkCallActivity = null

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)

        if (isTaskRoot) {
            // Trigger listener callback if no other activity is open
            for (tapTalkInstance in TapTalk.getTapTalkInstances()) {
                for (listener in TapTalk.getTapTalkListeners(tapTalkInstance.key)) {
                    Log.e(">>>> $TAG", "onTaskRootChatRoomClosed: ${tapTalkInstance.key}")
                    listener.onTaskRootChatRoomClosed(this)
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.e(">>>>>", "TapExtendedJitsiMeetActivity onBackPressed: ")
    }

    override fun finish() {
        super.finish()
        Log.e(">>>>>", "TapExtendedJitsiMeetActivity finish: ")
    }

    override fun getJitsiView(): JitsiMeetView {
//        val fragment = supportFragmentManager.findFragmentById(io.taptalk.meettalk.R.id.fragment_meettalk_call) as MeetTalkCallFragment
//        return fragment.meetTalkCallView
        if (!this::meetTalkCallView.isInitialized) {
            meetTalkCallView = MeetTalkCallView(this)
        }
        return meetTalkCallView
    }

    override fun onConferenceJoined(extraData: HashMap<String, Any>?) {
        super.onConferenceJoined(extraData)

//        TapCallManager.activeJitsiMeetActivity = this
        Log.e(">>>>", "TapExtendedJitsiMeetActivity onConferenceJoined: ")

//        if (intent?.getBooleanExtra("showWaitingScreen", false) == true) {
//            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(SHOW_WAITING_SCREEN))
//        }
    }

    override fun onConferenceTerminated(extraData: HashMap<String, Any>?) {
        super.onConferenceTerminated(extraData)

        Log.e(">>>>", "TapExtendedJitsiMeetActivity onConferenceTerminated: ")

//        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTIVE_USER_LEAVES_CALL))
//
//        TapCallManager.activeJitsiMeetActivity = null
    }

    private fun initView() {
        meetTalkCallView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        fl_meettalk_call_view_container.addView(meetTalkCallView)

        tv_calling_user.text = callInitiatedMessage.room.name
        Glide.with(this).load(callInitiatedMessage.room.imageURL?.fullsize).into(iv_profile_picture)

        if (isAudioMuted) {
            iv_button_toggle_audio_mute.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapGrey9b))
        }
        else {
            iv_button_toggle_audio_mute.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapColorPrimary))
        }
        if (isVideoMuted) {
            iv_button_toggle_video_mute.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapGrey9b))
        }
        else {
            iv_button_toggle_video_mute.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapColorPrimary))
        }

        iv_button_cancel_call.setOnClickListener { onBackPressed() }
        iv_button_toggle_audio_mute.setOnClickListener { toggleAudioMute() }
        iv_button_toggle_video_mute.setOnClickListener { toggleVideoMute() }
    }

    private fun registerForBroadcastMessages() {
        val intentFilter = IntentFilter()
        for (type in BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.action)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun onBroadcastReceived(intent: Intent?) {
        if (intent != null) {
            val event = BroadcastEvent(intent)
            when (event.type) {
                BroadcastEvent.Type.CONFERENCE_JOINED -> {
                    Log.e(">>>>", "onBroadcastReceived: CONFERENCE_JOINED ${TAPUtils.toJsonString(event.data)}")
                }
                BroadcastEvent.Type.CONFERENCE_WILL_JOIN -> {
                    Log.e(">>>>", "onBroadcastReceived: CONFERENCE_WILL_JOIN ${TAPUtils.toJsonString(event.data)}")
                }
                BroadcastEvent.Type.CONFERENCE_TERMINATED -> {
                    Log.e(">>>>", "onBroadcastReceived: CONFERENCE_TERMINATED ${TAPUtils.toJsonString(event.data)}")
                }
                BroadcastEvent.Type.PARTICIPANT_JOINED -> {
                    Log.e(">>>>", "onBroadcastReceived: PARTICIPANT_JOINED ${TAPUtils.toJsonString(event.data)}")
                }
                BroadcastEvent.Type.PARTICIPANT_LEFT -> {
                    Log.e(">>>>", "onBroadcastReceived: PARTICIPANT_LEFT ${TAPUtils.toJsonString(event.data)}")
                }
                BroadcastEvent.Type.ENDPOINT_TEXT_MESSAGE_RECEIVED -> {
                    Log.e(">>>>", "onBroadcastReceived: ENDPOINT_TEXT_MESSAGE_RECEIVED ${TAPUtils.toJsonString(event.data)}")
                }
                BroadcastEvent.Type.SCREEN_SHARE_TOGGLED -> {
                    Log.e(">>>>", "onBroadcastReceived: SCREEN_SHARE_TOGGLED ${TAPUtils.toJsonString(event.data)}")
                }
                BroadcastEvent.Type.PARTICIPANTS_INFO_RETRIEVED -> {
                    Log.e(">>>>", "onBroadcastReceived: PARTICIPANTS_INFO_RETRIEVED ${TAPUtils.toJsonString(event.data)}")
                }
                BroadcastEvent.Type.CHAT_MESSAGE_RECEIVED -> {
                    Log.e(">>>>", "onBroadcastReceived: CHAT_MESSAGE_RECEIVED ${TAPUtils.toJsonString(event.data)}")
                }
                BroadcastEvent.Type.CHAT_TOGGLED -> {
                    Log.e(">>>>", "onBroadcastReceived: CHAT_TOGGLED ${TAPUtils.toJsonString(event.data)}")
                }
                BroadcastEvent.Type.AUDIO_MUTED_CHANGED -> {
                    Log.e(">>>>", "onBroadcastReceived: AUDIO_MUTED_CHANGED ${TAPUtils.toJsonString(event.data)}")
                }
                BroadcastEvent.Type.VIDEO_MUTED_CHANGED -> {
                    Log.e(">>>>", "onBroadcastReceived: VIDEO_MUTED_CHANGED ${TAPUtils.toJsonString(event.data)}")
                }
                else -> {
                    Log.e(">>>>", "onBroadcastReceived OTHERS: ${intent.action} ${TAPUtils.toJsonString(intent.data)}")
                }
            }
        }
    }

    private fun toggleAudioMute() {
        isAudioMuted = !isAudioMuted
        if (isAudioMuted) {
            iv_button_toggle_audio_mute.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapGrey9b))
        }
        else {
            iv_button_toggle_audio_mute.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapColorPrimary))
        }
        val muteBroadcastIntent = BroadcastIntentHelper.buildSetAudioMutedIntent(isAudioMuted)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(muteBroadcastIntent)
    }

    private fun toggleVideoMute() {
        isVideoMuted = !isVideoMuted
        if (isVideoMuted) {
            iv_button_toggle_video_mute.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapGrey9b))
        }
        else {
            iv_button_toggle_video_mute.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapColorPrimary))
        }
        val muteBroadcastIntent = BroadcastIntentHelper.buildSetVideoMutedIntent(isVideoMuted)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(muteBroadcastIntent)
    }
}
