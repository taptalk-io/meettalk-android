package io.taptalk.meettalk.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.MESSAGE
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.TapTalk.Model.TAPMessageModel
import io.taptalk.TapTalk.R
import io.taptalk.meettalk.constant.MeetTalkConstant.BroadcastEvent.ACTIVE_USER_LEAVES_CALL
import io.taptalk.meettalk.manager.TapCallManager
import kotlinx.android.synthetic.main.meettalk_activity_call.*
import org.jitsi.meet.sdk.*
import java.util.HashMap

class MeetTalkCallActivity : JitsiMeetActivity() {

    lateinit var options: JitsiMeetConferenceOptions
    lateinit var callInitiatedMessage: TAPMessageModel

    private var isAudioMuted = false
    private var isVideoMuted = false

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

//        fun launch(context: Context, url: String?) {
//            val options = JitsiMeetConferenceOptions.Builder().setRoom(url).build()
//            launch(context, options)
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(io.taptalk.meettalk.R.layout.meettalk_activity_call)

        callInitiatedMessage = intent.getParcelableExtra(MESSAGE)!!
        options = intent.getParcelableExtra("JitsiMeetConferenceOptions") ?: JitsiMeet.getDefaultConferenceOptions()
        isAudioMuted = options.audioMuted
        isVideoMuted = options.videoMuted

        TapCallManager.activeMeetTalkCallActivity = this
        Log.e(">>>>", "TapExtendedJitsiMeetActivity onCreate: ${TapCallManager.activeMeetTalkCallActivity}")

//        if (intent?.getBooleanExtra("showWaitingScreen", false) == true) {
//            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(SHOW_WAITING_SCREEN))
//        }

        initView()
    }

    override fun onResume() {
        super.onResume()
        Log.e(">>>>", "TapExtendedJitsiMeetActivity onResume: ")

        JitsiMeetActivityDelegate.onHostResume(this)
    }

    override fun onPause() {
        super.onPause()
        Log.e(">>>>", "TapExtendedJitsiMeetActivity onPause: ")

        JitsiMeetActivityDelegate.onHostPause(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.e(">>>>", "TapExtendedJitsiMeetActivity onDestroy: ${TapCallManager.activeMeetTalkCallActivity}")

        JitsiMeetActivityDelegate.onHostDestroy(this)

        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(ACTIVE_USER_LEAVES_CALL))

        TapCallManager.activeMeetTalkCallActivity = null

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
        overridePendingTransition(R.anim.tap_stay, R.anim.tap_slide_down)
        Log.e(">>>>>", "TapExtendedJitsiMeetActivity onBackPressed: ")
    }

    override fun finish() {
        super.finish()
        Log.e(">>>>>", "TapExtendedJitsiMeetActivity finish: ")
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
        tv_calling_user.text = callInitiatedMessage.room.name
        Glide.with(this).load(callInitiatedMessage.room.imageURL?.fullsize).into(iv_profile_picture)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isAudioMuted) {
                iv_button_toggle_audio_mute.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapGrey9b))
            } else {
                iv_button_toggle_audio_mute.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapColorPrimary))
            }
            if (isVideoMuted) {
                iv_button_toggle_video_mute.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapGrey9b))
            } else {
                iv_button_toggle_video_mute.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapColorPrimary))
            }
        }

        iv_button_cancel_call.setOnClickListener { onBackPressed() }
        iv_button_toggle_audio_mute.setOnClickListener { toggleAudioMute() }
        iv_button_toggle_video_mute.setOnClickListener { toggleVideoMute() }
    }

    private fun toggleAudioMute() {
        isAudioMuted = !isAudioMuted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isAudioMuted) {
                iv_button_toggle_audio_mute.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapGrey9b))
            } else {
                iv_button_toggle_audio_mute.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapColorPrimary))
            }
        }
        val muteBroadcastIntent = BroadcastIntentHelper.buildSetAudioMutedIntent(isAudioMuted)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(muteBroadcastIntent)
    }

    private fun toggleVideoMute() {
        isVideoMuted = !isVideoMuted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isVideoMuted) {
                iv_button_toggle_video_mute.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapGrey9b))
            } else {
                iv_button_toggle_video_mute.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tapColorPrimary))
            }
        }
        val muteBroadcastIntent = BroadcastIntentHelper.buildSetVideoMutedIntent(isVideoMuted)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(muteBroadcastIntent)
    }
}
