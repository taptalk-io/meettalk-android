package io.taptalk.meettalk.helper;

import static io.taptalk.TapTalk.Helper.TapTalk.TapTalkImplementationType.TapTalkImplementationTypeCore;
import static io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.CALL_MESSAGE_TYPE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import io.taptalk.TapTalk.Helper.TapTalk;
import io.taptalk.TapTalk.Listener.TapCoreMessageListener;
import io.taptalk.TapTalk.Listener.TapListener;
import io.taptalk.TapTalk.Listener.TapUIRoomListListener;
import io.taptalk.TapTalk.Manager.TapCoreMessageManager;
import io.taptalk.TapTalk.Manager.TapUI;
import io.taptalk.TapTalk.Model.TAPMessageModel;
import io.taptalk.TapTalk.Model.TAPRoomListModel;
import io.taptalk.TapTalk.Model.TAPRoomModel;
import io.taptalk.meettalk.BuildConfig;
import io.taptalk.meettalk.R;
import io.taptalk.meettalk.custombubble.MeetTalkCallChatBubbleClass;
import io.taptalk.meettalk.custombubble.MeetTalkCallChatBubbleListener;
import io.taptalk.meettalk.listener.MeetTalkListener;
import io.taptalk.meettalk.manager.MeetTalkCallManager;

public class MeetTalk {

    public static Context appContext;
    public static String appID;

    private static HashMap<String, ArrayList<MeetTalkListener>> meetTalkListenerMap;

    /**
     * =============================================================================================
     * Initialization
     * =============================================================================================
     */

    public static void init(
            Context applicationContext,
            String appKeyID,
            String appKeySecret,
            int clientAppIcon,
            String clientAppName,
            String appBaseUrl,
            TapTalk.TapTalkImplementationType tapTalkImplementationType,
            MeetTalkListener meetTalkListener
    ) {
        initNewInstance(
                "",
                applicationContext,
                appKeyID,
                appKeySecret,
                "",
                clientAppIcon,
                clientAppName,
                appBaseUrl,
                tapTalkImplementationType,
                meetTalkListener
        );
    }

    public static void init(
            Context applicationContext,
            String appKeyID,
            String appKeySecret,
            String userAgent,
            int clientAppIcon,
            String clientAppName,
            String appBaseUrl,
            TapTalk.TapTalkImplementationType tapTalkImplementationType,
            MeetTalkListener meetTalkListener
    ) {
        initNewInstance(
                "",
                applicationContext,
                appKeyID,
                appKeySecret,
                userAgent,
                clientAppIcon,
                clientAppName,
                appBaseUrl,
                tapTalkImplementationType,
                meetTalkListener
        );
    }

    public static void initNewInstance(
            String instanceKey,
            Context applicationContext,
            String appKeyID,
            String appKeySecret,
            int clientAppIcon,
            String clientAppName,
            String appBaseUrl,
            TapTalk.TapTalkImplementationType tapTalkImplementationType,
            MeetTalkListener meetTalkListener
    ) {
        initNewInstance(
                instanceKey,
                applicationContext,
                appKeyID,
                appKeySecret,
                "android",
                clientAppIcon,
                clientAppName,
                appBaseUrl,
                tapTalkImplementationType,
                meetTalkListener
        );
    }

    public static void initNewInstance(
            String instanceKey,
            Context applicationContext,
            String appKeyID,
            String appKeySecret,
            String userAgent,
            int clientAppIcon,
            String clientAppName,
            String appBaseUrl,
            TapTalk.TapTalkImplementationType tapTalkImplementationType,
            MeetTalkListener meetTalkListener
    ) {
        // Save application context and ID
        appContext = applicationContext;
        appID = appKeyID;

        // Initialize TapListener
        TapListener tapListener = new TapListener() {
            // Add override if TapListener is updated to redirect all events to MeetTalkListener

            @Override
            public void onInitializationCompleted(String instanceKey) {
                super.onInitializationCompleted(instanceKey);
                meetTalkListener.onInitializationCompleted(instanceKey);
            }

            @Override
            public void onTapTalkRefreshTokenExpired() {
                super.onTapTalkRefreshTokenExpired();
                meetTalkListener.onTapTalkRefreshTokenExpired();
            }

            @Override
            public void onTapTalkUnreadChatRoomBadgeCountUpdated(int unreadCount) {
                super.onTapTalkUnreadChatRoomBadgeCountUpdated(unreadCount);
                meetTalkListener.onTapTalkUnreadChatRoomBadgeCountUpdated(unreadCount);
            }

            @Override
            public void onNotificationReceived(TAPMessageModel message) {
                meetTalkListener.onNotificationReceived(message);

                // Handle call events
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>", "MeetTalk: onNotificationReceived: " + message.getType() + " - " + message.getBody());
                }
                MeetTalkCallManager.Companion.checkAndHandleCallNotificationFromMessage(
                        message,
                        instanceKey,
                        TapTalk.getTapTalkActiveUser(instanceKey)
                );
            }

            @Override
            public void onUserLogout() {
                super.onUserLogout();
                meetTalkListener.onUserLogout();
            }

            @Override
            public void onTaskRootChatRoomClosed(Activity activity) {
                super.onTaskRootChatRoomClosed(activity);
                meetTalkListener.onTaskRootChatRoomClosed(activity);
            }
        };

