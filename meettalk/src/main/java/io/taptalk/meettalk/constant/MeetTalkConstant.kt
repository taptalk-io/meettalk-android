package io.taptalk.meettalk.constant

class MeetTalkConstant {

    object BroadcastEvent {
        const val ACTIVE_USER_LEAVES_CALL = "kMeetTalkActiveUserLeavesCall"
        const val ACTIVE_USER_CANCELS_CALL = "kMeetTalkActiveUserCancelsCall"
    }

    object Extra {
        const val CALLER_NAME = "kMeetTalkExtraCallerName"
        const val CALLER_NUMBER = "kMeetTalkExtraCallerNumber"
    }

    object CallMessageType {
        const val CALL_INITIATED = 8001
        const val CALL_CANCELLED = 8002
        const val CALL_ENDED = 8003
        const val TARGET_JOINED_CALL = 8004
        const val TARGET_BUSY = 8005
        const val TARGET_REJECTED_CALL = 8006
        const val TARGET_MISSED_CALL = 8007
        const val TARGET_UNABLE_TO_RECEIVE_CALL = 8008
    }

    object Preference {
        const val ENABLE_PHONE_ACCOUNT_SETTINGS_REQUESTED_APP_NAME = "kMeetTalkEnablePhoneAccountSettingsRequestedAppName"
    }

}
