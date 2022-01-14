package io.taptalk.meettalk.listener;

import android.app.Activity;

import io.taptalk.TapTalk.Model.TAPMessageModel;
import io.taptalk.TapTalk.Model.TAPRoomModel;
import io.taptalk.meettalk.model.MeetTalkConferenceInfo;

public interface MeetTalkInterface {

    /**
     * =============================================================================================
     * TapListener callbacks
     * =============================================================================================
     */

    void onInitializationCompleted(String instanceKey);

    void onTapTalkRefreshTokenExpired();

    void onTapTalkUnreadChatRoomBadgeCountUpdated(int unreadCount);

    void onNotificationReceived(TAPMessageModel message);

    void onUserLogout();

    void onTaskRootChatRoomClosed(Activity activity);

    /**
     * =============================================================================================
     * MeetTalk call/conference notification callbacks
     * =============================================================================================
     */

    void onReceiveCallInitiatedNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onReceiveCallCancelledNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onReceiveCallEndedNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onReceiveRecipientAnsweredCallNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onReceiveRecipientBusyNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onReceiveRecipientRejectedCallNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onReceiveRecipientMissedCallNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onReceiveRecipientUnableToReceiveCallNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onReceiveActiveUserRejectedCallNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onReceiveParticipantJoinedConferenceNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onReceiveParticipantLeftConferenceNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onReceiveConferenceInfoUpdatedNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo);

    /**
     * =============================================================================================
     * MeetTalk incoming call callbacks
     * =============================================================================================
     */

    void onIncomingCallReceived(String instanceKey, TAPMessageModel message);

    void onShowIncomingCallFailed(String instanceKey, TAPMessageModel message, String errorMessage);

    void onIncomingCallAnswered();

    void onIncomingCallRejected();

    void onIncomingCallDisconnected();

    /**
     * =============================================================================================
     * MeetTalk conference callbacks
     * =============================================================================================
     */

    void onDisconnectedFromConference(MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onReconnectedToConference(MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onConferenceJoined(MeetTalkConferenceInfo meetTalkConferenceInfo);

    void onConferenceTerminated(MeetTalkConferenceInfo meetTalkConferenceInfo);

    /**
     * =============================================================================================
     * MeetTalk UI callbacks
     * =============================================================================================
     */

    void onChatBubbleCallButtonTapped(String instanceKey, Activity activity, TAPMessageModel message);
}
