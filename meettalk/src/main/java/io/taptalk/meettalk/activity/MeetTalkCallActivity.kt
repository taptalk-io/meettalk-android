package io.taptalk.meettalk.activity

import android.app.Activity
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.INSTANCE_KEY
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.MESSAGE
import io.taptalk.TapTalk.Const.TAPDefaultConstant.RoomType.TYPE_PERSONAL
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.TapTalk.Helper.TapTalk.TapTalkSocketConnectionMode.ALWAYS_ON
import io.taptalk.TapTalk.Listener.TAPSocketListener
import io.taptalk.TapTalk.Listener.TapCommonListener
import io.taptalk.TapTalk.Listener.TapCoreGetMessageListener
import io.taptalk.TapTalk.Manager.TAPConnectionManager
import io.taptalk.TapTalk.Manager.TapCoreMessageManager
import io.taptalk.TapTalk.Model.TAPMessageModel
import io.taptalk.meettalk.R
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.CALL_MESSAGE_TYPE
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.CONFERENCE_INFO
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetBroadcastEventType.RETRIEVE_PARTICIPANTS_INFO
import io.taptalk.meettalk.constant.MeetTalkConstant.ParticipantRole.PARTICIPANT
import io.taptalk.meettalk.helper.MeetTalk
import io.taptalk.meettalk.helper.MeetTalkUtils
import io.taptalk.meettalk.manager.MeetTalkCallManager
import io.taptalk.meettalk.manager.MeetTalkCallManager.Companion.CallState.IDLE
import io.taptalk.meettalk.model.MeetTalkConferenceInfo
import io.taptalk.meettalk.model.MeetTalkParticipantInfo
import io.taptalk.meettalk.view.MeetTalkCallView
import kotlinx.android.synthetic.main.meettalk_activity_call.*
import org.jitsi.meet.sdk.*
import java.util.*
import android.view.WindowManager
import io.taptalk.TapTalk.R.anim.*

class MeetTalkCallActivity : JitsiMeetActivity() {

    private val TAG = MeetTalkCallActivity::class.java.simpleName

    private lateinit var instanceKey: String
    private lateinit var options: JitsiMeetConferenceOptions
    private lateinit var meetTalkCallView: MeetTalkCallView
    private lateinit var callInitiatedMessage: TAPMessageModel
    private lateinit var activeParticipantInfo: MeetTalkParticipantInfo
    private lateinit var activeUserID: String
    private lateinit var roomDisplayName: String
    private lateinit var durationTimer: Timer
    private lateinit var savedSocketConnectionMode: TapTalk.TapTalkSocketConnectionMode

