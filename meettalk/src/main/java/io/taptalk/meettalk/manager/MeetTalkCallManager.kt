package io.taptalk.meettalk.manager

import android.Manifest
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import io.taptalk.TapTalk.Const.TAPDefaultConstant.RoomType.TYPE_PERSONAL
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.TapTalk.Helper.TapTalkDialog
import io.taptalk.TapTalk.Listener.TAPSocketListener
import io.taptalk.TapTalk.Listener.TapCommonListener
import io.taptalk.TapTalk.Listener.TapCoreGetMessageListener
import io.taptalk.TapTalk.Listener.TapCoreSendMessageListener
import io.taptalk.TapTalk.Manager.TAPChatManager
import io.taptalk.TapTalk.Manager.TAPConnectionManager
import io.taptalk.TapTalk.Manager.TapCoreMessageManager
import io.taptalk.TapTalk.Model.TAPMessageModel
import io.taptalk.TapTalk.Model.TAPRoomModel
import io.taptalk.TapTalk.Model.TAPUserModel
import io.taptalk.meettalk.BuildConfig
import io.taptalk.meettalk.R
import io.taptalk.meettalk.activity.MeetTalkCallActivity
import io.taptalk.meettalk.activity.MeetTalkIncomingCallActivity
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.CALL_CANCELLED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.CALL_ENDED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.CALL_INITIATED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.CONFERENCE_INFO
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.PARTICIPANT_JOINED_CONFERENCE
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.PARTICIPANT_LEFT_CONFERENCE
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.RECIPIENT_ANSWERED_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.RECIPIENT_BUSY
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.RECIPIENT_MISSED_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.RECIPIENT_REJECTED_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.RECIPIENT_UNABLE_TO_RECEIVE_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.CALL_MESSAGE_TYPE
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.CALLER_NAME
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.CALLER_NUMBER
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_NOTIFICATION_CONTENT
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_NOTIFICATION_TITLE
import io.taptalk.meettalk.constant.MeetTalkConstant.IncomingCallNotification.INCOMING_CALL_NOTIFICATION_CHANNEL_DESCRIPTION
import io.taptalk.meettalk.constant.MeetTalkConstant.IncomingCallNotification.INCOMING_CALL_NOTIFICATION_CHANNEL_ID
import io.taptalk.meettalk.constant.MeetTalkConstant.IncomingCallNotification.INCOMING_CALL_NOTIFICATION_CHANNEL_NAME
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.ADD_PEOPLE_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.AUDIO_MUTE_BUTTON_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.CALL_INTEGRATION_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.CHAT_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.FILMSTRIP_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.HELP_BUTTON_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.INVITE_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.KICK_OUT_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.LOBBY_MODE_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.MEETING_NAME_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.MEETING_PASSWORD_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.NOTIFICATIONS_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.OVERFLOW_MENU_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.RAISE_HAND_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.REACTIONS_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.RECORDING_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.SECURITY_OPTIONS_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.TILE_VIEW_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.TOOLBOX_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.VIDEO_MUTE_BUTTON_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.JitsiMeetFlag.VIDEO_SHARE_BUTTON_ENABLED
import io.taptalk.meettalk.constant.MeetTalkConstant.ParticipantRole.HOST
import io.taptalk.meettalk.constant.MeetTalkConstant.ParticipantRole.PARTICIPANT
import io.taptalk.meettalk.constant.MeetTalkConstant.RequestCode.REQUEST_PERMISSION_AUDIO
import io.taptalk.meettalk.constant.MeetTalkConstant.RequestCode.REQUEST_PERMISSION_CAMERA
import io.taptalk.meettalk.constant.MeetTalkConstant.Url.MEET_ROOM_ID_PREFIX
import io.taptalk.meettalk.constant.MeetTalkConstant.Url.MEET_URL
import io.taptalk.meettalk.constant.MeetTalkConstant.Value.INCOMING_CALL_TIMEOUT_DURATION
import io.taptalk.meettalk.helper.MeetTalk
import io.taptalk.meettalk.helper.MeetTalkCallConnection
import io.taptalk.meettalk.helper.MeetTalkConnectionService
import io.taptalk.meettalk.helper.MeetTalkIncomingCallService
import io.taptalk.meettalk.model.MeetTalkConferenceInfo
import io.taptalk.meettalk.model.MeetTalkParticipantInfo
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@RequiresApi(Build.VERSION_CODES.M)
class MeetTalkCallManager {

