package io.taptalk.meettalkandroidsample;

import static io.taptalk.TapTalk.Const.TAPDefaultConstant.RoomType.TYPE_PERSONAL;
import static io.taptalk.TapTalk.Helper.TapTalk.TapTalkImplementationType.TapTalkImplementationTypeCombine;
import static io.taptalk.meettalkandroidsample.BuildConfig.GOOGLE_MAPS_API_KEY;
import static io.taptalk.meettalkandroidsample.BuildConfig.TAPTALK_SDK_APP_KEY_ID;
import static io.taptalk.meettalkandroidsample.BuildConfig.TAPTALK_SDK_APP_KEY_SECRET;
import static io.taptalk.meettalkandroidsample.BuildConfig.TAPTALK_SDK_BASE_URL;

import android.app.Activity;
import android.os.Build;

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
import io.taptalk.meettalk.manager.TapCallManager;
import io.taptalk.meettalkandroidsample.activity.TAPLoginActivity;

public class SampleApplication extends MultiDexApplication {

    public static final String INSTANCE_KEY = "";

    @Override
    public void onCreate() {
        super.onCreate();
        TapTalk.setLoggingEnabled(true);
//        if (BuildConfig.BUILD_TYPE.equals("release")) {
//            TapTalk.initializeAnalyticsForSampleApps("b476744eb06c9b3285d19dca3d7781c7");
//        } else if (BuildConfig.BUILD_TYPE.equals("staging")) {
//            TapTalk.initializeAnalyticsForSampleApps("1b400091d6ab3e08584cadffd57a7a40");
//        } else {
//            TapTalk.initializeAnalyticsForSampleApps("84f4d93bf3c34abe56fac7b2faaaa8b1");
//        }
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

//        Stetho.initialize(
//                Stetho.newInitializerBuilder(this)
//                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
//                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
//                        .build());

        TapUI.getInstance(INSTANCE_KEY).setLogoutButtonVisible(true);
        TapUI.getInstance(INSTANCE_KEY).setConnectionStatusIndicatorVisible(false);
        TapUI.getInstance(INSTANCE_KEY).addCustomKeyboardListener(customKeyboardListener);

        if (BuildConfig.DEBUG) {
            TapUI.getInstance(INSTANCE_KEY).setCloseButtonInRoomListVisible(true);
        }
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
            TapTalk.showTapTalkNotification(INSTANCE_KEY, message);
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
    TapUICustomKeyboardListener customKeyboardListener = new TapUICustomKeyboardListener() {
        @Override
        public List<TAPCustomKeyboardItemModel> setCustomKeyboardItems(TAPRoomModel room, TAPUserModel activeUser, @Nullable TAPUserModel recipientUser) {
            if (room.getType() == TYPE_PERSONAL) {
                ArrayList<TAPCustomKeyboardItemModel> customKeyboardItems = new ArrayList<>();
                TAPCustomKeyboardItemModel voiceCallItem = new TAPCustomKeyboardItemModel("voiceCall", ContextCompat.getDrawable(SampleApplication.this, R.drawable.tap_ic_call_orange), "Voice Call");
                customKeyboardItems.add(voiceCallItem);
                return customKeyboardItems;
            } else {
                return null;
            }
        }

        @Override
        public void onCustomKeyboardItemTapped(Activity activity, TAPCustomKeyboardItemModel customKeyboardItem, TAPRoomModel room, TAPUserModel activeUser, @Nullable TAPUserModel recipientUser) {
            if (customKeyboardItem.getItemID().equals("voiceCall")) {
                // TODO: CALL API TO GET JITSI ROOM NAME (ID)
                MeetTalk.initiateNewConferenceCall(activity, INSTANCE_KEY, room);
            }
        }
    };
}