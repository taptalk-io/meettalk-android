package io.taptalk.meettalk.listener;

import static io.taptalk.TapTalk.Const.TAPDefaultConstant.PermissionRequest.PERMISSION_CAMERA_CAMERA;
import static io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.CALL_MESSAGE_TYPE;

import android.Manifest;
import android.app.Activity;

import androidx.annotation.Keep;
import androidx.core.app.ActivityCompat;

import io.taptalk.TapTalk.Helper.TAPUtils;
import io.taptalk.TapTalk.Helper.TapTalk;
import io.taptalk.TapTalk.Model.TAPMessageModel;
import io.taptalk.TapTalk.Model.TAPRoomModel;
import io.taptalk.TapTalk.View.Activity.TAPBarcodeScannerActivity;
import io.taptalk.TapTalk.View.Activity.TAPNewChatActivity;
import io.taptalk.meettalk.helper.MeetTalk;
import io.taptalk.meettalk.helper.MeetTalkUtils;
import io.taptalk.meettalk.model.MeetTalkConferenceInfo;

@Keep
public abstract class MeetTalkListener implements MeetTalkInterface {

    private String instanceKey = "";

    public MeetTalkListener() {
    }

    public MeetTalkListener(String instanceKey) {
        this.instanceKey = instanceKey;
    }

    /**
     * =============================================================================================
     * TapListener callbacks
     * =============================================================================================
     */

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
        if (message.getType() != CALL_MESSAGE_TYPE) {
            TapTalk.showTapTalkNotification(instanceKey, message);
        }
    }

    @Override
    public void onUserLogout() {
    }

    @Override
    public void onTaskRootChatRoomClosed(Activity activity) {

    }

    /**
     * =============================================================================================
     * MeetTalk call/conference notification callbacks
     * =============================================================================================
     */

    @Override
    public void onReceiveCallInitiatedNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo) {
        MeetTalk.showIncomingCall(message);
    }

    @Override
    public void onReceiveCallCancelledNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onReceiveCallEndedNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onReceiveRecipientAnsweredCallNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onReceiveRecipientBusyNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onReceiveRecipientRejectedCallNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onReceiveRecipientMissedCallNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onReceiveRecipientUnableToReceiveCallNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onReceiveActiveUserRejectedCallNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onReceiveParticipantJoinedConferenceNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onReceiveParticipantLeftConferenceNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onReceiveConferenceInfoUpdatedNotificationMessage(String instanceKey, TAPMessageModel message, MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    /**
     * =============================================================================================
     * MeetTalk incoming call callbacks
     * =============================================================================================
     */

    @Override
    public void onIncomingCallReceived(String instanceKey, TAPMessageModel message) {

    }

    @Override
    public void onShowIncomingCallFailed(String instanceKey, TAPMessageModel message, String errorMessage) {

    }

    @Override
    public void onIncomingCallAnswered() {
        MeetTalk.joinPendingIncomingConferenceCall();
    }

    @Override
    public void onIncomingCallRejected() {
        MeetTalk.rejectPendingIncomingConferenceCall();
    }

    @Override
    public void onIncomingCallDisconnected() {

    }

    /**
     * =============================================================================================
     * MeetTalk conference callbacks
     * =============================================================================================
     */

    @Override
    public void onDisconnectedFromConference(MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onReconnectedToConference(MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onConferenceJoined(MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    @Override
    public void onConferenceTerminated(MeetTalkConferenceInfo meetTalkConferenceInfo) {

    }

    /**
     * =============================================================================================
     * MeetTalk UI callbacks
     * =============================================================================================
     */

    @Override
    public void onTaskRootCallActivityClosed(Activity activity) {

    }

    @Override
    public void onChatBubbleCallButtonTapped(String instanceKey, Activity activity, TAPMessageModel message) {
        MeetTalk.initiateNewConferenceCall(activity, instanceKey, message.getRoom());
    }

    @Override
    public void onChatRoomVoiceCallButtonTapped(String instanceKey, Activity activity, TAPRoomModel room) {
        MeetTalk.initiateNewConferenceCall(activity, instanceKey, room);
    }

    @Override
    public void onChatRoomVideoCallButtonTapped(String instanceKey, Activity activity, TAPRoomModel room) {
        MeetTalk.initiateNewConferenceCall(activity, instanceKey, room, false, false);
    }
}
