package io.taptalk.meettalk.listener;

import android.app.Activity;

import io.taptalk.TapTalk.Model.TAPMessageModel;

public interface MeetTalkInterface {
    void onInitializationCompleted(String instanceKey);

    void onTapTalkRefreshTokenExpired();

    void onTapTalkUnreadChatRoomBadgeCountUpdated(int unreadCount);

    void onNotificationReceived(TAPMessageModel message);

    void onUserLogout();

    void onTaskRootChatRoomClosed(Activity activity);
}