        // Initialize TapCoreMessageListener
        TapCoreMessageListener tapCoreMessageListener = new TapCoreMessageListener() {
            @Override
            public void onReceiveNewMessage(TAPMessageModel message) {
                super.onReceiveNewMessage(message);

                // Handle call events
                if (BuildConfig.DEBUG) {
                    Log.e(">>>>", "onReceiveNewMessage: " + message.getBody());
                }
                MeetTalkCallManager.Companion.checkAndHandleCallNotificationFromMessage(
                        message,
                        instanceKey,
                        TapTalk.getTapTalkActiveUser(instanceKey)
                );
            }
        };

        // Initialize TapTalk instance and listeners
        TapTalk.initNewInstance(
                instanceKey,
                applicationContext,
                appKeyID,
                appKeySecret,
                userAgent,
                clientAppIcon,
                clientAppName,
                appBaseUrl,
                tapTalkImplementationType,
                tapListener
        );
        TapCoreMessageManager.getInstance(instanceKey).addMessageListener(tapCoreMessageListener);
        addMeetTalkListener(instanceKey, meetTalkListener);

        if (tapTalkImplementationType != TapTalkImplementationTypeCore) {
            // Initialize call message bubble
            MeetTalkCallChatBubbleListener callChatBubbleListener = (activity, message) -> {
                // Trigger listener callback
                for (MeetTalkListener listener : getMeetTalkListeners(instanceKey)) {
                    listener.onChatBubbleCallButtonTapped(instanceKey, activity, message);
                }
            };
            MeetTalkCallChatBubbleClass callChatBubbleClass = new MeetTalkCallChatBubbleClass(
                    instanceKey,
                    R.layout.meettalk_cell_chat_bubble_call,
                    8001,
                    callChatBubbleListener
            );
            TapUI.getInstance(instanceKey).addCustomBubble(callChatBubbleClass);

            // Add room list listener to replace room list message body
            TapUI.getInstance(instanceKey).addRoomListListener(new TapUIRoomListListener() {
                @Override
                public String setRoomListContentText(TAPRoomListModel roomList, int position, Context context) {
                    if (roomList.getLastMessage().getType() == CALL_MESSAGE_TYPE) {
                        String body = roomList.getLastMessage().getBody();
                        return body.replace("{{sender}}", roomList.getLastMessage().getUser().getFullname());
                    }
                    return super.setRoomListContentText(roomList, position, context);
                }
            });
        }

        // Start service to handle sending notification when app is killed
        appContext.startService(new Intent(appContext, MeetTalkTaskRemovedService.class));

