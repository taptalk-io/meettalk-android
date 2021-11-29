package io.taptalk.meettalk.helper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import io.taptalk.TapTalk.Helper.TapTalk;
import io.taptalk.TapTalk.Listener.TapCoreMessageListener;
import io.taptalk.TapTalk.Listener.TapListener;
import io.taptalk.TapTalk.Manager.TapCoreMessageManager;
import io.taptalk.TapTalk.Model.TAPMessageModel;
import io.taptalk.meettalk.listener.MeetTalkListener;
import io.taptalk.meettalk.manager.MeetTalkDataManager;
import io.taptalk.meettalk.manager.TapCallManager;

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
                super.onNotificationReceived(message);
                meetTalkListener.onNotificationReceived(message);

                // Handle call events
                Log.e(">>>>", "onNotificationReceived: " + message.getBody());
                TapCallManager.Companion.checkAndHandleCallNotificationFromMessage(
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
                TapCallManager.Companion.checkAndHandleCallNotificationFromMessage(
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

        meetTalkListener.onInitializationCompleted(instanceKey);
    }

    public static void checkAndRequestEnablePhoneAccountSettings(
            String instanceKey,
            Activity activity
    ) {
        TapCallManager.Companion.checkAndRequestEnablePhoneAccountSettings(instanceKey, activity);
    }
}
