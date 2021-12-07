package io.taptalk.meettalk.manager

import android.app.Activity
import android.content.BroadcastReceiver
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
import androidx.annotation.RequiresApi
import io.taptalk.TapTalk.Const.TAPDefaultConstant.RoomType.TYPE_PERSONAL
import io.taptalk.TapTalk.Helper.*
import io.taptalk.TapTalk.Manager.TAPChatManager
import io.taptalk.TapTalk.Manager.TapCoreMessageManager
import io.taptalk.TapTalk.Model.TAPMessageModel
import io.taptalk.TapTalk.Model.TAPRoomModel
import io.taptalk.TapTalk.Model.TAPUserModel
import io.taptalk.meettalk.R
import io.taptalk.meettalk.activity.MeetTalkCallActivity
import io.taptalk.meettalk.constant.MeetTalkConstant.BroadcastEvent.ACTIVE_USER_LEAVES_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.CALL_CANCELLED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.CALL_ENDED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.CALL_INITIATED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.TARGET_BUSY
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.TARGET_JOINED_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.TARGET_MISSED_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.TARGET_REJECTED_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.TARGET_UNABLE_TO_RECEIVE_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.CALLER_NAME
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.CALLER_NUMBER
import io.taptalk.meettalk.helper.TapCallConnection
import io.taptalk.meettalk.helper.TapConnectionService
import org.jitsi.meet.sdk.JitsiMeet
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import java.net.MalformedURLException
import java.net.URL

@RequiresApi(Build.VERSION_CODES.M)
class TapCallManager {