    private var isAudioMuted = false
    private var isVideoMuted = false
    private var isCallStarted = false
    private var callStartTimestamp = 0L

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            onBroadcastReceived(intent)
        }
    }

    private val socketListener = object : TAPSocketListener() {
        override fun onSocketConnected() {
            if (BuildConfig.DEBUG) {
                Log.e(">>>> $TAG", "onSocketConnected: ")
            }
            fetchNewerMessages()

            // TODO: CHECK IF JOINED CALL NOTIFICATION IS ALREADY SENT

            // Trigger listener callback
            for (meetTalkListener in MeetTalk.getMeetTalkListeners(instanceKey)) {
                meetTalkListener.onReconnectedToConference(MeetTalkCallManager.activeConferenceInfo)
            }
        }

        override fun onSocketDisconnected() {
            if (BuildConfig.DEBUG) {
                Log.e(">>>> $TAG", "onSocketDisconnected: ")
            }
            showVoiceCallLayout(true)
            stopCallDurationTimer()
            tv_call_duration_status.text = getString(R.string.meettalk_disconnected)

            // Trigger listener callback
            for (meetTalkListener in MeetTalk.getMeetTalkListeners(instanceKey)) {
                meetTalkListener.onDisconnectedFromConference(MeetTalkCallManager.activeConferenceInfo)
            }
        }

        override fun onSocketConnecting() {
            if (BuildConfig.DEBUG) {
                Log.e(">>>> $TAG", "onSocketConnecting: ")
            }
            showVoiceCallLayout(true)
            stopCallDurationTimer()
            tv_call_duration_status.text = getString(R.string.meettalk_connecting_ellipsis)
        }

        override fun onSocketError() {
            if (BuildConfig.DEBUG) {
                Log.e(">>>> $TAG", "onSocketError: ")
            }
            onSocketDisconnected()
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
                intent.flags = FLAG_ACTIVITY_NEW_TASK// or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(tap_fade_in, tap_stay)
            }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
        else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        MeetTalkCallManager.activeMeetTalkCallActivity = this

        if (BuildConfig.DEBUG) {
            Log.e(">>>>", "MeetTalkCallActivity onCreate: ${MeetTalkCallManager.activeMeetTalkCallActivity}")
        }

        initData()
        initView()
        registerForBroadcastMessages()
        TAPConnectionManager.getInstance(instanceKey).addSocketListener(socketListener)
    }

    override fun onResume() {
        super.onResume()

        if (BuildConfig.DEBUG) {
            Log.e(">>>>", "MeetTalkCallActivity onResume: ")
        }

        JitsiMeetActivityDelegate.onHostResume(this)

        checkIfCallIsEnded()
    }

    override fun onPause() {
        super.onPause()

        if (BuildConfig.DEBUG) {
            Log.e(">>>>", "MeetTalkCallActivity onPause: ")
        }

        JitsiMeetActivityDelegate.onHostPause(this)

        if (!isVideoMuted) {
            toggleVideoMute()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (BuildConfig.DEBUG) {
            Log.e(">>>>", "MeetTalkCallActivity onDestroy: ${MeetTalkCallManager.activeConferenceInfo?.callEndedTime ?: "info null"}")
        }

        JitsiMeetActivityDelegate.onHostDestroy(this)

        if (callInitiatedMessage.room.type == TYPE_PERSONAL) {
            if (MeetTalkCallManager.activeConferenceInfo != null &&
                MeetTalkCallManager.activeConferenceInfo!!.callEndedTime == 0L
            ) {
                if (isCallStarted || MeetTalkCallManager.activeConferenceInfo!!.participants.size > 1 ) {
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>>>", "MeetTalkCallActivity onDestroy: sendCallEndedNotification")
                    }
                    // Send call ended notification to notify the other party
                    MeetTalkCallManager.sendCallEndedNotification(instanceKey, callInitiatedMessage.room)
                }
                else if (MeetTalkCallManager.activeConferenceInfo!!.hostUserID == activeUserID) {
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>>>", "MeetTalkCallActivity onDestroy: sendCallCancelledNotification")
                    }
                    // Send call cancelled notification to notify recipient
                    MeetTalkCallManager.sendCallCancelledNotification(instanceKey, callInitiatedMessage.room)
                }
            }
        }
        else {
            // Send left call notification to conference
            if (BuildConfig.DEBUG) {
                Log.e(">>>>>", "MeetTalkCallActivity onDestroy: sendLeftCallNotification")
            }
            MeetTalkCallManager.sendLeftCallNotification(instanceKey, callInitiatedMessage.room)
        }

        MeetTalkCallManager.setActiveCallAsEnded()
        MeetTalkCallManager.activeMeetTalkCallActivity = null
        MeetTalkCallManager.callState = IDLE
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        TapTalk.setTapTalkSocketConnectionMode(instanceKey, savedSocketConnectionMode)
        TAPConnectionManager.getInstance(instanceKey).removeSocketListener(socketListener)

        if (isTaskRoot) {
            // Trigger listener callback if no other activity is open
            for (meetTalkListener in MeetTalk.getMeetTalkListeners(instanceKey)) {
                meetTalkListener.onTaskRootCallActivityClosed(this)
            }
        }
    }

    override fun finish() {
        if (isFinishing) {
            return
        }
        runOnUiThread {
            super.finish()
            overridePendingTransition(tap_stay, tap_fade_out)
        }
        if (BuildConfig.DEBUG) {
            Log.e(">>>>>", "MeetTalkCallActivity finish: ")
        }
    }

    /**
     * ==========================================================================================
     * JITSI MEET ACTIVITY OVERRIDE
     * ==========================================================================================
     */

    override fun getJitsiView(): JitsiMeetView {
        if (!this::meetTalkCallView.isInitialized) {
            meetTalkCallView = MeetTalkCallView(this)
        }
        return meetTalkCallView
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (BuildConfig.DEBUG) {
            Log.e(">>>>", "MeetTalkCallActivity onNewIntent: ${TAPUtils.toJsonString(intent?.data)}")
        }
    }

    override fun onConferenceWillJoin(extraData: HashMap<String, Any>?) {
        super.onConferenceWillJoin(extraData)
        if (BuildConfig.DEBUG) {
            Log.e(">>>>", "MeetTalkCallActivity onConferenceWillJoin: ${TAPUtils.toJsonString(extraData)}")
        }
    }

    override fun onConferenceJoined(extraData: HashMap<String, Any>?) {
        if (BuildConfig.DEBUG) {
            Log.e(">>>>", "MeetTalkCallActivity onConferenceJoined: ${TAPUtils.toJsonString(extraData)}")
        }
        enableButtons()
//        retrieveParticipantsInfo()
        if (callInitiatedMessage.room.type == TYPE_PERSONAL) {
            // Joined an existing call, send participant joined notification
            MeetTalkCallManager.sendJoinedCallNotification(
                instanceKey,
                callInitiatedMessage.room
            )

            // Set status text to Waiting for User
            if (!isCallStarted) {
                tv_call_duration_status.text = String.format(
                    getString(R.string.meettalk_format_waiting_for_user_ellipsis),
                    TAPUtils.getFirstWordOfString(roomDisplayName)
                )
            }
        }

        // Trigger listener callback
        for (meetTalkListener in MeetTalk.getMeetTalkListeners(instanceKey)) {
            meetTalkListener.onConferenceJoined(MeetTalkCallManager.activeConferenceInfo)
        }
    }

    override fun onConferenceTerminated(extraData: HashMap<String, Any>?) {
        super.onConferenceTerminated(extraData)
        if (BuildConfig.DEBUG) {
            Log.e(">>>>", "MeetTalkCallActivity onConferenceTerminated: ${TAPUtils.toJsonString(extraData)}")
        }

        // Trigger listener callback
        for (meetTalkListener in MeetTalk.getMeetTalkListeners(instanceKey)) {
            meetTalkListener.onConferenceTerminated(MeetTalkCallManager.activeConferenceInfo)
        }
    }

    override fun onParticipantJoined(extraData: HashMap<String, Any>?) {
        super.onParticipantJoined(extraData)
        if (BuildConfig.DEBUG) {
            Log.e(">>>>", "MeetTalkCallActivity onParticipantJoined: ${TAPUtils.toJsonString(extraData)}")
        }
//        if (extraData != null && extraData["isLocal"] == false) {
//
//        }
    }

    override fun onParticipantLeft(extraData: HashMap<String, Any>?) {
        super.onParticipantLeft(extraData)
        if (BuildConfig.DEBUG) {
            Log.e(">>>>", "MeetTalkCallActivity onParticipantLeft: ${TAPUtils.toJsonString(extraData)}")
        }
        if (callInitiatedMessage.room.type == TYPE_PERSONAL) {
            // The other user left, terminate the call
            finish()
        }
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

        savedSocketConnectionMode = TapTalk.getTapTalkSocketConnectionMode(instanceKey)
        TapTalk.setTapTalkSocketConnectionMode(instanceKey, ALWAYS_ON)
        TapTalk.connect(instanceKey, object : TapCommonListener() {})

        if (MeetTalkCallManager.getRoomAliasMap(instanceKey)[callInitiatedMessage.room.roomID].isNullOrEmpty()) {
            roomDisplayName = callInitiatedMessage.room.name
        }
        else {
            roomDisplayName = MeetTalkCallManager.getRoomAliasMap(instanceKey)[callInitiatedMessage.room.roomID]!!
        }

        if (MeetTalkCallManager.activeConferenceInfo != null) {
            for (participant in MeetTalkCallManager.activeConferenceInfo!!.participants) {
                if (participant.userID == activeUserID) {
                    activeParticipantInfo = participant
                    break
                }
            }
        }
        if (!this::activeParticipantInfo.isInitialized) {
            activeParticipantInfo = MeetTalkCallManager.generateParticipantInfo(instanceKey, PARTICIPANT)
        }

        options = intent.getParcelableExtra("JitsiMeetConferenceOptions") ?: JitsiMeet.getDefaultConferenceOptions()
        isAudioMuted = MeetTalkCallManager.defaultAudioMuted //options.audioMuted
        isVideoMuted = MeetTalkCallManager.defaultVideoMuted //options.videoMuted
    }

    private fun initView() {
        runOnUiThread {
            meetTalkCallView.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            fl_meettalk_call_view_container.addView(meetTalkCallView)

            tv_room_display_name.text = roomDisplayName

            if (!callInitiatedMessage.room.imageURL?.fullsize.isNullOrEmpty()) {
                loadRoomPicture()
            }
            else {
                iv_profile_picture.visibility = View.GONE
            }

            iv_button_toggle_audio_mute.alpha = 0.5f
            iv_button_toggle_video_mute.alpha = 0.5f
            showAudioButtonMuted(isAudioMuted)
            showVideoButtonMuted(isVideoMuted)

            iv_button_cancel_call.setOnClickListener { onBackPressed() }
        }
    }

    private fun loadRoomPicture() {
        Glide
            .with(this)
            .load(callInitiatedMessage.room.imageURL?.fullsize)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    iv_profile_picture.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    iv_profile_picture.visibility = View.VISIBLE
                    return false
                }
            })
            .into(iv_profile_picture)
    }

    private fun enableButtons() {
        runOnUiThread {
            iv_button_toggle_audio_mute.setOnClickListener { toggleAudioMute() }
            iv_button_toggle_video_mute.setOnClickListener { toggleVideoMute() }
//            iv_button_flip_camera.setOnClickListener { flipCamera() }
            iv_button_toggle_audio_mute.animate()
                .alpha(1.0f)
                .setDuration(200L)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
            iv_button_toggle_video_mute.animate()
                .alpha(1.0f)
                .setDuration(200L)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
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
            BroadcastEvent.Type.ENDPOINT_TEXT_MESSAGE_RECEIVED -> {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>", "onBroadcastReceived: ENDPOINT_TEXT_MESSAGE_RECEIVED ${TAPUtils.toJsonString(event.data)}")
                }
            }
            BroadcastEvent.Type.SCREEN_SHARE_TOGGLED -> {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>", "onBroadcastReceived: SCREEN_SHARE_TOGGLED ${TAPUtils.toJsonString(event.data)}")
                }
            }
            BroadcastEvent.Type.PARTICIPANTS_INFO_RETRIEVED -> {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>", "onBroadcastReceived: PARTICIPANTS_INFO_RETRIEVED ${TAPUtils.toJsonString(event.data)}")
                }
