package io.taptalk.meettalk.listener;

import android.app.Activity;

import androidx.annotation.Keep;

import io.taptalk.TapTalk.Helper.TapTalk;
import io.taptalk.TapTalk.Model.TAPMessageModel;

@Keep
public abstract class MeetTalkListener implements MeetTalkInterface {

    private String instanceKey = "";

    public MeetTalkListener() {
    }

    public MeetTalkListener(String instanceKey) {
        this.instanceKey = instanceKey;
    }

    @Override
    public void onInitializationCompleted(String instanceKey) {

    }

    @Override
    public void onTapTalkRefreshTokenExpired() {
    }

    @Override
    public void onTapTalkUnreadChatRoomBadgeCountUpdated(int unreadCount) {
    }

    @Override
    public void onNotificationReceived(TAPMessageModel message) {
        TapTalk.showTapTalkNotification(instanceKey, message);
    }

    @Override
    public void onUserLogout() {
    }

    @Override
    public void onTaskRootChatRoomClosed(Activity activity) {

    }
}
