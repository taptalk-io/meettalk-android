package io.taptalk.meettalk.manager

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import io.taptalk.TapTalk.Const.TAPDefaultConstant.RoomType.TYPE_PERSONAL
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.TapTalk.Helper.TapTalkDialog
import io.taptalk.TapTalk.Manager.TAPChatManager
import io.taptalk.TapTalk.Manager.TapCoreMessageManager
import io.taptalk.TapTalk.Model.TAPMessageModel
import io.taptalk.TapTalk.Model.TAPRoomModel
import io.taptalk.TapTalk.Model.TAPUserModel
import io.taptalk.meettalk.BuildConfig
import io.taptalk.meettalk.R
import io.taptalk.meettalk.activity.MeetTalkCallActivity
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.CALL_CANCELLED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.CALL_ENDED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.CALL_INITIATED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.CONFERENCE_INFO
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.TARGET_ANSWERED_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.TARGET_BUSY
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.TARGET_JOINED_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.TARGET_MISSED_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.TARGET_REJECTED_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.TARGET_UNABLE_TO_RECEIVE_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.CALL_MESSAGE_TYPE
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.CALLER_NAME
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.CALLER_NUMBER
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
import io.taptalk.meettalk.constant.MeetTalkConstant.Url.MEET_URL
import io.taptalk.meettalk.helper.MeetTalk
import io.taptalk.meettalk.helper.MeetTalkCallConnection
import io.taptalk.meettalk.helper.MeetTalkConnectionService
import io.taptalk.meettalk.model.MeetTalkConferenceInfo
import io.taptalk.meettalk.model.MeetTalkParticipantInfo
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.MalformedURLException
import java.net.URL

@RequiresApi(Build.VERSION_CODES.M)
class MeetTalkCallManager {