        // Trigger initialization completed callback
        meetTalkListener.onInitializationCompleted(instanceKey);
    }

    /**
     * =============================================================================================
     * MeetTalk Listener
     * =============================================================================================
     */

    private static HashMap<String, ArrayList<MeetTalkListener>> getMeetTalkListenerMap() {
        if (meetTalkListenerMap == null) {
            meetTalkListenerMap = new HashMap<>();
        }
        return meetTalkListenerMap;
    }

    public static ArrayList<MeetTalkListener> getMeetTalkListeners() {
        return getMeetTalkListeners("");
    }

    public static ArrayList<MeetTalkListener> getMeetTalkListeners(String instanceKey) {
        if (!getMeetTalkListenerMap().containsKey(instanceKey)) {
            ArrayList<MeetTalkListener> meetTalkListeners = new ArrayList<>();
            getMeetTalkListenerMap().put(instanceKey, meetTalkListeners);
        }
        return getMeetTalkListenerMap().get(instanceKey);
    }

    // FIXME: METHODS INSIDE CALLBACK IN LISTENER TRIGGERED MORE THAN ONCE WHEN ADDED MORE THAN ONCE
    private static void addMeetTalkListener(MeetTalkListener meetTalkListener) {
        addMeetTalkListener("", meetTalkListener);
    }

    private static void addMeetTalkListener(String instanceKey, MeetTalkListener meetTalkListener) {
        if (!getMeetTalkListeners(instanceKey).contains(meetTalkListener)) {
            getMeetTalkListeners(instanceKey).add(meetTalkListener);
        }
    }

    private static void removeMeetTalkListener(MeetTalkListener meetTalkListener) {
        removeMeetTalkListener("", meetTalkListener);
    }

    private static void removeMeetTalkListener(String instanceKey, MeetTalkListener meetTalkListener) {
        getMeetTalkListeners(instanceKey).remove(meetTalkListener);
    }

    /**
     * =============================================================================================
     * Phone Account & Incoming Call
     * =============================================================================================
     */

    public static boolean isPhoneAccountEnabled() {
        return MeetTalkCallManager.Companion.isPhoneAccountEnabled();
    }

    public static boolean isEnablePhoneAccountSettingsRequested() {
        return isEnablePhoneAccountSettingsRequested("");
    }

    public static boolean isEnablePhoneAccountSettingsRequested(String instanceKey) {
        return MeetTalkCallManager.Companion.isEnablePhoneAccountSettingsRequested(instanceKey);
    }

    public static void requestEnablePhoneAccountSettings(Activity activity) {
        requestEnablePhoneAccountSettings("", activity);
    }

    public static void requestEnablePhoneAccountSettings(String instanceKey, Activity activity) {
        MeetTalkCallManager.Companion.requestEnablePhoneAccountSettings(instanceKey, activity);
    }

    public static void checkAndRequestEnablePhoneAccountSettings(Activity activity) {
        checkAndRequestEnablePhoneAccountSettings("", activity);
    }

    public static void checkAndRequestEnablePhoneAccountSettings(
            String instanceKey,
            Activity activity
    ) {
        MeetTalkCallManager.Companion.checkAndRequestEnablePhoneAccountSettings(instanceKey, activity);
    }

    public static void openPhoneAccountSettings() {
        MeetTalkCallManager.Companion.openPhoneAccountSettings();
    }

    public static void openAppNotificationSettings(Context context) {
        MeetTalkCallManager.Companion.openAppNotificationSettings(context, false);
    }

    public static void openAppIncomingCallNotificationChannelSettings(Context context) {
        MeetTalkCallManager.Companion.openAppNotificationSettings(context, true);
    }

    public static void showIncomingCall(TAPMessageModel message) {
        showIncomingCall(message, "", "");
    }

    public static void showIncomingCall(TAPMessageModel message, String name, String phoneNumber) {
        MeetTalkCallManager.Companion.showIncomingCall(message, name, phoneNumber);
    }

    /**
     * =============================================================================================
     * Conference Call
     * =============================================================================================
     */

    public static void initiateNewConferenceCall(
            Activity activity,
            TAPRoomModel room
    ) {
        initiateNewConferenceCall(activity, "", room);
    }

    public static void initiateNewConferenceCall(
            Activity activity,
            String instanceKey,
            TAPRoomModel room
    ) {
        MeetTalkCallManager.Companion.initiateNewConferenceCall(
                activity,
                instanceKey,
                room,
                MeetTalkCallManager.Companion.getDefaultAudioMuted(),
                MeetTalkCallManager.Companion.getDefaultVideoMuted()
        );
    }

    public static void initiateNewConferenceCall(
            Activity activity,
            String instanceKey,
            TAPRoomModel room,
            String recipientDisplayName
    ) {
        MeetTalkCallManager.Companion.initiateNewConferenceCall(
                activity,
                instanceKey,
                room,
                MeetTalkCallManager.Companion.getDefaultAudioMuted(),
                MeetTalkCallManager.Companion.getDefaultVideoMuted(),
                recipientDisplayName
        );
    }

    public static void initiateNewConferenceCall(
            Activity activity,

            TAPRoomModel room,
            boolean startWithAudioMuted,
            boolean startWithVideoMuted
    ) {
        initiateNewConferenceCall(activity, "", room, startWithAudioMuted, startWithVideoMuted);
    }

    public static void initiateNewConferenceCall(
            Activity activity,
            String instanceKey,
            TAPRoomModel room,
            boolean startWithAudioMuted,
            boolean startWithVideoMuted
    ) {
        MeetTalkCallManager.Companion.initiateNewConferenceCall(activity, instanceKey, room, startWithAudioMuted, startWithVideoMuted);
    }

    public static void initiateNewConferenceCall(
            Activity activity,
            String instanceKey,
            TAPRoomModel room,
            boolean startWithAudioMuted,
            boolean startWithVideoMuted,
            String recipientDisplayName
    ) {
        MeetTalkCallManager.Companion.initiateNewConferenceCall(activity, instanceKey, room, startWithAudioMuted, startWithVideoMuted, recipientDisplayName);
    }

    public static boolean joinPendingIncomingConferenceCall() {
        return MeetTalkCallManager.Companion.joinPendingIncomingConferenceCall();
    }

    public static void rejectPendingIncomingConferenceCall() {
        MeetTalkCallManager.Companion.rejectPendingIncomingConferenceCall();
    }

    public static boolean launchMeetTalkCallActivity(
            String instanceKey,
            Context context
    ) {
        return MeetTalkCallManager.Companion.launchMeetTalkCallActivity(
                instanceKey,
                context
        );
    }

    public static boolean launchMeetTalkCallActivity(
            String instanceKey,
            Context context,
            TAPRoomModel room,
            String activeUserName,
            String activeUserAvatarUrl
    ) {
        return MeetTalkCallManager.Companion.launchMeetTalkCallActivity(
                instanceKey,
                context,
                room,
                activeUserName,
                activeUserAvatarUrl
        );
    }

    /**
     * =============================================================================================
     * Permission
     * =============================================================================================
     */

    public static boolean checkAndRequestAudioPermission(Activity activity) {
        return MeetTalkCallManager.Companion.checkAndRequestAudioPermission(activity);
    }

    public static boolean checkAndRequestCameraPermission(Activity activity) {
        return MeetTalkCallManager.Companion.checkAndRequestCameraPermission(activity);
    }
}