    companion object {
        enum class CallState {
            IDLE,
            RINGING, // Incoming call received
            IN_CALL, // In outgoing call or accepted incoming call
        }

        var callState = CallState.IDLE
        var activeMeetTalkCallActivity: MeetTalkCallActivity? = null
//        var activeWaitingScreenActivity: TapCallWaitingScreenActivity? = null

        private val appName = TapTalk.appContext.getString(R.string.app_name)
        private val telecomManager = TapTalk.appContext.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        private val phoneAccountHandle = PhoneAccountHandle(ComponentName(TapTalk.appContext, TapConnectionService::class.java), appName)
        private var activeCallMessage: TAPMessageModel? = null
        private var activeCallInstanceKey: String? = null
        private var pendingIncomingCallRoomName: String? = null
        private var pendingIncomingCallPhoneNumber: String? = null

        init {
            // Initialize Jitsi Meet
            val serverURL: URL = try {
                URL("https://meet.taptalk.io") // TODO: MOVE URL TO MeetTalk.init() ?
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                throw RuntimeException("Invalid server URL!")
            }
            val defaultOptions: JitsiMeetConferenceOptions = JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                .setWelcomePageEnabled(false)
                .build()
            JitsiMeet.setDefaultConferenceOptions(defaultOptions)

            buildAndRegisterPhoneAccount()

            registerBroadcastReceiver()
        }

        private fun registerBroadcastReceiver() {
            TAPBroadcastManager.register(TapTalk.appContext, object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == ACTIVE_USER_LEAVES_CALL) {
                        Log.e(">>>>", "onReceive ACTIVE_USER_LEAVES_CALL: Call ended")
                        sendCallEndedNotification(activeCallInstanceKey ?: return, activeCallMessage?.room ?: return)
                        callState = CallState.IDLE
                    }
//                    if (intent?.action == SHOW_WAITING_SCREEN) {
//                        Log.e(">>>>", "onReceive SHOW_WAITING_SCREEN")
//                        TapCallWaitingScreenActivity.start(activeMeetTalkCallActivity ?: return, activeCallInstanceKey ?: return, activeCallMessage ?: return)
//                    }
                }
            }, ACTIVE_USER_LEAVES_CALL/*, ACTIVE_USER_CANCELS_CALL, SHOW_WAITING_SCREEN*/)
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

        private fun showIncomingCall(name: String, phoneNumber: String) {
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
                telecomManager.addNewIncomingCall(phoneAccountHandle, extras)
            } catch (e: SecurityException) {
                e.printStackTrace()
                // This PhoneAccountHandle is not enabled for this user
                sendUnableToReceiveCallNotification(activeCallInstanceKey ?: return, activeCallMessage?.room ?: return, "{{user}} has not enabled app's phone account.")
            }

            pendingIncomingCallRoomName = TAPUtils.getFirstWordOfString(name)
            pendingIncomingCallPhoneNumber = phoneNumber

            // TODO: START COUNTDOWN TIMER FOR MISSED CALL
        }

        fun clearPendingIncomingCall() {
            callState = CallState.IDLE
            pendingIncomingCallRoomName = null
            pendingIncomingCallPhoneNumber = null
        }

        fun joinPendingIncomingConferenceCall() {
            sendJoinedCallNotification(activeCallInstanceKey ?: return, activeCallMessage?.room ?: return)
            startConferenceCall(TapTalk.appContext, pendingIncomingCallRoomName ?: return, false)
            pendingIncomingCallRoomName = null
            pendingIncomingCallPhoneNumber = null
        }

        fun initiateNewConferenceCall(activity: Activity, instanceKey: String, room: TAPRoomModel) {
            /*val callInitiatedMessage = */sendCallInitiatedNotification(instanceKey, room)
            startConferenceCall(activity, TAPUtils.getFirstWordOfString(TapTalk.getTapTalkActiveUser(instanceKey).name), true)
//            TapCallWaitingScreenActivity.start(activity, instanceKey, callInitiatedMessage)
        }

        fun rejectPendingIncomingConferenceCall() {
            sendRejectedCallNotification(activeCallInstanceKey ?: return, activeCallMessage?.room ?: return)
            clearPendingIncomingCall()
        }

        private fun startConferenceCall(context: Context, roomName: String, showWaitingScreen: Boolean) {
            Log.e(">>>>", "startConferenceCall: $roomName")
            callState = CallState.IN_CALL
            val options: JitsiMeetConferenceOptions = JitsiMeetConferenceOptions.Builder()
                .setRoom(roomName)
                .setWelcomePageEnabled(false)
                .setAudioMuted(false)
                .setVideoMuted(true)
                .build()
//            JitsiMeetActivity.launch(context, options)
            MeetTalkCallActivity.launch(context, options, showWaitingScreen, activeCallMessage!!)
        }

        fun checkAndHandleCallNotificationFromMessage(message: TAPMessageModel, instanceKey: String, activeUser: TAPUserModel) {
            Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: ${message.type} ${message.user.fullname} ${message.body}")
            if (message.type == CALL_INITIATED && message.user.userID != activeUser.userID) {
                if (callState == CallState.IDLE) {
                    // Received call initiated notification, show incoming call
                    Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CALL_INITIATED - Show incoming call")
                    activeCallMessage = message
                    activeCallInstanceKey = instanceKey
                    // TODO: HANDLE COUNTRY CODE IN NUMBER
                    showIncomingCall(
                        message.user.name,
                        String.format("0%s", message.user.phoneNumber)
                    )
                    callState = CallState.RINGING
                } else {
                    Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CALL_INITIATED - Target busy")
                    sendBusyNotification(instanceKey, message.room)
                }
            }
            else if ((message.type == CALL_CANCELLED && message.user.userID != activeUser.userID) ||
                (message.type == TARGET_REJECTED_CALL && message.user.userID == activeUser.userID)
            ) {
                // Caller canceled call or target rejected call elsewhere, dismiss incoming call
                Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CALL_CANCELED")
                TapCallConnection.getInstance().onDisconnect()
                activeCallMessage = null
                activeCallInstanceKey = null
                callState = CallState.IDLE
            }
            else if (message.type == CALL_ENDED) {
                // A party ended the call, leave active call room
                Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: CALL_ENDED $activeMeetTalkCallActivity")
                activeMeetTalkCallActivity?.finish()
                activeCallMessage = null
                activeCallInstanceKey = null
                callState = CallState.IDLE
            }
            else if (message.type == TARGET_JOINED_CALL) {
                if (message.user.userID != activeUser.userID) {
                    // Dismiss caller's waiting screen
                    Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: TARGET_JOINED_CALL")
//                    activeWaitingScreenActivity?.finish()
                } else {
                    // Target answered call elsewhere, dismiss incoming call
                    TapCallConnection.getInstance().onDisconnect()
                    callState = CallState.IDLE
                }
            }
            else if (message.user.userID != activeUser.userID &&
                (message.type == TARGET_BUSY ||
                message.type == TARGET_REJECTED_CALL ||
                message.type == TARGET_MISSED_CALL)
            ) {
                // Target did not join call, leave call room & dismiss waiting screen
                Log.e(">>>>", "checkAndHandleCallNotificationFromMessage: TARGET_BUSY | TARGET_REJECTED_CALL | TARGET_MISSED_CALL")
                // TODO: NOTIFY USER
//                activeWaitingScreenActivity?.finish()
                activeMeetTalkCallActivity?.finish()
                activeCallMessage = null
                activeCallInstanceKey = null
                callState = CallState.IDLE
            }
            else if (message.type == TARGET_UNABLE_TO_RECEIVE_CALL) {
                // One of target's device is unable to receive the call
                // TODO:
            }
        }

        fun sendCallInitiatedNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            val message = sendCallNotificationMessage(instanceKey, room, "{{user}} initiated call.", CALL_INITIATED)
            activeCallMessage = message
            activeCallInstanceKey = instanceKey
            return message
        }

        fun sendCallCanceledNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            activeCallMessage = null
            activeCallInstanceKey = null
            return sendCallNotificationMessage(instanceKey, room, "{{user}} canceled call.", CALL_CANCELLED)
        }

        fun sendCallEndedNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            activeCallMessage = null
            activeCallInstanceKey = null
            return sendCallNotificationMessage(instanceKey, room, "{{user}} ended call.", CALL_ENDED)
        }

        fun sendJoinedCallNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            return sendCallNotificationMessage(instanceKey, room, "{{user}} joined call.", TARGET_JOINED_CALL)
        }

        fun sendBusyNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            activeCallMessage = null
            activeCallInstanceKey = null
            return sendCallNotificationMessage(instanceKey, room, "{{user}} is busy.", TARGET_BUSY)
        }

        fun sendRejectedCallNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            activeCallMessage = null
            activeCallInstanceKey = null
            return sendCallNotificationMessage(instanceKey, room, "{{user}} rejected call.", TARGET_REJECTED_CALL)
        }

        fun sendMissedCallNotification(instanceKey: String, room: TAPRoomModel) : TAPMessageModel {
            activeCallMessage = null
            activeCallInstanceKey = null
            return sendCallNotificationMessage(instanceKey, room, "{{user}} missed the call.", TARGET_MISSED_CALL)
        }

        fun sendUnableToReceiveCallNotification(instanceKey: String, room: TAPRoomModel, body: String) : TAPMessageModel {
            activeCallMessage = null
            activeCallInstanceKey = null
            return sendCallNotificationMessage(instanceKey, room, body, TARGET_UNABLE_TO_RECEIVE_CALL)
        }

        fun sendCallNotificationMessage(instanceKey: String, room: TAPRoomModel, body: String, type: Int) : TAPMessageModel {
            val notificationMessage = TAPMessageModel.Builder(
                body,
                room,
                type,
                System.currentTimeMillis(),
                TapTalk.getTapTalkActiveUser(instanceKey),
                if (room.type == TYPE_PERSONAL) TAPChatManager.getInstance(instanceKey).getOtherUserIdFromRoom(room.roomID) else "0",
                null
            )
//            notificationMessage.hidden = true
            TapCoreMessageManager.getInstance(instanceKey).sendCustomMessage(notificationMessage, null)
            return notificationMessage
        }
    }
}