    companion object {
        enum class CallState {
            IDLE,
            RINGING, // Incoming call received
            IN_CALL, // In outgoing call or accepted incoming call
        }

        var callState = CallState.IDLE
        var activeMeetTalkCallActivity: MeetTalkCallActivity? = null
        var activeConferenceInfo: MeetTalkConferenceInfo? = null

        private val appName = TapTalk.appContext.getString(R.string.app_name)
        private val telecomManager = TapTalk.appContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        private val phoneAccountHandle = PhoneAccountHandle(ComponentName(TapTalk.appContext, MeetTalkConnectionService::class.java), appName)
        private val defaultAudioMuted = BuildConfig.DEBUG
        private const val defaultVideoMuted = true
        private var activeCallMessage: TAPMessageModel? = null
        private var activeCallInstanceKey: String? = null
        private var pendingIncomingCallRoomID: String? = null
        private var pendingIncomingCallPhoneNumber: String? = null
        private var handledCallNotificationMessageLocalIDs: ArrayList<String> = ArrayList()

        init {
            // Initialize Jitsi Meet
            val serverURL: URL = try {
                URL(MEET_URL)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                throw RuntimeException("Invalid server URL!")
            }

            val defaultOptions = JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                .setWelcomePageEnabled(false)
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
        }

        fun checkAndRequestEnablePhoneAccountSettings(instanceKey: String, activity: Activity) {
            if (MeetTalkDataManager.getInstance(instanceKey).getEnablePhoneAccountSettingsRequestedAppName() == appName ||
                getPhoneAccount()?.isEnabled == true
            ) {
                return
            }
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

        fun buildAndRegisterPhoneAccount() {
            if (getPhoneAccount() == null) {
                val phoneAccount = PhoneAccount.builder(phoneAccountHandle, appName)
                    .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER).build()
                telecomManager.registerPhoneAccount(phoneAccount)
            }
        }

        fun getPhoneAccount() : PhoneAccount? {
            return telecomManager.getPhoneAccount(phoneAccountHandle)
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

        private fun showIncomingCall(message: TAPMessageModel) {
            val name = message.user.fullname
            val phoneNumber = String.format("0%s", message.user.phone) // TODO: HANDLE COUNTRY CODE IN NUMBER
            Log.e(">>>>", "showIncomingCall: add new incoming call $name $phoneNumber")
            buildAndRegisterPhoneAccount()

            Log.e(">>>>", "showIncomingCall: obtainedPhoneAccount ${TAPUtils.toJsonString(
                getPhoneAccount())} isEnabled: ${getPhoneAccount()?.isEnabled}")

            val extras = Bundle()
            val uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, phoneNumber, null)
            extras.putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri)
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
            extras.putString(CALLER_NAME, name)
            extras.putString(CALLER_NUMBER, phoneNumber)
            try {
                // Show incoming call
                telecomManager.addNewIncomingCall(phoneAccountHandle, extras)

                pendingIncomingCallRoomID = message.room.roomID
                pendingIncomingCallPhoneNumber = phoneNumber

                // TODO: START COUNTDOWN TIMER FOR MISSED CALL
            }
            catch (e: SecurityException) {
                e.printStackTrace()
                // This PhoneAccountHandle is not enabled for this user
                sendUnableToReceiveCallNotification(
                    activeCallInstanceKey ?: return,
                    activeCallMessage?.room ?: return,
                    "{{user}} has not enabled app's phone account."
                )
                Toast.makeText(
                    MeetTalk.appContext,
                    String.format(
                        MeetTalk.appContext.getString(R.string.meettalk_format_received_call_phone_account_not_enabled),
                        TAPUtils.getFirstWordOfString(message.user.fullname),
                        appName
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
            catch (e: Exception) {
                e.printStackTrace()
                // Other exceptions
                sendUnableToReceiveCallNotification(
                    activeCallInstanceKey ?: return,
                    activeCallMessage?.room ?: return,
                    "{{user}} is unable to receive call."
                )
                Toast.makeText(
                    MeetTalk.appContext,
                    String.format(
                        MeetTalk.appContext.getString(R.string.meettalk_format_unable_to_receive_call),
                        TAPUtils.getFirstWordOfString(message.user.fullname),
                        e.localizedMessage
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
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

        fun joinPendingIncomingConferenceCall() {
            sendAnsweredCallNotification(activeCallInstanceKey ?: return, activeCallMessage?.room ?: return)
            launchMeetTalkCallActivity(activeCallInstanceKey!!, TapTalk.appContext, pendingIncomingCallRoomID ?: return)
            pendingIncomingCallRoomID = null
            pendingIncomingCallPhoneNumber = null
        }

        fun initiateNewConferenceCall(activity: Activity, instanceKey: String, room: TAPRoomModel) {
            sendCallInitiatedNotification(instanceKey, room)
            launchMeetTalkCallActivity(instanceKey, activity, room.roomID)
        }

        private fun launchMeetTalkCallActivity(instanceKey: String, context: Context, roomID: String) {
            if (activeCallMessage == null ||
                activeConferenceInfo == null
            ) {
                return
            }
            Log.e(">>>>", "launchMeetTalkCallActivity: $roomID")
            callState = CallState.IN_CALL
            val room = String.format("%s%s", MeetTalk.appID, roomID)
            val userInfo = JitsiMeetUserInfo()
            userInfo.avatar = URL(activeCallMessage?.user?.imageURL?.fullsize ?: "")
            userInfo.displayName = activeCallMessage?.user?.fullname ?: ""
            userInfo.email = activeCallMessage?.user?.email ?: ""
            val options: JitsiMeetConferenceOptions = JitsiMeetConferenceOptions.Builder()
                .setRoom(room)
                .setWelcomePageEnabled(false)
                .setAudioMuted(defaultAudioMuted)
                .setVideoMuted(defaultVideoMuted)
                .setUserInfo(userInfo)
                .build()
            MeetTalkCallActivity.launch(
                instanceKey,
                context,
                options,
                activeCallMessage!!,
                activeConferenceInfo!!
            )
        }

        fun checkAndHandleCallNotificationFromMessage(message: TAPMessageModel, instanceKey: String, activeUser: TAPUserModel) {
            if (message.type != CALL_MESSAGE_TYPE || handledCallNotificationMessageLocalIDs.contains(message.localID)) {
                // Return if message type is invalid or has been previously checked
                return
            }
            Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: ${message.body} - ${TAPUtils.toJsonString(message.data)}")
            handledCallNotificationMessageLocalIDs.add(message.localID)
            if (message.action == CALL_INITIATED && message.user.userID != activeUser.userID) {
                if (callState == CallState.IDLE) {
                    // Received call initiated notification, show incoming call
                    Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CALL_INITIATED - Show incoming call")
                    activeCallMessage = message
                    activeConferenceInfo = MeetTalkConferenceInfo.fromMessageModel(message)
                    activeCallInstanceKey = instanceKey
                    showIncomingCall(message)
                    callState = CallState.RINGING
                } else {
                    // Send busy notification when a different call is received
                    Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CALL_INITIATED - Target busy")
                    sendBusyNotification(instanceKey, message.room)
                }
            }
            else if ((message.action == CALL_CANCELLED && message.user.userID != activeUser.userID) ||
                (message.action == TARGET_REJECTED_CALL && message.user.userID == activeUser.userID)
            ) {
                // Caller canceled call or target rejected call elsewhere, dismiss incoming call
                Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: ${message.action}")
                MeetTalkCallConnection.getInstance().onDisconnect()
                activeMeetTalkCallActivity?.finish()
                activeCallMessage = null
                activeConferenceInfo = null
                activeCallInstanceKey = null
                callState = CallState.IDLE
            }
            else if (message.action == CALL_ENDED) {
                // A party ended the call, leave active call room
                Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CALL_ENDED $activeMeetTalkCallActivity")
                activeMeetTalkCallActivity?.finish()
                activeCallMessage = null
                activeConferenceInfo = null
                activeCallInstanceKey = null
                callState = CallState.IDLE
            }
            else if (message.action == TARGET_ANSWERED_CALL) {
                Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: TARGET_ANSWERED_CALL is self: ${message.user.userID == activeUser.userID}")
                if (message.user.userID == activeUser.userID) {
                    // Target answered call elsewhere, dismiss incoming call
                    MeetTalkCallConnection.getInstance().onDisconnect()
                    callState = CallState.IDLE
                }
            }
            else if (message.action == TARGET_JOINED_CALL) {
                // A participant successfully joined the call, notify call activity
                Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: TARGET_JOINED_CALL ${message.user.fullname}")
                val updatedConferenceInfo = MeetTalkConferenceInfo.fromMessageModel(message)
                if (activeConferenceInfo != null && updatedConferenceInfo != null) {
                    activeConferenceInfo?.updateValue(updatedConferenceInfo)
                    activeMeetTalkCallActivity?.onConferenceInfoUpdated(activeConferenceInfo!!)
                }
            }
            else if (message.user.userID != activeUser.userID &&
                (message.action == TARGET_BUSY ||
                message.action == TARGET_REJECTED_CALL ||
                message.action == TARGET_MISSED_CALL)
            ) {
                // Target did not join call, leave call room & dismiss waiting screen
                Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: TARGET_BUSY | TARGET_REJECTED_CALL | TARGET_MISSED_CALL")
                // TODO: NOTIFY USER
                activeMeetTalkCallActivity?.finish()
                activeCallMessage = null
                activeConferenceInfo = null
                activeCallInstanceKey = null
                callState = CallState.IDLE
            }
            else if (message.action == CONFERENCE_INFO) {
                if (message.user.userID != activeUser.userID) {
                    Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CONFERENCE_INFO - update activity")
                    // Received updated conference info
                    val updatedConferenceInfo = MeetTalkConferenceInfo.fromMessageModel(message)
                    if (activeConferenceInfo != null && updatedConferenceInfo != null) {
                        activeConferenceInfo?.updateValue(updatedConferenceInfo)
                        activeMeetTalkCallActivity?.onConferenceInfoUpdated(activeConferenceInfo!!)
                    }
                }
            }
            else if (message.action == TARGET_UNABLE_TO_RECEIVE_CALL) {
                // One of target's device is unable to receive the call
                // TODO:
            }
        }

        private fun generateCallNotificationMessage(
            instanceKey: String,
            room: TAPRoomModel,
            body: String,
            action: String
        ) : TAPMessageModel {
            val activeUser = TapTalk.getTapTalkActiveUser(instanceKey)
            val updatedBody = body.replace("{{user}}", TAPUtils.getFirstWordOfString(activeUser.fullname))

            val notificationMessage = TAPMessageModel.Builder(
                updatedBody,
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
                System.currentTimeMillis(),
                defaultAudioMuted,
                defaultVideoMuted
            )
        }

        private fun setMessageConferenceInfoAsEnded(message: TAPMessageModel) : TAPMessageModel {
            val conferenceInfo = activeConferenceInfo?.copy()
            if (conferenceInfo != null) {
                conferenceInfo.callEndedTime = message.created
                if (conferenceInfo.callStartedTime > 0L) {
                    conferenceInfo.callDuration = conferenceInfo.callEndedTime - conferenceInfo.callStartedTime
                }
                message.data = conferenceInfo.toHashMap()
                message.filterID = conferenceInfo.callID
            }
            return message
        }

        fun sendCallInitiatedNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            val message = generateCallNotificationMessage(instanceKey, room, "{{user}} started call.", CALL_INITIATED)
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
                participants
            )
            message.data = newConferenceInfo.toHashMap()
            message.filterID = newConferenceInfo.callID

            activeCallMessage = message
            activeConferenceInfo = MeetTalkConferenceInfo.fromMessageModel(message)
            activeCallInstanceKey = instanceKey

            TapCoreMessageManager.getInstance(instanceKey).sendCustomMessage(message, null)

            return message
        }

        fun sendCallCanceledNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            var message = generateCallNotificationMessage(instanceKey, room, "{{user}} canceled call.", CALL_CANCELLED)
            message = setMessageConferenceInfoAsEnded(message)

            activeCallMessage = null
            activeConferenceInfo = null
            activeCallInstanceKey = null

            TapCoreMessageManager.getInstance(instanceKey).sendCustomMessage(message, null)

            return message
        }

        fun sendCallEndedNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            var message = generateCallNotificationMessage(instanceKey, room, "{{user}} ended call.", CALL_ENDED)
            message = setMessageConferenceInfoAsEnded(message)

            activeCallMessage = null
            activeConferenceInfo = null
            activeCallInstanceKey = null

            TapCoreMessageManager.getInstance(instanceKey).sendCustomMessage(message, null)

            return message
        }

        fun sendAnsweredCallNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            val message = generateCallNotificationMessage(instanceKey, room, "{{user}} answered call.", TARGET_ANSWERED_CALL)
            message.data = activeConferenceInfo?.toHashMap()
            message.hidden = true

            TapCoreMessageManager.getInstance(instanceKey).sendCustomMessage(message, null)

            return message
        }

        fun sendJoinedCallNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            val message = generateCallNotificationMessage(instanceKey, room, "{{user}} joined call.", TARGET_JOINED_CALL)
            val participant = generateParticipantInfo(instanceKey, PARTICIPANT)
            activeConferenceInfo?.updateParticipant(participant)
            message.data = activeConferenceInfo?.toHashMap()
            message.hidden = true

            TapCoreMessageManager.getInstance(instanceKey).sendCustomMessage(message, null)

            return message
        }

