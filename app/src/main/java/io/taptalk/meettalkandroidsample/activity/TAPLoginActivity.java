package io.taptalk.meettalkandroidsample.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.taptalk.TapTalk.BuildConfig;
import io.taptalk.meettalk.helper.MeetTalk;
import io.taptalk.meettalkandroidsample.R;
import io.taptalk.meettalkandroidsample.fragment.TAPLoginVerificationFragment;
import io.taptalk.meettalkandroidsample.fragment.TAPPhoneLoginFragment;
import io.taptalk.TapTalk.API.Api.TAPApiManager;
import io.taptalk.TapTalk.View.Activity.TAPBaseActivity;
import io.taptalk.TapTalk.View.Activity.TapUIRoomListActivity;
import io.taptalk.TapTalk.ViewModel.TAPLoginViewModel;

import static io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.INSTANCE_KEY;
import static io.taptalk.TapTalk.Const.TAPDefaultConstant.RequestCode.REGISTER;

public class TAPLoginActivity extends TAPBaseActivity {

    private static final String TAG = TAPLoginActivity.class.getSimpleName();
    private FrameLayout flContainer;
    private TAPLoginViewModel vm;

    public static void start(
            Context context,
            String instanceKey) {
        start(context, instanceKey, true);
    }

    public static void start(
            Context context,
            String instanceKey,
            boolean newTask) {
        Intent intent = new Intent(context, TAPLoginActivity.class);
        intent.putExtra(INSTANCE_KEY, instanceKey);
        if (newTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tap_activity_login);
        initViewModel();
        initView();
        initFirstPage();

        MeetTalk.checkAndRequestEnablePhoneAccountSettings(INSTANCE_KEY, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REGISTER:
                    TAPApiManager.getInstance(instanceKey).setLoggedOut(false);
                    if (BuildConfig.DEBUG) {
                        TapDevLandingActivity.Companion.start(TAPLoginActivity.this, instanceKey);
                    } else {
                        TapUIRoomListActivity.start(TAPLoginActivity.this, instanceKey);
                    }
                    finish();
                    break;
            }
        }
    }

    private void initView() {
        flContainer = findViewById(R.id.fl_container);
    }

    public void initFirstPage() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_container, TAPPhoneLoginFragment.Companion.getInstance())
                .commit();
    }

    public void showPhoneLogin() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.tap_slide_left_fragment, R.animator.tap_fade_out_fragment, R.animator.tap_fade_in_fragment, R.animator.tap_slide_right_fragment)
                .replace(R.id.fl_container, TAPPhoneLoginFragment.Companion.getInstance())
                .addToBackStack(null)
                .commit();
    }

    public void showOTPVerification(Long otpID, String otpKey, String phoneNumber, String phoneNumberWithCode, int countryID, String countryCallingID, String countryFlagUrl, String channel, int nextRequestSeconds) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.tap_slide_left_fragment, R.animator.tap_fade_out_fragment, R.animator.tap_fade_in_fragment, R.animator.tap_slide_right_fragment)
                .replace(R.id.fl_container, TAPLoginVerificationFragment.Companion.getInstance(otpID, otpKey, phoneNumber, phoneNumberWithCode, countryID, countryCallingID, countryFlagUrl, channel, nextRequestSeconds))
                .addToBackStack(null)
                .commit();
    }

    public void setLastLoginData(Long otpID, String otpKey, String phoneNumber, String phoneNumberWithCode, int countryID, String countryCallingID, String channel) {
        vm.setLastLoginData(otpID, otpKey, phoneNumber, phoneNumberWithCode, countryID, countryCallingID, channel);
    }

    private void initViewModel() {
        vm = new ViewModelProvider(this).get(TAPLoginViewModel.class);
    }

    public TAPLoginViewModel getVm() {
        return null == vm ? vm = new ViewModelProvider(this).get(TAPLoginViewModel.class) : vm;
    }
}