//                if (event.data != null) {
//                    val participantInfoList: List<ParticipantInfo> = Gson().fromJson<Any>(
//                        event.data["participantsInfo"].toString(),
//                        object : TypeToken<java.util.ArrayList<ParticipantInfo?>?>() {}.type
//                    ) as List<ParticipantInfo>

//                    val participantsInfo: ArrayList<Any?> =
//                        TAPUtils.fromJSON(
//                            object : TypeReference<ArrayList<Any?>>() {},
//                            event.data["participantsInfo"] as String
//                        )
//                }
            }
            BroadcastEvent.Type.CHAT_MESSAGE_RECEIVED -> {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>", "onBroadcastReceived: CHAT_MESSAGE_RECEIVED ${TAPUtils.toJsonString(event.data)}")
                }
            }
            BroadcastEvent.Type.CHAT_TOGGLED -> {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>", "onBroadcastReceived: CHAT_TOGGLED ${TAPUtils.toJsonString(event.data)}")
                }
            }
            BroadcastEvent.Type.AUDIO_MUTED_CHANGED -> {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>", "onBroadcastReceived: AUDIO_MUTED_CHANGED ${TAPUtils.toJsonString(event.data)}")
                }
            }
            BroadcastEvent.Type.VIDEO_MUTED_CHANGED -> {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>", "onBroadcastReceived: VIDEO_MUTED_CHANGED ${TAPUtils.toJsonString(event.data)}")
                }
                if (!isVideoMuted && isCallStarted) {
                    MeetTalkCallManager.sendConferenceInfoNotification(instanceKey, callInitiatedMessage.room)
                }
            }
            else -> {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>", "onBroadcastReceived OTHERS: ${intent.action} ${TAPUtils.toJsonString(intent.data)}")
                }
            }
        }
    }

    fun retrieveParticipantsInfo() {
        val retrieveParticipantIntent = Intent(RETRIEVE_PARTICIPANTS_INFO)
        retrieveParticipantIntent.putExtra("requestId ", System.currentTimeMillis().toString())
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(retrieveParticipantIntent)
    }

    private fun updateLayout(animated: Boolean) {
        if (MeetTalkCallManager.activeConferenceInfo == null) {
            return
        }
        var hasVideoFootage = false
        for (participant in MeetTalkCallManager.activeConferenceInfo!!.participants) {
            if (participant?.userID != activeUserID && // TODO: SHOW OWN VIDEO FOOTAGE
                participant?.videoMuted != null &&
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
        if (fl_meettalk_call_view_container.visibility == View.INVISIBLE) {
            return
        }
        val duration = if (animated) {
            200L
        }
        else {
            0L
        }
        val interpolator = AccelerateDecelerateInterpolator()
        runOnUiThread {
            fl_meettalk_call_view_container.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .withEndAction { fl_meettalk_call_view_container.visibility = View.INVISIBLE }
                .start()
            cl_label_container.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
            cl_button_container.animate()
                .translationY(0f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
            v_button_container_background.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
            v_background_overlay_solid.animate()
                .alpha(0.2f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
        }
    }

    private fun showVideoCallLayout(animated: Boolean) {
        if (fl_meettalk_call_view_container.visibility == View.VISIBLE) {
            return
        }
        val duration = if (animated) {
            200L
        }
        else {
            0L
        }
        val interpolator = AccelerateDecelerateInterpolator()
        runOnUiThread {
            fl_meettalk_call_view_container.visibility = View.VISIBLE
            fl_meettalk_call_view_container.alpha = 0f
            fl_meettalk_call_view_container.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
            cl_label_container.animate()
                .alpha(0f)
                .translationY(TAPUtils.dpToPx(-56).toFloat())
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
            cl_button_container.animate()
                .translationY(TAPUtils.dpToPx(56).toFloat())
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
            v_button_container_background.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
            v_background_overlay_solid.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
        }
    }

    private fun updateActiveParticipantInConferenceInfo() {
        if (MeetTalkCallManager.activeConferenceInfo == null) {
            return
        }
        for (participant in MeetTalkCallManager.activeConferenceInfo!!.participants) {
            if (participant?.userID == activeUserID) {
                MeetTalkCallManager.activeConferenceInfo!!.participants[MeetTalkCallManager.activeConferenceInfo!!.participants.indexOf(participant)] =
                    activeParticipantInfo
                break
            }
        }
    }

    private fun toggleAudioMute() {
        isAudioMuted = !isAudioMuted
        showAudioButtonMuted(isAudioMuted)
        val muteBroadcastIntent = BroadcastIntentHelper.buildSetAudioMutedIntent(isAudioMuted)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(muteBroadcastIntent)

        activeParticipantInfo.audioMuted = isAudioMuted
        activeParticipantInfo.lastUpdated = System.currentTimeMillis()
        updateActiveParticipantInConferenceInfo()
        if (isCallStarted) {
            MeetTalkCallManager.sendConferenceInfoNotification(instanceKey, callInitiatedMessage.room)
        }
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
        updateActiveParticipantInConferenceInfo()
        if (isCallStarted) {
            MeetTalkCallManager.sendConferenceInfoNotification(instanceKey, callInitiatedMessage.room)
        }
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

    private fun startCallDurationTimer() {
        if (callStartTimestamp == 0L) {
            return
        }

        durationTimer = Timer()

        tv_call_duration_status.text = getString(R.string.meettalk_connected)

        val timerTask: TimerTask
        timerTask = object : TimerTask() {
            override fun run() {
                val duration = System.currentTimeMillis() - callStartTimestamp
                val durationString = MeetTalkUtils.getCallDurationString(duration)
                runOnUiThread {
                    tv_call_duration_status.text = durationString
                }
            }
        }
        durationTimer.schedule(timerTask, 1000L, 1000L)
    }

    private fun stopCallDurationTimer() {
        if (!this::durationTimer.isInitialized) {
            return
        }
        durationTimer.cancel()
    }

    private fun fetchNewerMessages() {
        if (MeetTalkCallManager.activeConferenceInfo == null) {
            return
        }
        // Fetch missed notifications when socket was offline
        TapCoreMessageManager.getInstance(instanceKey).getNewerMessagesAfterTimestamp(
            callInitiatedMessage.room.roomID,
            MeetTalkCallManager.activeConferenceInfo!!.lastUpdated,
            MeetTalkCallManager.activeConferenceInfo!!.lastUpdated,
            object : TapCoreGetMessageListener() {
                override fun onSuccess(messages: MutableList<TAPMessageModel>?) {
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>> $TAG", "getNewerMessagesAfterTimestamp onSuccess: ${messages?.size}")
                    }
                    if (!messages.isNullOrEmpty()) {
                        messages.reverse()
                        for (message in messages) {
                            if (message.room.roomID == callInitiatedMessage.room.roomID &&
                                message.type == CALL_MESSAGE_TYPE
                            ) {
                                MeetTalkCallManager.checkAndHandleCallNotificationFromMessage(
                                    message,
                                    instanceKey,
                                    TapTalk.getTapTalkActiveUser(instanceKey)
                                )
                            }
                        }
                    }
                    if (!isFinishing) {
                        if (MeetTalkCallManager.activeConferenceInfo != null) {
                            if (BuildConfig.DEBUG) {
                                Log.e(">>>> $TAG", "getNewerMessagesAfterTimestamp: onConferenceInfoUpdated")
                            }
                            startCallDurationTimer()
                            onConferenceInfoUpdated(MeetTalkCallManager.activeConferenceInfo!!)
                        }
                        else {
                            if (BuildConfig.DEBUG) {
                                Log.e(">>>> $TAG", "getNewerMessagesAfterTimestamp: finish")
                            }
                            finish()
                        }
                    }
                }

                override fun onError(errorCode: String?, errorMessage: String?) {
                    // TODO: Request latest conference info
                }
            }
        )
    }

    private fun checkIfCallIsEnded() : Boolean {
        if (callInitiatedMessage.room.type == TYPE_PERSONAL &&
            (MeetTalkCallManager.activeConferenceInfo == null ||
                    MeetTalkCallManager.activeConferenceInfo!!.callEndedTime > 0L)
        ) {
            finish()
            return true
        }
        return false
    }

    /**
     * ==========================================================================================
     * PUBLIC METHODS
     * ==========================================================================================
     */

    fun onConferenceInfoUpdated(updatedConferenceInfo: MeetTalkConferenceInfo) {
        if (BuildConfig.DEBUG) {
            Log.e(">>>> $TAG", "onConferenceInfoUpdated: ${TAPUtils.toJsonString(updatedConferenceInfo)}")
            Log.e(">>>> $TAG", "onConferenceInfoUpdated: $isCallStarted")
            Log.e(">>>> $TAG", "onConferenceInfoUpdated: ${updatedConferenceInfo.participants.size}")
        }

        if (checkIfCallIsEnded()) {
            return
        }

        if (MeetTalkCallManager.activeConferenceInfo != null &&
            !isCallStarted &&
            callInitiatedMessage.room.type == TYPE_PERSONAL &&
            updatedConferenceInfo.participants.size > 1
        ) {
            // Recipient has joined, mark the call as started
            isCallStarted = true
            if (MeetTalkCallManager.activeConferenceInfo!!.callStartedTime == 0L) {
                MeetTalkCallManager.activeConferenceInfo!!.callStartedTime = System.currentTimeMillis()
            }
            callStartTimestamp = MeetTalkCallManager.activeConferenceInfo!!.callStartedTime
            //enableButtons()
            startCallDurationTimer()

            // Send updated conference info
            MeetTalkCallManager.sendConferenceInfoNotification(
                instanceKey,
                callInitiatedMessage.room
            )
        }

        if (isCallStarted) {
            updateLayout(true)
        }
    }
}
