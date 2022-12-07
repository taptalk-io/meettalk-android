package io.taptalk.meettalkandroidsample;

import static io.taptalk.TapTalk.Const.TAPDefaultConstant.RoomType.TYPE_PERSONAL;
import static io.taptalk.TapTalk.Helper.TapTalk.TapTalkImplementationType.TapTalkImplementationTypeCombine;
import static io.taptalk.meettalkandroidsample.BuildConfig.GOOGLE_MAPS_API_KEY;
import static io.taptalk.meettalkandroidsample.BuildConfig.TAPTALK_SDK_APP_KEY_ID;
import static io.taptalk.meettalkandroidsample.BuildConfig.TAPTALK_SDK_APP_KEY_SECRET;
import static io.taptalk.meettalkandroidsample.BuildConfig.TAPTALK_SDK_BASE_URL;

import android.app.Activity;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDexApplication;

import java.util.ArrayList;
import java.util.List;

import io.taptalk.TapTalk.Helper.TapTalk;
import io.taptalk.TapTalk.Listener.TapUICustomKeyboardListener;
import io.taptalk.TapTalk.Manager.TapUI;
import io.taptalk.TapTalk.Model.TAPCustomKeyboardItemModel;
import io.taptalk.TapTalk.Model.TAPMessageModel;
import io.taptalk.TapTalk.Model.TAPRoomModel;
import io.taptalk.TapTalk.Model.TAPUserModel;
import io.taptalk.TapTalk.View.Activity.TapUIRoomListActivity;
import io.taptalk.meettalk.helper.MeetTalk;
import io.taptalk.meettalk.listener.MeetTalkListener;
import io.taptalk.meettalkandroidsample.activity.TAPLoginActivity;

public class SampleApplication extends MultiDexApplication {

    public static final String INSTANCE_KEY = "";

    @Override
    public void onCreate() {
        super.onCreate();
        TapTalk.setLoggingEnabled(true);
        MeetTalk.init(
                this,
                TAPTALK_SDK_APP_KEY_ID,
                TAPTALK_SDK_APP_KEY_SECRET,
                R.drawable.ic_taptalk_logo,
                getString(R.string.app_name),
                TAPTALK_SDK_BASE_URL,
                TapTalkImplementationTypeCombine,
                meetTalkListener);
        TapTalk.initializeGooglePlacesApiKey(GOOGLE_MAPS_API_KEY);

        TapUI.getInstance(INSTANCE_KEY).setLogoutButtonVisible(true);
        TapUI.getInstance(INSTANCE_KEY).setConnectionStatusIndicatorVisible(false);
//        TapUI.getInstance(INSTANCE_KEY).addCustomKeyboardListener(customKeyboardListener);
    }

    MeetTalkListener meetTalkListener = new MeetTalkListener(INSTANCE_KEY) {
        @Override
        public void onTapTalkRefreshTokenExpired() {
            TAPLoginActivity.start(getApplicationContext(), INSTANCE_KEY);
        }

        @Override
        public void onTapTalkUnreadChatRoomBadgeCountUpdated(int unreadCount) {

        }

        @Override
        public void onNotificationReceived(TAPMessageModel message) {
            super.onNotificationReceived(message);
        }

        @Override
        public void onUserLogout() {
            TAPLoginActivity.start(getApplicationContext(), INSTANCE_KEY);
        }

        @Override
        public void onTaskRootChatRoomClosed(Activity activity) {
            TapUIRoomListActivity.start(activity, INSTANCE_KEY, null, true);
        }
    };

    // TODO: TEST CUSTOM KEYBOARD FOR CONFERENCE CALL
//    TapUICustomKeyboardListener customKeyboardListener = new TapUICustomKeyboardListener() {
//        @Override
//        public List<TAPCustomKeyboardItemModel> setCustomKeyboardItems(TAPRoomModel room, TAPUserModel activeUser, @Nullable TAPUserModel recipientUser) {
//            if (room.getType() == TYPE_PERSONAL) {
//                ArrayList<TAPCustomKeyboardItemModel> customKeyboardItems = new ArrayList<>();
//                TAPCustomKeyboardItemModel voiceCallItem = new TAPCustomKeyboardItemModel("voiceCall", ContextCompat.getDrawable(SampleApplication.this, R.drawable.tap_ic_call_orange), "Voice Call");
//                customKeyboardItems.add(voiceCallItem);
//                TAPCustomKeyboardItemModel videoCallItem = new TAPCustomKeyboardItemModel("videoCall", ContextCompat.getDrawable(SampleApplication.this, R.drawable.meettalk_ic_video_camera_orange), "Video Call");
//                customKeyboardItems.add(videoCallItem);
//                return customKeyboardItems;
//            } else {
//                return null;
//            }
//        }
//
//        @Override
//        public void onCustomKeyboardItemTapped(Activity activity, TAPCustomKeyboardItemModel customKeyboardItem, TAPRoomModel room, TAPUserModel activeUser, @Nullable TAPUserModel recipientUser) {
//            if (customKeyboardItem.getItemID().equals("voiceCall")) {
//                MeetTalk.initiateNewConferenceCall(activity, INSTANCE_KEY, room);
//            }
//            else if (customKeyboardItem.getItemID().equals("videoCall")) {
//                MeetTalk.initiateNewConferenceCall(activity, INSTANCE_KEY, room, false, false);
//            }
//        }
//    };
}