    companion object {
        enum class CallState {
            IDLE,
            RINGING, // Incoming call received
            IN_CALL, // In outgoing call or accepted incoming call
        }

        val defaultAudioMuted = BuildConfig.DEBUG
        const val defaultVideoMuted = true

        var callState = CallState.IDLE
        var activeCallMessage: TAPMessageModel? = null
        var activeCallInstanceKey: String? = null
        var activeMeetTalkCallActivity: MeetTalkCallActivity? = null
        var activeMeetTalkIncomingCallActivity: MeetTalkIncomingCallActivity? = null
        var activeConferenceInfo: MeetTalkConferenceInfo? = null
        var pendingNotificationMessageInstanceKey: String? = null

        private val appName = TapTalk.appContext.getString(R.string.app_name)
        private val telecomManager = TapTalk.appContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        private val phoneAccountHandle = PhoneAccountHandle(ComponentName(TapTalk.appContext, MeetTalkConnectionService::class.java), appName)
        private var pendingIncomingCallRoomID: String? = null
        private var pendingIncomingCallPhoneNumber: String? = null
        private var pendingCallNotificationMessages: ArrayList<TAPMessageModel> = ArrayList()
        private var handledCallNotificationMessageLocalIDs: ArrayList<String> = ArrayList()
        private var roomAliasMap: HashMap<String, HashMap<String, String>> = HashMap()
        private var socketListener: TAPSocketListener? = null

        private lateinit var missedCallTimer: Timer

        init {
            // Initialize Jitsi Meet
            val serverURL: URL = try {
//                URL(MEET_URL)
                URL("https://meet.jit.si") // FIXME: TEMPORARILY DIRECT CALL TO DEFAULT SERVER
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                throw RuntimeException("Invalid server URL!")
            }

            val defaultOptions = JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                //.setWelcomePageEnabled(false)
                .setFeatureFlag(ADD_PEOPLE_ENABLED, false)
                .setFeatureFlag(AUDIO_MUTE_BUTTON_ENABLED, false)
                .setFeatureFlag(CALL_INTEGRATION_ENABLED, false)
                .setFeatureFlag(CHAT_ENABLED, false)
                .setFeatureFlag(FILMSTRIP_ENABLED, false)
                .setFeatureFlag(HELP_BUTTON_ENABLED, false)
                .setFeatureFlag(INVITE_ENABLED, false)
                .setFeatureFlag(KICK_OUT_ENABLED, false)
                .setFeatureFlag(LOBBY_MODE_ENABLED, false)
                .setFeatureFlag(MEETING_NAME_ENABLED, false)
                .setFeatureFlag(MEETING_PASSWORD_ENABLED, false)
                .setFeatureFlag(NOTIFICATIONS_ENABLED, false)
                .setFeatureFlag(OVERFLOW_MENU_ENABLED, false)
                .setFeatureFlag(RAISE_HAND_ENABLED, false)
                .setFeatureFlag(REACTIONS_ENABLED, false)
                .setFeatureFlag(RECORDING_ENABLED, false)
                .setFeatureFlag(SECURITY_OPTIONS_ENABLED, false)
                .setFeatureFlag(TILE_VIEW_ENABLED, false)
                .setFeatureFlag(TOOLBOX_ENABLED, false)
                .setFeatureFlag(VIDEO_MUTE_BUTTON_ENABLED, false)
                .setFeatureFlag(VIDEO_SHARE_BUTTON_ENABLED, false)
                .build()
            JitsiMeet.setDefaultConferenceOptions(defaultOptions)

            buildAndRegisterPhoneAccount()

            initSocketListener()
//            initBroadcastReceiver()
            createIncomingCallNotificationChannel()
        }

        private fun initSocketListener() {
            socketListener = object : TAPSocketListener() {
                override fun onSocketConnected() {
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>>", "MeetTalkCallManager onSocketConnected: sendPendingCallNotificationMessages")
                    }
                    sendPendingCallNotificationMessages()
                }

                override fun onSocketDisconnected() {
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>>", "MeetTalkCallManager onSocketDisconnected: ")
                    }
                    // FIXME: SOCKET IS DISCONNECTED WHEN NATIVE INCOMING CALL IS SHOWN
                    if (callState == CallState.RINGING) {
                        TapTalk.connect(activeCallInstanceKey!!, object : TapCommonListener() {})
                    }
                }
            }
        }

