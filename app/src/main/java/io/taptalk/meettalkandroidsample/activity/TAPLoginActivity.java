package io.taptalk.meettalkandroidsample.activity;

import static io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.INSTANCE_KEY;
import static io.taptalk.TapTalk.Const.TAPDefaultConstant.RequestCode.REGISTER;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.taptalk.TapTalk.API.Api.TAPApiManager;
import io.taptalk.TapTalk.API.View.TAPDefaultDataView;
import io.taptalk.TapTalk.BuildConfig;
import io.taptalk.TapTalk.Helper.TapTalk;
import io.taptalk.TapTalk.Helper.TapTalkDialog;
import io.taptalk.TapTalk.Listener.TapCommonListener;
import io.taptalk.TapTalk.Manager.TAPDataManager;
import io.taptalk.TapTalk.Model.ResponseModel.TAPLoginOTPVerifyResponse;
import io.taptalk.TapTalk.Model.TAPErrorModel;
import io.taptalk.TapTalk.View.Activity.TAPBaseActivity;
import io.taptalk.TapTalk.View.Activity.TapUIRoomListActivity;
import io.taptalk.TapTalk.ViewModel.TAPLoginViewModel;
import io.taptalk.meettalkandroidsample.R;
import io.taptalk.meettalkandroidsample.fragment.TAPLoginVerificationFragment;
import io.taptalk.meettalkandroidsample.fragment.TAPPhoneLoginFragment;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tap_activity_login);
        initViewModel();
        initView();
        initFirstPage();
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
        if (BuildConfig.DEBUG) {
            String otpCode = phoneNumber.substring(phoneNumber.length() - 6);
            TAPDataManager.getInstance(instanceKey).verifyOTPLogin(otpID, otpKey, otpCode, new TAPDefaultDataView<TAPLoginOTPVerifyResponse>() {
                @Override
                public void onSuccess(TAPLoginOTPVerifyResponse response) {
                    if (response.isRegistered()) {
                        TapTalk.authenticateWithAuthTicket(instanceKey, response.getTicket(), true, new TapCommonListener() {
                            @Override
                            public void onSuccess(String successMessage) {
                                TapDevLandingActivity.Companion.start(TAPLoginActivity.this, instanceKey);
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {
                                new TapTalkDialog.Builder(TAPLoginActivity.this)
                                        .setTitle("Error Verifying OTP")
                                        .setMessage(errorMessage)
                                        .setPrimaryButtonTitle("OK")
                                        .show();
                            }
                        });
                    }
                    else {
                        TAPRegisterActivity.Companion.start(
                                TAPLoginActivity.this,
                                instanceKey,
                                countryID,
                                countryCallingID,
                                countryFlagUrl,
                                phoneNumber
                        );
                        vm.setPhoneNumber("0");
                        vm.setCountryID(0);
                        onBackPressed();
                    }
                }

                @Override
                public void onError(TAPErrorModel error) {
                    onError(error.getMessage());
                }

                @Override
                public void onError(String errorMessage) {
                    new TapTalkDialog.Builder(TAPLoginActivity.this)
                            .setTitle("Error Verifying OTP")
                            .setMessage(errorMessage)
                            .setPrimaryButtonTitle("OK")
                            .show();
                }
            });
        }
        else {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.animator.tap_slide_left_fragment, R.animator.tap_fade_out_fragment, R.animator.tap_fade_in_fragment, R.animator.tap_slide_right_fragment)
                    .replace(R.id.fl_container, TAPLoginVerificationFragment.Companion.getInstance(otpID, otpKey, phoneNumber, phoneNumberWithCode, countryID, countryCallingID, countryFlagUrl, channel, nextRequestSeconds))
                    .addToBackStack(null)
                    .commit();
        }
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
