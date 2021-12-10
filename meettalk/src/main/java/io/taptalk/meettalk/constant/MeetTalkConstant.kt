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

    /**
     * Use in setFeatureFlag(CONST, VALUE) when building JitsiMeetConferenceOptions
     */
    object JitsiMeetFlag {
        /**
         * Flag indicating if add-people functionality should be enabled.
         * Default: enabled (true).
         */
        const val ADD_PEOPLE_ENABLED = "add-people.enabled"

        /**
         * Flag indicating if the SDK should not require the audio focus.
         * Used by apps that do not use Jitsi audio.
         * Default: disabled (false).
         */
        const val AUDIO_FOCUS_DISABLED = "audio-focus.disabled"

        /**
         * Flag indicating if the audio mute button should be displayed.
         * Default: enabled (true).
         */
        const val AUDIO_MUTE_BUTTON_ENABLED = "audio-mute.enabled"

        /**
         * Flag indicating that the Audio only button in the overflow menu is enabled.
         * Default: enabled (true).
         */
        const val AUDIO_ONLY_BUTTON_ENABLED = "audio-only.enabled"

        /**
         * Flag indicating if calendar integration should be enabled.
         * Default: enabled (true) on Android, auto-detected on iOS.
         */
        const val CALENDAR_ENABLED = "calendar.enabled"

        /**
         * Flag indicating if call integration (CallKit on iOS, ConnectionService on Android)
         * should be enabled.
         * Default: enabled (true).
         */
        const val CALL_INTEGRATION_ENABLED = "call-integration.enabled"

        /**
         * Flag indicating if close captions should be enabled.
         * Default: enabled (true).
         */
        const val CLOSE_CAPTIONS_ENABLED = "close-captions.enabled"

        /**
         * Flag indicating if conference timer should be enabled.
         * Default: enabled (true).
         */
        const val CONFERENCE_TIMER_ENABLED = "conference-timer.enabled"

        /**
         * Flag indicating if chat should be enabled.
         * Default: enabled (true).
         */
        const val CHAT_ENABLED = "chat.enabled"

        /**
         * Flag indicating if the filmstrip should be enabled.
         * Default: enabled (true).
         */
        const val FILMSTRIP_ENABLED = "filmstrip.enabled"

        /**
         * Flag indicating if fullscreen (immersive) mode should be enabled.
         * Default: enabled (true).
         */
        const val FULLSCREEN_ENABLED = "fullscreen.enabled"

        /**
         * Flag indicating if the Help button should be enabled.
         * Default: enabled (true).
         */
        const val HELP_BUTTON_ENABLED = "help.enabled"

        /**
         * Flag indicating if invite functionality should be enabled.
         * Default: enabled (true).
         */
        const val INVITE_ENABLED = "invite.enabled"

        /**
         * Flag indicating if recording should be enabled in iOS.
         * Default: disabled (false).
         */
        const val IOS_RECORDING_ENABLED = "ios.recording.enabled"

        /**
         * Flag indicating if screen sharing should be enabled in iOS.
         * Default: disabled (false).
         */
        const val IOS_SCREENSHARING_ENABLED = "ios.screensharing.enabled"

        /**
         * Flag indicating if screen sharing should be enabled in android.
         * Default: enabled (true).
         */
        const val ANDROID_SCREENSHARING_ENABLED = "android.screensharing.enabled"

        /**
         * Flag indicating if speaker statistics should be enabled.
         * Default: enabled (true).
         */
        const val SPEAKERSTATS_ENABLED = "speakerstats.enabled"

        /**
         * Flag indicating if kickout is enabled.
         * Default: enabled (true).
         */
        const val KICK_OUT_ENABLED = "kick-out.enabled"

        /**
         * Flag indicating if live-streaming should be enabled.
         * Default: auto-detected.
         */
        const val LIVE_STREAMING_ENABLED = "live-streaming.enabled"

        /**
         * Flag indicating if lobby mode button should be enabled.
         * Default: enabled.
         */
        const val LOBBY_MODE_ENABLED = "lobby-mode.enabled"

        /**
         * Flag indicating if displaying the meeting name should be enabled.
         * Default: enabled (true).
         */
        const val MEETING_NAME_ENABLED = "meeting-name.enabled"

        /**
         * Flag indicating if the meeting password button should be enabled.
         * Note that this flag just decides on the button, if a meeting has a password
         * set, the password dialog will still show up.
         * Default: enabled (true).
         */
        const val MEETING_PASSWORD_ENABLED = "meeting-password.enabled"

        /**
         * Flag indicating if the notifications should be enabled.
         * Default: enabled (true).
         */
        const val NOTIFICATIONS_ENABLED = "notifications.enabled"

        /**
         * Flag indicating if the audio overflow menu button should be displayed.
         * Default: enabled (true).
         */
        const val OVERFLOW_MENU_ENABLED = "overflow-menu.enabled"

        /**
         * Flag indicating if Picture-in-Picture should be enabled.
         * Default: auto-detected.
         */
        const val PIP_ENABLED = "pip.enabled"

        /**
         * Flag indicating if raise hand feature should be enabled.
         * Default: enabled.
         */
        const val RAISE_HAND_ENABLED = "raise-hand.enabled"

        /**
         * Flag indicating if the reactions feature should be enabled.
         * Default: enabled (true).
         */
        const val REACTIONS_ENABLED = "reactions.enabled"

        /**
         * Flag indicating if recording should be enabled.
         * Default: auto-detected.
         */
        const val RECORDING_ENABLED = "recording.enabled"

        /**
         * Flag indicating if the user should join the conference with the replaceParticipant functionality.
         * Default: (false).
         */
        const val REPLACE_PARTICIPANT = "replace.participant"

        /**
         * Flag indicating the local and (maximum) remote video resolution. Overrides
         * the server configuration.
         * Default: (unset).
         */
        const val RESOLUTION = "resolution"

        /**
         * Flag indicating if the security options button should be enabled.
         * Default: enabled (true).
         */
        const val SECURITY_OPTIONS_ENABLED = "security-options.enabled"

        /**
         * Flag indicating if server URL change is enabled.
         * Default: enabled (true).
         */
        const val SERVER_URL_CHANGE_ENABLED = "server-url-change.enabled"

        /**
         * Flag indicating if tile view feature should be enabled.
         * Default: enabled.
         */
        const val TILE_VIEW_ENABLED = "tile-view.enabled"

        /**
         * Flag indicating if the toolbox should be always be visible
         * Default: disabled (false).
         */
        const val TOOLBOX_ALWAYS_VISIBLE = "toolbox.alwaysVisible"

        /**
         * Flag indicating if the toolbox should be enabled
         * Default: enabled.
         */
        const val TOOLBOX_ENABLED = "toolbox.enabled"

        /**
         * Flag indicating if the video mute button should be displayed.
         * Default: enabled (true).
         */
        const val VIDEO_MUTE_BUTTON_ENABLED = "video-mute.enabled"

        /**
         * Flag indicating if the video share button should be enabled
         * Default: enabled (true).
         */
        const val VIDEO_SHARE_BUTTON_ENABLED = "video-share.enabled"

        /**
         * Flag indicating if the welcome page should be enabled.
         * Default: disabled (false).
         */
        const val WELCOME_PAGE_ENABLED = "welcomepage.enabled"
    }

}