//        private fun initBroadcastReceiver() {
//            TAPBroadcastManager.register(MeetTalk.appContext, object : BroadcastReceiver() {
//                override fun onReceive(context: Context?, intent: Intent?) {
//                    Log.e(">>>>>>>", "onReceive broadcast: ${intent?.action ?: "null"}")
//                    if (intent?.action == ANSWER_INCOMING_CALL && activeCallInstanceKey != null) {
//                        // Trigger listener callback
//                        for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
//                            meetTalkListener.onIncomingCallAnswered()
//                        }
//                        clearPendingIncomingCall()
//                    }
//                    else if (intent?.action == REJECT_INCOMING_CALL && activeCallInstanceKey != null) {
//                        // Trigger listener callback
//                        for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
//                            meetTalkListener.onIncomingCallRejected()
//                        }
//                        clearPendingIncomingCall()
//                    }
//                }
//            },
//            ANSWER_INCOMING_CALL,
//            REJECT_INCOMING_CALL
//            )
//        }

        fun isPhoneAccountEnabled() : Boolean {
            return getPhoneAccount()?.isEnabled == true
        }

        fun isEnablePhoneAccountSettingsRequested(instanceKey: String) : Boolean {
            return MeetTalkDataManager.getInstance(instanceKey).getEnablePhoneAccountSettingsRequestedAppName() == appName
        }

        fun requestEnablePhoneAccountSettings(instanceKey: String, activity: Activity) {
            TapTalkDialog.Builder(activity)
                .setTitle(activity.getString(R.string.meettalk_enable_voice_call))
                .setMessage(String.format(activity.getString(R.string.meettalk_format_enable_phone_account_message), appName))
                .setCancelable(false)
                .setPrimaryButtonTitle(activity.getString(R.string.tap_go_to_settings))
                .setPrimaryButtonListener { openPhoneAccountSettings() }
                .setSecondaryButtonTitle(activity.getString(R.string.meettalk_dismiss))
                .show()
            MeetTalkDataManager.getInstance(instanceKey).setEnablePhoneAccountSettingsRequestedAppName(appName)
        }

        fun checkAndRequestEnablePhoneAccountSettings(instanceKey: String, activity: Activity) {
            if (isEnablePhoneAccountSettingsRequested(instanceKey) || isPhoneAccountEnabled()) {
                return
            }
            requestEnablePhoneAccountSettings(instanceKey, activity)
        }

        fun openPhoneAccountSettings() {
            val intent = Intent()
            if (Build.MANUFACTURER.equals("Samsung", ignoreCase = true)) {
                intent.component = ComponentName(
                    "com.android.server.telecom",
                    "com.android.server.telecom.settings.EnableAccountPreferenceActivity"
                )
            } else {
                intent.action = TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            TapTalk.appContext.startActivity(intent)
        }

        private fun buildAndRegisterPhoneAccount() {
            if (getPhoneAccount() == null) {
                val phoneAccount = PhoneAccount.builder(phoneAccountHandle, appName)
                    .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER).build()
                telecomManager.registerPhoneAccount(phoneAccount)
            }
        }

        private fun getPhoneAccount() : PhoneAccount? {
            return telecomManager.getPhoneAccount(phoneAccountHandle)
        }

        fun openAppNotificationSettings(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val settingsIntent: Intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    .putExtra(Settings.EXTRA_CHANNEL_ID, INCOMING_CALL_NOTIFICATION_CHANNEL_ID)
                context.startActivity(settingsIntent)
            }
            else {
                val settingsIntent: Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(settingsIntent)
            }
        }

        fun showIncomingCall(message: TAPMessageModel?, displayName: String?, displayPhoneNumber: String?) {
            if (callState != CallState.IDLE) {
                return
            }
            val name: String
            if (!displayName.isNullOrEmpty() && message != null && activeCallInstanceKey != null) {
                getRoomAliasMap(activeCallInstanceKey!!)[message.room.roomID] = displayName
                name = displayName
            }
            else if (message != null) {
                name = message.user?.fullname ?: ""
            }
            else {
                name = ""
            }
            val phoneNumber = if (displayPhoneNumber.isNullOrEmpty()) {
                if (message?.user != null) {
                    // TODO: HANDLE COUNTRY CODE IN NUMBER
                    String.format("0%s", message.user.phone)
                }
                else {
                    "Unknown Number"
                }
            }
            else {
                displayPhoneNumber
            }

            // Show incoming call
            callState = CallState.RINGING

            if (message != null) {
                pendingIncomingCallRoomID = message.room.roomID
                pendingIncomingCallPhoneNumber = phoneNumber

                // Trigger listener callback
                for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                    meetTalkListener.onIncomingCallReceived(activeCallInstanceKey, message)
                }
            }

            startMissedCallTimer()
            showIncomingCallNotification(activeCallInstanceKey!!, MeetTalk.appContext, name, phoneNumber)
        }

        fun showNativeIncomingCallScreen(message: TAPMessageModel?, displayName: String?, displayPhoneNumber: String?) {
            if (callState != CallState.IDLE) {
                return
            }
            val name: String
            if (!displayName.isNullOrEmpty() && message != null && activeCallInstanceKey != null) {
                getRoomAliasMap(activeCallInstanceKey!!)[message.room.roomID] = displayName!!
                name = message.user?.fullname ?: ""
            }
            else {
                name = displayName!!
            }
            val phoneNumber = if (displayPhoneNumber.isNullOrEmpty()) {
                if (message?.user != null) {
                    // TODO: HANDLE COUNTRY CODE IN NUMBER
                    String.format("0%s", message.user.phone)
                }
                else {
                    "Unknown Number"
                }
            }
            else {
                displayPhoneNumber
            }

            buildAndRegisterPhoneAccount()

            if (BuildConfig.DEBUG) {
                Log.e(">>>>", "showIncomingCall: obtainedPhoneAccount ${TAPUtils.toJsonString(getPhoneAccount())} isEnabled: ${getPhoneAccount()?.isEnabled}")
            }

            val extras = Bundle()
            val uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, phoneNumber, null)
            extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri)
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
            extras.putString(CALLER_NAME, name)
            extras.putString(CALLER_NUMBER, phoneNumber)
            try {
                // Show incoming call
                callState = CallState.RINGING
                telecomManager.addNewIncomingCall(phoneAccountHandle, extras)

                if (message != null) {
                    pendingIncomingCallRoomID = message.room.roomID
                    pendingIncomingCallPhoneNumber = phoneNumber

                    // Trigger listener callback
                    for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                        meetTalkListener.onIncomingCallReceived(activeCallInstanceKey, message)
                    }
                }

                startMissedCallTimer()
            }
            catch (e: SecurityException) {
                e.printStackTrace()
                // This PhoneAccountHandle is not enabled for this user
                callState = CallState.IDLE

                val errorMessage =
                    String.format(
                        MeetTalk.appContext.getString(R.string.meettalk_format_received_call_phone_account_not_enabled),
                        TAPUtils.getFirstWordOfString(name),
                        appName
                    )

                if (message != null) {
                    // Trigger listener callback
                    for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                        meetTalkListener.onShowIncomingCallFailed(activeCallInstanceKey, message, errorMessage)
                    }
                }

                Toast.makeText(
                    MeetTalk.appContext,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()

                sendUnableToReceiveCallNotification(
                    activeCallInstanceKey ?: return,
                    activeCallMessage?.room ?: return,
                    "{{sender}} has not enabled app's phone account."
                )
            }
            catch (e: Exception) {
                e.printStackTrace()
                // Other exceptions
                callState = CallState.IDLE

                if (message != null) {
                    // Trigger listener callback
                    for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                        meetTalkListener.onShowIncomingCallFailed(activeCallInstanceKey, message, e.message)
                    }
                }

                Toast.makeText(
                    MeetTalk.appContext,
                    String.format(
                        MeetTalk.appContext.getString(R.string.meettalk_format_unable_to_receive_call),
                        TAPUtils.getFirstWordOfString(name),
                        e.localizedMessage
                    ),
                    Toast.LENGTH_LONG
                ).show()

                sendUnableToReceiveCallNotification(
                    activeCallInstanceKey ?: return,
                    activeCallMessage?.room ?: return,
                    "{{sender}} is unable to receive call."
                )
            }
        }

        private fun createIncomingCallNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = MeetTalk.appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val notificationChannel = NotificationChannel(
                    INCOMING_CALL_NOTIFICATION_CHANNEL_ID,
                    INCOMING_CALL_NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )
                // TODO: CUSTOM RINGTONE URI
                val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                notificationChannel.setSound(
                    ringtoneUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                notificationChannel.description = INCOMING_CALL_NOTIFICATION_CHANNEL_DESCRIPTION
                notificationChannel.setShowBadge(true)
                notificationChannel.enableLights(true)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                notificationChannel.lightColor = MeetTalk.appContext.getColor(R.color.tapColorPrimary)

                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        fun showIncomingCallNotification(
            instanceKey: String,
            context: Context,
            notificationTitle: String,
            notificationContent: String
            //, activityClass: Class<Activity>
        ) {

//            // Create an intent which triggers your fullscreen incoming call user interface.
//            val intent = Intent(Intent.ACTION_MAIN, null)
//            intent.flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK
//            intent.setClass(context, MeetTalkIncomingCallActivity::class.java)
//            val pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_MUTABLE)
//
//            // Build the notification as an ongoing high priority item; this ensures it will show as
//            // a heads up notification which slides down over top of the current content.
////            val builder: Notification.Builder = Notification.Builder(context)
//            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, INCOMING_CALL_NOTIFICATION_CHANNEL_ID)
//            builder.setOngoing(true)
//            builder.priority = NotificationCompat.PRIORITY_MAX
//
//            // Set notification content intent to take user to fullscreen UI if user taps on the
//            // notification body.
//            builder.setContentIntent(pendingIntent)
//            // Set full screen intent to trigger display of the fullscreen UI when the notification
//            // manager deems it appropriate.
//            builder.setFullScreenIntent(pendingIntent, true)
//
//            // Setup notification content.
//            builder.setSmallIcon(TapTalk.getClientAppIcon(instanceKey))
//            builder.setContentTitle(notificationTitle)
//            builder.setContentText(notificationContent)
//
//            // Set notification as insistent to cause your ringtone to loop.
//            val notification: Notification = builder.build()
//            notification.flags = notification.flags or Notification.FLAG_INSISTENT
//
//            // Use builder.addAction(..) to add buttons to answer or reject the call.
//            val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
//            createIncomingCallNotificationChannel(context)
//            notificationManager.notify(INCOMING_CALL_NOTIFICATION_CHANNEL_ID, 0, notification)

            val incomingCallNotificationIntent = Intent(context, MeetTalkIncomingCallService::class.java)
            incomingCallNotificationIntent.putExtra(INCOMING_CALL_NOTIFICATION_TITLE, notificationTitle)
            incomingCallNotificationIntent.putExtra(INCOMING_CALL_NOTIFICATION_CONTENT, notificationContent)
            context.startService(incomingCallNotificationIntent)

            Log.e(">>>>>>>", "showIncomingCallNotification: ")
        }

        fun closeIncomingCallNotification(context: Context) {
            context.stopService(Intent(context, MeetTalkIncomingCallService::class.java))
        }

        fun answerIncomingCall() {
            if (activeCallInstanceKey == null) {
                return
            }
            for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                meetTalkListener.onIncomingCallAnswered()
            }
            clearPendingIncomingCall()
        }

        fun rejectIncomingCall() {
            if (activeCallInstanceKey == null) {
                return
            }
            for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                meetTalkListener.onIncomingCallRejected()
            }
            clearPendingIncomingCall()
        }

        fun checkAndRequestAudioPermission(activity: Activity) : Boolean {
            if (!TAPUtils.hasPermissions(activity, Manifest.permission.RECORD_AUDIO)) {
                // Check camera permission
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_PERMISSION_AUDIO
                )
                return false
            }
            return true
        }

        fun checkAndRequestCameraPermission(activity: Activity) : Boolean {
            if (!TAPUtils.hasPermissions(activity, Manifest.permission.CAMERA)) {
                // Check camera permission
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_PERMISSION_CAMERA
                )
                return false
            }
            return true
        }

        fun clearPendingIncomingCall() {
            callState = CallState.IDLE
            pendingIncomingCallRoomID = null
            pendingIncomingCallPhoneNumber = null
        }

        fun rejectPendingIncomingConferenceCall() {
            sendRejectedCallNotification(activeCallInstanceKey ?: return, activeCallMessage?.room ?: return)
            clearPendingIncomingCall()
        }

        fun joinPendingIncomingConferenceCall() : Boolean {
            if (pendingIncomingCallRoomID == null) {
                return false
            }
            sendAnsweredCallNotification(activeCallInstanceKey ?: return false, activeCallMessage?.room ?: return false)
            launchMeetTalkCallActivity(activeCallInstanceKey ?: return false, TapTalk.appContext)
            pendingIncomingCallRoomID = null
            pendingIncomingCallPhoneNumber = null
            return true
        }

        private fun closeIncomingCall() {
            if (activeMeetTalkIncomingCallActivity == null) {
                closeIncomingCallNotification(MeetTalk.appContext)
                clearPendingIncomingCall()
            }
            else {
                activeMeetTalkIncomingCallActivity!!.closeIncomingCall()
            }
        }

        fun initiateNewConferenceCall(activity: Activity, instanceKey: String, room: TAPRoomModel) {
            if (room.type != TYPE_PERSONAL) {
                // TODO: Temporarily disabled for non-personal rooms
                return
            }
            sendCallInitiatedNotification(instanceKey, room)
            launchMeetTalkCallActivity(instanceKey, activity)
        }

        fun initiateNewConferenceCall(activity: Activity, instanceKey: String, room: TAPRoomModel, recipientDisplayName: String) {
            getRoomAliasMap(instanceKey)[room.roomID] = recipientDisplayName
            initiateNewConferenceCall(activity, instanceKey, room)
        }

        fun launchMeetTalkCallActivity(instanceKey: String, context: Context) : Boolean {
            if (activeCallMessage == null ||
                activeCallInstanceKey == null ||
                activeConferenceInfo == null
            ) {
                return false
            }
            val activeUser = TapTalk.getTapTalkActiveUser(activeCallInstanceKey)
            return launchMeetTalkCallActivity(
                instanceKey,
                context,
                activeCallMessage!!.room,
                activeUser.fullname ?: "",
                activeUser.imageURL?.fullsize ?: ""
            )
        }

        fun launchMeetTalkCallActivity(
            instanceKey: String,
            context: Context,
            room: TAPRoomModel,
            activeUserName: String?,
            activeUserAvatarUrl: String?
        ) : Boolean {
            if (activeCallMessage == null ||
                activeCallInstanceKey == null ||
                activeConferenceInfo == null
            ) {
                return false
            }
            callState = CallState.IN_CALL
            val conferenceRoomID = String.format(
                "%s%s%s",
                MEET_ROOM_ID_PREFIX,
                MeetTalk.appID,
                room.roomID
            )
            val userInfo = JitsiMeetUserInfo()
            if (!activeUserAvatarUrl.isNullOrEmpty()) {
                try {
                    userInfo.avatar = URL(activeUserAvatarUrl)
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                }
            }
            if (!activeUserName.isNullOrEmpty()) {
                userInfo.displayName = activeUserName
            }
            if (!activeCallMessage?.user?.email.isNullOrEmpty()) {
                userInfo.email = activeCallMessage?.user?.email ?: ""
            }
            val options: JitsiMeetConferenceOptions = JitsiMeetConferenceOptions.Builder()
                .setRoom(conferenceRoomID)
                //.setWelcomePageEnabled(false)
                .setAudioMuted(defaultAudioMuted)
                .setVideoMuted(defaultVideoMuted)
                .setUserInfo(userInfo)
                .build()
            if (BuildConfig.DEBUG) {
                Log.e(">>>>", "launchMeetTalkCallActivity: ${options.room} - ${userInfo.displayName} - ${userInfo.avatar}")
            }
//            JitsiMeetActivity.launch(
//                context, options
//            )
            MeetTalkCallActivity.launch(
                instanceKey,
                context,
                options,
                activeCallMessage!!,
                activeConferenceInfo!!
            )
            return true
        }

        fun checkAndHandleCallNotificationFromMessage(message: TAPMessageModel, instanceKey: String, activeUser: TAPUserModel) {
            if (message.type != CALL_MESSAGE_TYPE ||
                handledCallNotificationMessageLocalIDs.contains(message.localID)
//                (message.action != CALL_INITIATED &&
//                MeetTalkConferenceInfo.fromMessageModel(message) == null)
            ) {
                // Return if:
                // • Message type is invalid
                // • Message has been previously handled
                return
            }
            if (BuildConfig.DEBUG) {
                Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: ${message.body} - ${TAPUtils.toJsonString(message.data)}")
            }
            handledCallNotificationMessageLocalIDs.add(message.localID)

            if (message.hidden == false &&
                message.action != CALL_ENDED &&
                message.action != CALL_CANCELLED &&
                message.action != RECIPIENT_BUSY &&
                message.action != RECIPIENT_REJECTED_CALL &&
                message.action != RECIPIENT_MISSED_CALL
            ) {
                // Mark invisible message as read
                TapCoreMessageManager.getInstance(instanceKey).markMessageAsRead(message)
            }

            if (message.action == CALL_INITIATED &&
                message.user.userID != activeUser.userID &&
                System.currentTimeMillis() - message.created < INCOMING_CALL_TIMEOUT_DURATION
            ) {
                if (callState == CallState.IDLE) {
                    if (message.data == null) {
                        // Received call initiated notification with no data, fetch data from API
                        if (BuildConfig.DEBUG) {
                            Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CALL_INITIATED - Fetch data from API")
                        }
                        TapCoreMessageManager.getInstance(instanceKey).getNewerMessagesAfterTimestamp(
                                message.room.roomID,
                                message.created,
                                message.created,
                                object : TapCoreGetMessageListener() {
                                    override fun onSuccess(messages: MutableList<TAPMessageModel>?) {
                                        if (!messages.isNullOrEmpty()) {
                                            for (newMessage in messages) {
                                                if (message.localID == newMessage.localID &&
                                                    newMessage.data != null
                                                ) {
                                                    message.data = newMessage.data
                                                    setActiveCallData(instanceKey, message)
                                                    // Trigger listener callback
                                                    for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                                                        meetTalkListener.onReceiveCallInitiatedNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                    }
                    else {
                        // Received call initiated notification, show incoming call
                        if (BuildConfig.DEBUG) {
                            Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CALL_INITIATED - Show incoming call")
                        }
                        setActiveCallData(instanceKey, message)
                        // Trigger listener callback
                        for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                            meetTalkListener.onReceiveCallInitiatedNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                        }
                    }
                } else {
                    // Send busy notification when a different call is received
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CALL_INITIATED - Recipient busy")
                    }
                    sendBusyNotification(instanceKey, message.room)

                    // Trigger listener callback
                    for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                        meetTalkListener.onReceiveCallInitiatedNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                    }
                }
            }
            else if (MeetTalkConferenceInfo.fromMessageModel(message)?.callID == activeConferenceInfo?.callID) {
                if (((message.action == CALL_CANCELLED && message.user.userID != activeUser.userID) ||
                    (message.action == RECIPIENT_REJECTED_CALL && message.user.userID == activeUser.userID))
                ) {
                    // Caller cancelled call or recipient rejected call elsewhere, dismiss incoming call
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: ${message.action}")
                    }
                    closeIncomingCallNotification(MeetTalk.appContext)
                    MeetTalkCallConnection.getInstance().onDisconnect()
                    activeMeetTalkCallActivity?.finish()
                    closeIncomingCall()
                    setActiveCallAsEnded()

                    // Trigger listener callback
                    if (message.action == CALL_CANCELLED) {
                        for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                            meetTalkListener.onReceiveCallCancelledNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                        }
                    }
                    else if (message.action == RECIPIENT_REJECTED_CALL) {
                        for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                            meetTalkListener.onReceiveActiveUserRejectedCallNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                        }
                    }
                }
                else if (message.action == CALL_ENDED) {
                    // A party ended the call, leave active call room
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CALL_ENDED $activeMeetTalkCallActivity")
                    }
                    activeMeetTalkCallActivity?.finish()
                    setActiveCallAsEnded()

                    // Trigger listener callback
                    for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                        meetTalkListener.onReceiveCallEndedNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                    }
                }
                else if (message.action == RECIPIENT_ANSWERED_CALL) {
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: RECIPIENT_ANSWERED_CALL is self: ${message.user.userID == activeUser.userID}")
                    }
                    if (message.user.userID == activeUser.userID) {
                        // Recipient answered call elsewhere, dismiss incoming call
                        MeetTalkCallConnection.getInstance().onDisconnect()
                        closeIncomingCall()
                        callState = CallState.IDLE
                    }

                    // Trigger listener callback
                    for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                        meetTalkListener.onReceiveRecipientAnsweredCallNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                    }
                }
                else if (message.action == PARTICIPANT_JOINED_CONFERENCE) {
                    // A participant successfully joined the call, notify call activity
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: PARTICIPANT_JOINED_CONFERENCE ${message.user.fullname}")
                    }
                    val updatedConferenceInfo = MeetTalkConferenceInfo.fromMessageModel(message)
                    if (activeConferenceInfo != null && updatedConferenceInfo != null) {
                        activeConferenceInfo?.updateValue(updatedConferenceInfo)
                        activeMeetTalkCallActivity?.onConferenceInfoUpdated(activeConferenceInfo!!)
                    }

                    // Trigger listener callback
                    for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                        meetTalkListener.onReceiveParticipantJoinedConferenceNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                    }
                }
                else if (message.action == PARTICIPANT_LEFT_CONFERENCE) {
                    // A participant left the call
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: PARTICIPANT_LEFT_CONFERENCE ${message.user.fullname}")
                    }
                    val updatedConferenceInfo = MeetTalkConferenceInfo.fromMessageModel(message)
                    if (activeConferenceInfo != null && updatedConferenceInfo != null) {
                        activeConferenceInfo?.updateValue(updatedConferenceInfo)
                        activeMeetTalkCallActivity?.onConferenceInfoUpdated(activeConferenceInfo!!)
                    }

                    // Trigger listener callback
                    for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                        meetTalkListener.onReceiveParticipantLeftConferenceNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                    }
                }
                else if (
                    message.user.userID != activeUser.userID &&
                    (message.action == RECIPIENT_BUSY ||
                    message.action == RECIPIENT_REJECTED_CALL ||
                    message.action == RECIPIENT_MISSED_CALL)
                ) {
                    // Recipient did not join call, leave call room
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: ${message.action}")
                    }
                    activeMeetTalkCallActivity?.finish()
                    setActiveCallAsEnded()

                    // Trigger listener callback
                    if (message.action == RECIPIENT_BUSY) {
                        for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                            meetTalkListener.onReceiveRecipientBusyNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                        }
                    }
                    else if (message.action == RECIPIENT_REJECTED_CALL) {
                        for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                            meetTalkListener.onReceiveRecipientRejectedCallNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                        }
                    }
                    else if (message.action == RECIPIENT_MISSED_CALL) {
                        for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                            meetTalkListener.onReceiveRecipientMissedCallNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                        }
                    }
                }
                else if (
                    message.action == CONFERENCE_INFO &&
                    message.user.userID != activeUser.userID
                ) {
                    if (BuildConfig.DEBUG) {
                        Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CONFERENCE_INFO - update activity")
                    }
                    // Received updated conference info
                    val updatedConferenceInfo = MeetTalkConferenceInfo.fromMessageModel(message)
                    if (activeConferenceInfo != null && updatedConferenceInfo != null) {
                        activeConferenceInfo?.updateValue(updatedConferenceInfo)
                        activeMeetTalkCallActivity?.retrieveParticipantsInfo()
                        activeMeetTalkCallActivity?.onConferenceInfoUpdated(activeConferenceInfo!!)
                    }

                    // Trigger listener callback
                    for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                        meetTalkListener.onReceiveConferenceInfoUpdatedNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                    }
                }
                else if (message.action == RECIPIENT_UNABLE_TO_RECEIVE_CALL) {
                    // One of recipient's device is unable to receive the call
                    // TODO:

                    // Trigger listener callback
                    for (meetTalkListener in MeetTalk.getMeetTalkListeners(activeCallInstanceKey)) {
                        meetTalkListener.onReceiveRecipientUnableToReceiveCallNotificationMessage(instanceKey, message, MeetTalkConferenceInfo.fromMessageModel(message))
                    }
                }
            }
        }

        private fun generateCallNotificationMessage(
            instanceKey: String,
            room: TAPRoomModel,
            body: String,
            action: String
        ) : TAPMessageModel {
            val activeUser = TapTalk.getTapTalkActiveUser(instanceKey)
            //val updatedBody = body.replace("{{sender}}", TAPUtils.getFirstWordOfString(activeUser.fullname))

            val notificationMessage = TAPMessageModel.Builder(
                body,
                room,
                CALL_MESSAGE_TYPE,
                System.currentTimeMillis(),
                activeUser,
                if (room.type == TYPE_PERSONAL) {
                    TAPChatManager.getInstance(instanceKey).getOtherUserIdFromRoom(room.roomID)
                }
                else {
                    "0"
                },
                null
            )
            notificationMessage.action = action

            return notificationMessage
        }

        fun generateParticipantInfo(instanceKey: String, role: String) : MeetTalkParticipantInfo {
            val activeUser = TapTalk.getTapTalkActiveUser(instanceKey)
            return MeetTalkParticipantInfo(
                activeUser.userID,
                "",
                activeUser.fullname,
                activeUser.imageURL.fullsize,
                role,
                0L,
                System.currentTimeMillis(),
                defaultAudioMuted,
                defaultVideoMuted
            )
        }

        private fun setMessageConferenceInfoAsEnded(message: TAPMessageModel) : TAPMessageModel {
            val conferenceInfo = activeConferenceInfo?.copy()
            if (conferenceInfo != null) {
                conferenceInfo.callEndedTime = message.created
                conferenceInfo.lastUpdated = message.created
                if (conferenceInfo.callStartedTime > 0L) {
                    conferenceInfo.callDuration = conferenceInfo.callEndedTime - conferenceInfo.callStartedTime
                }
                conferenceInfo.attachToMessage(message)
                message.filterID = conferenceInfo.callID
            }
            return message
        }

        fun sendCallInitiatedNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            val message = generateCallNotificationMessage(instanceKey, room, "{{sender}} started call.", CALL_INITIATED)
            val participants: ArrayList<MeetTalkParticipantInfo> = ArrayList()
            val host = generateParticipantInfo(instanceKey, HOST)
            participants.add(host)
            val newConferenceInfo = MeetTalkConferenceInfo(
                message.localID,
                message.room.roomID,
                message.user.userID,
                message.created,
                0L,
                0L,
                0L,
                message.created,
                participants
            )
            newConferenceInfo.attachToMessage(message)
            message.filterID = newConferenceInfo.callID

            setActiveCallData(instanceKey, message)
            sendCallNotificationMessage(instanceKey, message)

            return message
        }

        fun sendCallCancelledNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            var message = generateCallNotificationMessage(instanceKey, room, "{{sender}} cancelled call.", CALL_CANCELLED)
            message = setMessageConferenceInfoAsEnded(message)

            setActiveCallAsEnded()

            sendCallNotificationMessage(instanceKey, message)

            return message
        }

        fun sendCallEndedNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            var message = generateCallNotificationMessage(instanceKey, room, "{{sender}} ended call.", CALL_ENDED)
            message = setMessageConferenceInfoAsEnded(message)

            setActiveCallAsEnded()

            sendCallNotificationMessage(instanceKey, message)

            return message
        }

        fun sendAnsweredCallNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            val message = generateCallNotificationMessage(instanceKey, room, "{{sender}} answered call.", RECIPIENT_ANSWERED_CALL)
            activeConferenceInfo?.attachToMessage(message)
            //message.hidden = true

            sendCallNotificationMessage(instanceKey, message)

            return message
        }

        fun sendJoinedCallNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            val message = generateCallNotificationMessage(instanceKey, room, "{{sender}} joined call.", PARTICIPANT_JOINED_CONFERENCE)
            val participant = generateParticipantInfo(instanceKey, PARTICIPANT)
            activeConferenceInfo?.updateParticipant(participant)
            activeConferenceInfo?.lastUpdated = message.created
            activeConferenceInfo?.attachToMessage(message)
            message.hidden = true

            sendCallNotificationMessage(instanceKey, message)

            return message
        }

        fun sendLeftCallNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            val message = generateCallNotificationMessage(instanceKey, room, "{{sender}} left call.", PARTICIPANT_LEFT_CONFERENCE)

            activeConferenceInfo?.lastUpdated = message.created
            activeConferenceInfo?.attachToMessage(message)
            message.hidden = true

            sendCallNotificationMessage(instanceKey, message)

            return message
        }

        fun sendBusyNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            var message = generateCallNotificationMessage(instanceKey, room, "{{sender}} is busy.", RECIPIENT_BUSY)
            message = setMessageConferenceInfoAsEnded(message)

            setActiveCallAsEnded()

            sendCallNotificationMessage(instanceKey, message)

            return message
        }

        fun sendRejectedCallNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            var message = generateCallNotificationMessage(instanceKey, room, "{{sender}} rejected call.", RECIPIENT_REJECTED_CALL)
            message = setMessageConferenceInfoAsEnded(message)

            setActiveCallAsEnded()

            sendCallNotificationMessage(instanceKey, message)

            return message
        }

        fun sendMissedCallNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            var message = generateCallNotificationMessage(instanceKey, room, "{{sender}} missed the call.", RECIPIENT_MISSED_CALL)
            message = setMessageConferenceInfoAsEnded(message)

            setActiveCallAsEnded()

            sendCallNotificationMessage(instanceKey, message)

            return message
        }

        fun sendUnableToReceiveCallNotification(instanceKey: String, room: TAPRoomModel, body: String) : TAPMessageModel {
            val message = generateCallNotificationMessage(instanceKey, room, body, RECIPIENT_UNABLE_TO_RECEIVE_CALL)
            message.hidden = true

            setActiveCallAsEnded()

            sendCallNotificationMessage(instanceKey, message)

            return message
        }

        fun sendConferenceInfoNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel? {
            if (activeConferenceInfo == null) {
                return null
            }
            val message = generateCallNotificationMessage(instanceKey, room, "Call info updated.", CONFERENCE_INFO)
            activeConferenceInfo?.lastUpdated = message.created
            activeConferenceInfo?.attachToMessage(message)
            message.hidden = true

            sendCallNotificationMessage(instanceKey, message)

            return message
        }

        fun sendPendingCallNotificationMessages() {
            if (BuildConfig.DEBUG) {
                Log.e(">>>>", "sendPendingCallNotificationMessages size: ${pendingCallNotificationMessages.size}")
            }
            val pendingMessagesCopy = ArrayList<TAPMessageModel>()
            pendingMessagesCopy.addAll(pendingCallNotificationMessages)
            for (message in pendingMessagesCopy) {
                val messageCopy = message.copyMessageModel()
                pendingCallNotificationMessages.remove(message)
                sendCallNotificationMessage(
                    activeCallInstanceKey ?: pendingNotificationMessageInstanceKey ?: return,
                    messageCopy
                )
            }
            pendingNotificationMessageInstanceKey = null
        }

        // TODO: ADD CALLBACK
        private fun sendCallNotificationMessage(instanceKey: String, message: TAPMessageModel) {
            if (TapTalk.isConnected(instanceKey)) {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>", "sendCallNotificationMessage: ${message.action}")
                }
                TapCoreMessageManager.getInstance(instanceKey).sendCustomMessage(message, object : TapCoreSendMessageListener() {
                    override fun onStart(message: TAPMessageModel?) {

                    }

                    override fun onSuccess(message: TAPMessageModel?) {

                    }

                    override fun onError(message: TAPMessageModel?, errorCode: String?, errorMessage: String?) {

                    }
                })
            }
            else {
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>", "sendCallNotificationMessage add to pending array: ${message.action}")
                }
                pendingCallNotificationMessages.add(message)
                if (activeCallInstanceKey == null) {
                    // Save instance key in case call is already ended
                    pendingNotificationMessageInstanceKey = instanceKey
                }
            }
        }

        fun setActiveCallData(instanceKey: String, message: TAPMessageModel) {
            activeCallMessage = message
            activeConferenceInfo = MeetTalkConferenceInfo.fromMessageModel(message)
            activeCallInstanceKey = instanceKey
            TAPConnectionManager.getInstance(instanceKey).addSocketListener(socketListener)
        }

        fun setActiveCallAsEnded() {
            //TAPConnectionManager.getInstance(activeCallInstanceKey).removeSocketListener(socketListener)
            pendingCallNotificationMessages.clear()
            activeCallMessage = null
            activeConferenceInfo = null
            activeCallInstanceKey = null
            callState = CallState.IDLE
        }

        private fun startMissedCallTimer() {
            if (activeCallMessage == null) {
                return
            }

            missedCallTimer = Timer()

            val timerTask: TimerTask
            timerTask = object : TimerTask() {
                override fun run() {
                    if (callState == CallState.RINGING) {
                        if (System.currentTimeMillis() - activeCallMessage!!.created > INCOMING_CALL_TIMEOUT_DURATION) {
                            // Send missed call notification
                            sendMissedCallNotification(
                                activeCallInstanceKey ?: return,
                                activeCallMessage!!.room
                            )
                            MeetTalkCallConnection.getInstance().onDisconnect()
                            closeIncomingCall()
                            callState = CallState.IDLE
                        }
                    }
                    else {
                        missedCallTimer.cancel()
                    }
                }
            }
            missedCallTimer.schedule(timerTask, 0, 1000)
        }

        fun getRoomAliasMap(instanceKey: String) : HashMap<String, String> {
            if (roomAliasMap[instanceKey] == null) {
                roomAliasMap[instanceKey] = HashMap()
            }
            return roomAliasMap[instanceKey]!!
        }
    }
}
