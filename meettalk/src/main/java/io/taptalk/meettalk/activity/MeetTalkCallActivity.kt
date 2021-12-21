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
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.INSTANCE_KEY
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.MESSAGE
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.TapTalk.Model.TAPMessageModel
import io.taptalk.meettalk.R
import io.taptalk.meettalk.constant.MeetTalkConstant.BroadcastEvent.ACTIVE_USER_LEAVES_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.CONFERENCE_INFO
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetBroadcastEventType.RETRIEVE_PARTICIPANTS_INFO
import io.taptalk.meettalk.constant.MeetTalkConstant.ParticipantRole.PARTICIPANT
import io.taptalk.meettalk.manager.TapCallManager
import io.taptalk.meettalk.model.MeetTalkConferenceInfo
import io.taptalk.meettalk.model.MeetTalkParticipantInfo
import io.taptalk.meettalk.view.MeetTalkCallView
import kotlinx.android.synthetic.main.meettalk_activity_call.*
import org.jitsi.meet.sdk.*
import java.util.*

class MeetTalkCallActivity : JitsiMeetActivity() {

    private lateinit var instanceKey: String
    private lateinit var options: JitsiMeetConferenceOptions
    private lateinit var meetTalkCallView: MeetTalkCallView
    private lateinit var callInitiatedMessage: TAPMessageModel
    private lateinit var conferenceInfo: MeetTalkConferenceInfo
    private lateinit var activeParticipantInfo: MeetTalkParticipantInfo
    private lateinit var activeUserID: String