        fun sendBusyNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            var message = generateCallNotificationMessage(instanceKey, room, "{{user}} is busy.", TARGET_BUSY)
            message = setMessageConferenceInfoAsEnded(message)

            activeCallMessage = null
            activeConferenceInfo = null
            activeCallInstanceKey = null

            TapCoreMessageManager.getInstance(instanceKey).sendCustomMessage(message, null)

            return message
        }

        fun sendRejectedCallNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            var message = generateCallNotificationMessage(instanceKey, room, "{{user}} rejected call.", TARGET_REJECTED_CALL)
            message = setMessageConferenceInfoAsEnded(message)

            activeCallMessage = null
            activeConferenceInfo = null
            activeCallInstanceKey = null

            TapCoreMessageManager.getInstance(instanceKey).sendCustomMessage(message, null)

            return message
        }

        fun sendMissedCallNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            var message = generateCallNotificationMessage(instanceKey, room, "{{user}} missed the call.", TARGET_MISSED_CALL)
            message = setMessageConferenceInfoAsEnded(message)

            activeCallMessage = null
            activeConferenceInfo = null
            activeCallInstanceKey = null

            TapCoreMessageManager.getInstance(instanceKey).sendCustomMessage(message, null)

            return message
        }

        fun sendUnableToReceiveCallNotification(instanceKey: String, room: TAPRoomModel, body: String) : TAPMessageModel {
            var message = generateCallNotificationMessage(instanceKey, room, body, TARGET_UNABLE_TO_RECEIVE_CALL)
            message = setMessageConferenceInfoAsEnded(message)
            message.hidden = true

            activeCallMessage = null
            activeConferenceInfo = null
            activeCallInstanceKey = null

            TapCoreMessageManager.getInstance(instanceKey).sendCustomMessage(message, null)

            return message
        }

        fun sendConferenceInfoNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel? {
            if (activeConferenceInfo == null) {
                return null
            }
            val message = generateCallNotificationMessage(instanceKey, room, "Call info updated.", CONFERENCE_INFO)
            message.data = activeConferenceInfo!!.toHashMap()
            message.hidden = true

            TapCoreMessageManager.getInstance(instanceKey).sendCustomMessage(message, null)

            return message
        }
    }
}
