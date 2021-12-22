package io.taptalk.meettalk.helper;

import static io.taptalk.TapTalk.Helper.TapTalk.TapTalkImplementationType.TapTalkImplementationTypeCore;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import io.taptalk.TapTalk.Helper.TapTalk;
import io.taptalk.TapTalk.Listener.TapCoreMessageListener;
import io.taptalk.TapTalk.Listener.TapListener;
import io.taptalk.TapTalk.Manager.TapCoreMessageManager;
import io.taptalk.TapTalk.Manager.TapUI;
import io.taptalk.TapTalk.Model.TAPMessageModel;
import io.taptalk.TapTalk.Model.TAPRoomModel;
import io.taptalk.meettalk.R;
import io.taptalk.meettalk.custombubble.MeetTalkCallChatBubbleClass;
import io.taptalk.meettalk.custombubble.MeetTalkCallChatBubbleListener;
import io.taptalk.meettalk.listener.MeetTalkListener;
import io.taptalk.meettalk.manager.MeetTalkCallManager;

public class MeetTalk {

    public static Context appContext;

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
        // Save application context
        appContext = applicationContext;

        // Initialize TapListener
        TapListener tapListener = new TapListener() {
            // Add override if TapListener is updated to redirect all events to MeetTalkListener

            @Override
            public void onInitializationCompleted(String instanceKey) {
                super.onInitializationCompleted(instanceKey);
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
                Log.e(">>>>", "onNotificationReceived: " + message.getBody());
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
                Log.e(">>>>", "onReceiveNewMessage: " + message.getBody());
                MeetTalkCallManager.Companion.checkAndHandleCallNotificationFromMessage(
                        message,
                        instanceKey,
                        TapTalk.getTapTalkActiveUser(instanceKey)
                );
            }
        };

        // Initialize TapTalk instance
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

        if (tapTalkImplementationType != TapTalkImplementationTypeCore) {
//            // Initialize empty message bubble
//            MeetTalkEmptyListener emptyListener = new MeetTalkEmptyListener() {};
//            MeetTalkEmptyChatBubbleClass callInitiatedChatBubbleClass = new MeetTalkEmptyChatBubbleClass(
//                    instanceKey,
//                    io.taptalk.TapTalk.R.layout.tap_cell_empty,
//                    CALL_INITIATED,
//                    emptyListener
//            );
//            MeetTalkEmptyChatBubbleClass callTargetJoinedChatBubbleClass = new MeetTalkEmptyChatBubbleClass(
//                    instanceKey,
//                    io.taptalk.TapTalk.R.layout.tap_cell_empty,
//                    TARGET_JOINED_CALL,
//                    emptyListener
//            );
//            TapUI.getInstance(instanceKey).addCustomBubble(callInitiatedChatBubbleClass);
//            TapUI.getInstance(instanceKey).addCustomBubble(callTargetJoinedChatBubbleClass);

            // Initialize call message bubble
            MeetTalkCallChatBubbleListener callChatBubbleListener = (activity, message) -> {
                initiateNewConferenceCall(activity, instanceKey, message.getRoom());
            };
            MeetTalkCallChatBubbleClass callChatBubbleClass = new MeetTalkCallChatBubbleClass(
                    instanceKey,
                    R.layout.meettalk_cell_chat_bubble_call,
                    8001,
                    callChatBubbleListener
            );
            TapUI.getInstance(instanceKey).addCustomBubble(callChatBubbleClass);
//            MeetTalkCallChatBubbleClass callEndedChatBubbleClass = new MeetTalkCallChatBubbleClass(
//                    instanceKey,
//                    R.layout.meettalk_cell_chat_bubble_call,
//                    CALL_ENDED,
//                    callChatBubbleListener
//            );
//            MeetTalkCallChatBubbleClass callCancelledChatBubbleClass = new MeetTalkCallChatBubbleClass(
//                    instanceKey,
//                    R.layout.meettalk_cell_chat_bubble_call,
//                    CALL_CANCELLED,
//                    callChatBubbleListener
//            );
//            MeetTalkCallChatBubbleClass callTargetBusyChatBubbleClass = new MeetTalkCallChatBubbleClass(
//                    instanceKey,
//                    R.layout.meettalk_cell_chat_bubble_call,
//                    TARGET_BUSY,
//                    callChatBubbleListener
//            );
//            MeetTalkCallChatBubbleClass callTargetRejectedChatBubbleClass = new MeetTalkCallChatBubbleClass(
//                    instanceKey,
//                    R.layout.meettalk_cell_chat_bubble_call,
//                    TARGET_REJECTED_CALL,
//                    callChatBubbleListener
//            );
//            MeetTalkCallChatBubbleClass callTargetMissedChatBubbleClass = new MeetTalkCallChatBubbleClass(
//                    instanceKey,
//                    R.layout.meettalk_cell_chat_bubble_call,
//                    TARGET_MISSED_CALL,
//                    callChatBubbleListener
//            );
//            TapUI.getInstance(instanceKey).addCustomBubble(callEndedChatBubbleClass);
//            TapUI.getInstance(instanceKey).addCustomBubble(callCancelledChatBubbleClass);
//            TapUI.getInstance(instanceKey).addCustomBubble(callTargetBusyChatBubbleClass);
//            TapUI.getInstance(instanceKey).addCustomBubble(callTargetRejectedChatBubbleClass);
//            TapUI.getInstance(instanceKey).addCustomBubble(callTargetMissedChatBubbleClass);
        }

        meetTalkListener.onInitializationCompleted(instanceKey);
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
        MeetTalkCallManager.Companion.initiateNewConferenceCall(activity, instanceKey, room);
    }
}