    private var isAudioMuted = false
    private var isVideoMuted = false
    private var callStartTimestamp = 0L

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onBroadcastReceived(intent)
        }
    }

    companion object {
        fun launch(
            instanceKey: String,
            context: Context,
            options: JitsiMeetConferenceOptions?,
            callInitiatedMessage: TAPMessageModel,
            conferenceInfo: MeetTalkConferenceInfo
        ) {
            val intent = Intent(context, MeetTalkCallActivity::class.java)
            intent.action = "org.jitsi.meet.CONFERENCE"
            intent.putExtra("JitsiMeetConferenceOptions", options)
            intent.putExtra(INSTANCE_KEY, instanceKey)
            intent.putExtra(MESSAGE, callInitiatedMessage)
            intent.putExtra(CONFERENCE_INFO, conferenceInfo)
            if (context !is Activity) {
                intent.flags = FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    /**
     * ==========================================================================================
     * LIFECYCLE
     * ==========================================================================================
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.meettalk_activity_call)

        TapCallManager.activeMeetTalkCallActivity = this
        Log.e(">>>>", "TapExtendedJitsiMeetActivity onCreate: ${TapCallManager.activeMeetTalkCallActivity}")

        initData()
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

    /**
     * ==========================================================================================
     * JITSI MEET ACTIVITY OVERRIDE
     * ==========================================================================================
     */

    override fun getJitsiView(): JitsiMeetView {
//        val fragment = supportFragmentManager.findFragmentById(io.taptalk.meettalk.R.id.fragment_meettalk_call) as MeetTalkCallFragment
//        return fragment.meetTalkCallView
        if (!this::meetTalkCallView.isInitialized) {
            meetTalkCallView = MeetTalkCallView(this)
        }
        return meetTalkCallView
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e(">>>>", "MeetTalkCallActivity onNewIntent: ${TAPUtils.toJsonString(intent?.data)}")
    }

    override fun onConferenceWillJoin(extraData: HashMap<String, Any>?) {
        super.onConferenceWillJoin(extraData)
        Log.e(">>>>", "MeetTalkCallActivity onConferenceWillJoin: ${TAPUtils.toJsonString(extraData)}")
    }

    override fun onConferenceJoined(extraData: HashMap<String, Any>?) {
        Log.e(">>>>", "MeetTalkCallActivity onConferenceJoined: ${TAPUtils.toJsonString(extraData)}")
    }

    override fun onConferenceTerminated(extraData: HashMap<String, Any>?) {
        super.onConferenceTerminated(extraData)
        Log.e(">>>>", "MeetTalkCallActivity onConferenceTerminated: ${TAPUtils.toJsonString(extraData)}")
    }

    override fun onParticipantJoined(extraData: HashMap<String, Any>?) {
        super.onParticipantJoined(extraData)
        Log.e(">>>>", "MeetTalkCallActivity onParticipantJoined: ${TAPUtils.toJsonString(extraData)}")
    }

    override fun onParticipantLeft(extraData: HashMap<String, Any>?) {
        super.onParticipantLeft(extraData)
        Log.e(">>>>", "MeetTalkCallActivity onParticipantLeft: ${TAPUtils.toJsonString(extraData)}")
    }

    /**
     * ==========================================================================================
     * CUSTOM METHODS
     * ==========================================================================================
     */

    private fun initData() {
        instanceKey = intent.getStringExtra(INSTANCE_KEY) ?: ""

        activeUserID = TapTalk.getTapTalkActiveUser(instanceKey).userID

        callInitiatedMessage = intent.getParcelableExtra(MESSAGE)!!
        conferenceInfo = intent.getParcelableExtra(CONFERENCE_INFO)!!

        for (participant in conferenceInfo.participants) {
            if (participant.userID == activeUserID) {
                activeParticipantInfo = participant
                break
            }
        }
        if (!this::activeParticipantInfo.isInitialized) {
            activeParticipantInfo = TapCallManager.generateParticipantInfo(instanceKey, PARTICIPANT)
        }

        options = intent.getParcelableExtra("JitsiMeetConferenceOptions") ?: JitsiMeet.getDefaultConferenceOptions()
        isAudioMuted = options.audioMuted
        isVideoMuted = options.videoMuted
    }

    private fun initView() {
        meetTalkCallView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        fl_meettalk_call_view_container.addView(meetTalkCallView)

        tv_calling_user.text = callInitiatedMessage.room.name
        Glide.with(this).load(callInitiatedMessage.room.imageURL?.fullsize).into(iv_profile_picture)

        showAudioButtonMuted(isAudioMuted)
        showVideoButtonMuted(isVideoMuted)

        iv_button_cancel_call.setOnClickListener { onBackPressed() }
        iv_button_toggle_audio_mute.setOnClickListener { toggleAudioMute() }
        iv_button_toggle_video_mute.setOnClickListener { toggleVideoMute() }
        iv_button_flip_camera.setOnClickListener { flipCamera() }
    }

    private fun registerForBroadcastMessages() {
        val intentFilter = IntentFilter()
        for (type in BroadcastEvent.Type.values()) {
            intentFilter.addAction(type.action)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun onBroadcastReceived(intent: Intent?) {
        if (intent == null) {
            return
        }
        val event = BroadcastEvent(intent)
        when (event.type) {
            BroadcastEvent.Type.CONFERENCE_JOINED -> {
                Log.e(">>>>", "onBroadcastReceived: CONFERENCE_JOINED ${TAPUtils.toJsonString(event.data)}")
                retrieveParticipantsInfo()
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

    private fun retrieveParticipantsInfo() {
        val retrieveParticipantIntent = Intent(RETRIEVE_PARTICIPANTS_INFO)
        retrieveParticipantIntent.putExtra("requestId ", System.currentTimeMillis().toString())
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(retrieveParticipantIntent)
    }

    private fun updateLayout(animated: Boolean) {
        var hasVideoFootage = false
        for (participant in conferenceInfo.participants) {
            if (participant?.userID != activeUserID &&
                null != participant?.videoMuted &&
                !participant.videoMuted!!
            ) {
                showVideoCallLayout(animated)
                hasVideoFootage = true
                break
            }
        }
        if (!hasVideoFootage) {
            showVoiceCallLayout(animated)
        }
    }

    private fun showVoiceCallLayout(animated: Boolean) {
        val duration = if (animated) {
            200L
        }
        else {
            0L
        }
        fl_meettalk_call_view_container.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { fl_meettalk_call_view_container.visibility = View.INVISIBLE }
            .start()
        cl_label_container.animate()
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        cl_button_container.animate()
            .translationY(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        v_button_container_background.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        v_background_solid.animate()
            .alpha(0.2f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun showVideoCallLayout(animated: Boolean) {
        val duration = if (animated) {
            200L
        }
        else {
            0L
        }
        fl_meettalk_call_view_container.visibility = View.VISIBLE
        fl_meettalk_call_view_container.alpha = 0f
        fl_meettalk_call_view_container.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        cl_label_container.animate()
            .translationY(TAPUtils.dpToPx(-56).toFloat())
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        cl_button_container.animate()
            .translationY(TAPUtils.dpToPx(56).toFloat())
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        v_button_container_background.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        v_background_solid.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun toggleAudioMute() {
        isAudioMuted = !isAudioMuted
        showAudioButtonMuted(isAudioMuted)
        val muteBroadcastIntent = BroadcastIntentHelper.buildSetAudioMutedIntent(isAudioMuted)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(muteBroadcastIntent)

        activeParticipantInfo.audioMuted = isAudioMuted
        activeParticipantInfo.lastUpdated = System.currentTimeMillis()
//        conferenceInfo.participants[activeUserID] = activeParticipantInfo
        TapCallManager.sendConferenceInfoNotification(instanceKey, callInitiatedMessage.room, conferenceInfo)
    }

    private fun showAudioButtonMuted(isMuted: Boolean) {
        if (isMuted) {
            iv_button_toggle_audio_mute.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.meettalk_ic_mic_off_white))
            iv_button_toggle_audio_mute.backgroundTintList = ColorStateList.valueOf(getColor(R.color.meetTalkCallScreenInactiveButtonBackgroundColor))
        }
        else {
            iv_button_toggle_audio_mute.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.meettalk_ic_mic_white))
            iv_button_toggle_audio_mute.backgroundTintList = ColorStateList.valueOf(getColor(R.color.meetTalkCallScreenActiveButtonBackgroundColor))
        }
    }

    private fun toggleVideoMute() {
        isVideoMuted = !isVideoMuted
        showVideoButtonMuted(isVideoMuted)
        val muteBroadcastIntent = BroadcastIntentHelper.buildSetVideoMutedIntent(isVideoMuted)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(muteBroadcastIntent)

        activeParticipantInfo.videoMuted = isVideoMuted
        activeParticipantInfo.lastUpdated = System.currentTimeMillis()
//        conferenceInfo.participants[activeUserID] = activeParticipantInfo
        TapCallManager.sendConferenceInfoNotification(instanceKey, callInitiatedMessage.room, conferenceInfo)
    }

    private fun showVideoButtonMuted(isMuted: Boolean) {
        if (isMuted) {
            iv_button_toggle_video_mute.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.meettalk_ic_video_camera_off_white))
            iv_button_toggle_video_mute.backgroundTintList = ColorStateList.valueOf(getColor(R.color.meetTalkCallScreenInactiveButtonBackgroundColor))
        }
        else {
            iv_button_toggle_video_mute.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.meettalk_ic_video_camera_white))
            iv_button_toggle_video_mute.backgroundTintList = ColorStateList.valueOf(getColor(R.color.meetTalkCallScreenActiveButtonBackgroundColor))
        }
    }

    private fun flipCamera() {
        // TODO:
    }

    /**
     * ==========================================================================================
     * PUBLIC METHODS
     * ==========================================================================================
     */

    fun updateConferenceInfo(updatedConferenceInfo: MeetTalkConferenceInfo) {
        this.conferenceInfo.updateValue(updatedConferenceInfo)
        updateLayout(true)
    }
}
