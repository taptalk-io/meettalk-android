package io.taptalk.meettalkandroidsample.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import io.taptalk.TapTalk.API.View.TAPDefaultDataView
import io.taptalk.TapTalk.Const.TAPDefaultConstant
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.COUNTRY_ID
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.COUNTRY_LIST
import io.taptalk.TapTalk.Const.TAPDefaultConstant.RequestCode.COUNTRY_PICK
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.TapTalk.Helper.TapTalkDialog
import io.taptalk.TapTalk.Interface.TAPRequestOTPInterface
import io.taptalk.TapTalk.Manager.AnalyticsManager
import io.taptalk.TapTalk.Manager.TAPDataManager
import io.taptalk.TapTalk.Manager.TAPNetworkStateManager
import io.taptalk.TapTalk.Model.ResponseModel.TAPCountryListResponse
import io.taptalk.TapTalk.Model.ResponseModel.TAPLoginOTPResponse
import io.taptalk.TapTalk.Model.TAPCountryListItem
import io.taptalk.TapTalk.Model.TAPErrorModel
import io.taptalk.TapTalk.View.Activity.TAPBaseActivity
import io.taptalk.meettalkandroidsample.R
import io.taptalk.meettalkandroidsample.activity.TAPCountryListActivity
import io.taptalk.meettalkandroidsample.activity.TAPLoginActivity
import kotlinx.android.synthetic.main.tap_fragment_phone_login.*

class TAPPhoneLoginFragment : androidx.fragment.app.Fragment() {

    val generalErrorMessage = context?.resources?.getString(R.string.tap_error_message_general)
            ?: ""
    var countryIsoCode = "id" //Indonesia Default
    var defaultCallingCode = "62" //Indonesia Default
    var defaultCountryID = 1 //Indonesia Default
    var isNeedResetData = true //ini biar dy ga ngambil data hp setiap kali muncul halaman login

    //val oneDayAgoTimestamp = 604800000L // 7 * 24 * 60 * 60 * 1000
    private val oneDayAgoTimestamp: Long = 24 * 60 * 60 * 1000
    private var countryHashMap = mutableMapOf<String, TAPCountryListItem>()
    private var countryListitems = arrayListOf<TAPCountryListItem>()
    private var maxTime = 120L * 1000
    private var countryFlagUrl = ""
    private var previousPhoneNumber = "0"

    companion object {
        fun getInstance(): TAPPhoneLoginFragment {
            return TAPPhoneLoginFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.tap_fragment_phone_login, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lastCallCountryTimestamp = TAPDataManager.getInstance((activity as TAPBaseActivity).instanceKey).lastCallCountryTimestamp

        if (0L == lastCallCountryTimestamp || System.currentTimeMillis() - oneDayAgoTimestamp == lastCallCountryTimestamp) {
            callCountryListFromAPI()
        } else if (isNeedResetData) {
            callCountryListFromAPI()
            countryIsoCode = TAPUtils.getDeviceCountryCode(context)
            //countryHashMap = TAPDataManager.getInstance((activity as TAPBaseActivity).instanceKey).countryList
            countryListitems = TAPDataManager.getInstance((activity as TAPBaseActivity).instanceKey).countryList
            countryHashMap = countryListitems.associateBy({ it.iso2Code }, { it }).toMutableMap()
            isNeedResetData = false
            if (!countryHashMap.containsKey(countryIsoCode) || "" == countryHashMap.get(countryIsoCode)?.callingCode) {
                setCountry(defaultCountryID, defaultCallingCode, "")
            } else {
                setCountry(countryHashMap.get(countryIsoCode)?.countryID ?: 0,
                        countryHashMap.get(countryIsoCode)?.callingCode ?: "",
                        countryHashMap.get(countryIsoCode)?.flagIconUrl ?: "")
            }
        } else {
            setCountry(defaultCountryID, defaultCallingCode, countryFlagUrl)
        }
        initView()
    }

    private fun initView() {
        et_phone_number.addTextChangedListener(phoneNumberTextWatcher)

        et_phone_number.setOnEditorActionListener { v, actionId, event ->
            when (v?.length() ?: 0) {
                in 7..15 -> {
                    attemptLogin()
                }
            }
            false
        }

        et_phone_number.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                fl_phone_number.setBackgroundResource(R.drawable.tap_bg_text_field_active)
            } else {
                fl_phone_number.setBackgroundResource(R.drawable.tap_bg_text_field_inactive)
            }
        }

        enableCountryPicker()
    }

    private fun enableContinueButton() {
        enableCountryPicker()
        fl_continue_btn.setOnClickListener { attemptLogin() }
        fl_continue_btn.isClickable = true
    }

    private fun disableContinueButton() {
        disableCountryPicker()
        fl_continue_btn.setOnClickListener(null)
        fl_continue_btn.isClickable = false
    }

    private fun attemptLogin() {
        disableContinueButton()
        if (isVisible) {
            TAPUtils.dismissKeyboard(activity)
            showProgress()
            checkNumberAndCallAPI()
        }
    }

    private fun enableCountryPicker() {
        ll_country_code.setOnClickListener {
//            val activity = context as TAPBaseActivity
//            TAPCountryListActivity.start(activity, activity.instanceKey, countryListitems, defaultCountryID)
            val intent = Intent(context, TAPCountryListActivity::class.java)
            intent.putExtra(COUNTRY_LIST, countryListitems)
            intent.putExtra(COUNTRY_ID, defaultCountryID)
            startActivityForResult(intent, COUNTRY_PICK)
        }
    }

    private fun disableCountryPicker() {
        ll_country_code.setOnClickListener(null)
    }

    private fun checkAndEditPhoneNumber(): String {
        var phoneNumber = et_phone_number.text.toString().replace("-", "").trim()
        val callingCodeLength: Int = defaultCallingCode.length
        when {
            phoneNumber.isEmpty() || callingCodeLength > phoneNumber.length -> {
            }
            '0' == phoneNumber.elementAt(0) -> phoneNumber = phoneNumber.replaceFirst("0", "")
            //"+$defaultCallingCode" == phoneNumber.substring(0, (callingCodeLength + 1)) -> phoneNumber = phoneNumber.substring(3)
            defaultCallingCode == phoneNumber.substring(0, callingCodeLength) -> phoneNumber = phoneNumber.substring(callingCodeLength-1)
        }
        return phoneNumber
    }

    private fun checkNumberAndCallAPI() {
        val loginActivity = activity as TAPLoginActivity
        val loginViewModel = loginActivity.vm
        val currentOTPTimestampLength = System.currentTimeMillis() - loginViewModel.lastLoginTimestamp
        if (defaultCountryID == loginViewModel.countryID
                && checkAndEditPhoneNumber() == loginViewModel.phoneNumber
                && currentOTPTimestampLength <= maxTime) {
            requestOTPInterface.onRequestSuccess(loginViewModel.otpID, loginViewModel.otpKey, loginViewModel.phoneNumberWithCode.replaceFirst("+", ""), true, loginViewModel.channel, "", loginViewModel.waitTimeRequestOtp, "")
        } else {
            TAPDataManager.getInstance((activity as TAPBaseActivity).instanceKey).requestOTPLogin(defaultCountryID, checkAndEditPhoneNumber(), "", object : TAPDefaultDataView<TAPLoginOTPResponse>() {
                override fun onSuccess(response: TAPLoginOTPResponse) {
                    val additional = HashMap<String, String>()
                    additional.put("phoneNumber", defaultCallingCode + checkAndEditPhoneNumber())
                    additional.put("countryCode", defaultCountryID.toString())
                    AnalyticsManager.getInstance((activity as TAPBaseActivity).instanceKey).trackEvent("Request OTP Success", additional)
                    super.onSuccess(response)
                    requestOTPInterface.onRequestSuccess(response.otpID, response.otpKey, response.phoneWithCode, response.isSuccess, response.channel, response.message, response.nextRequestSeconds, response.whatsAppFailureReason)
                }

                override fun onError(error: TAPErrorModel) {
                    super.onError(error)
                    val additional = HashMap<String, String>()
                    additional.put("phoneNumber", defaultCallingCode + checkAndEditPhoneNumber())
                    additional.put("countryCode", defaultCountryID.toString())
                    AnalyticsManager.getInstance((activity as TAPBaseActivity).instanceKey).trackErrorEvent("Request OTP Failed", error.code, error.message, additional)
                    requestOTPInterface.onRequestFailed(error.message, error.code)
                }

                override fun onError(errorMessage: String) {
                    requestOTPInterface.onRequestFailed(errorMessage, "400")
                }
            })
            loginActivity.vm.lastLoginTimestamp = 0L
        }
    }

    private fun showProgress() {
        if (isVisible) {
            tv_btn_continue.visibility = View.GONE
            iv_loading_progress_request_otp.visibility = View.VISIBLE
            TAPUtils.rotateAnimateInfinitely(context, iv_loading_progress_request_otp)
        }
    }

    private fun stopAndHideProgress() {
        if (isVisible) {
            tv_btn_continue.visibility = View.VISIBLE
            iv_loading_progress_request_otp.visibility = View.GONE
            iv_loading_progress_request_otp.clearAnimation()
        }
    }

    private val requestOTPInterface = object : TAPRequestOTPInterface {
        override fun onRequestSuccess(otpID: Long, otpKey: String?, phone: String?, succeess: Boolean, channel: String, message: String, nextRequestSeconds: Int, whatsAppFailureReason: String) {
            maxTime = nextRequestSeconds * 1000L
            if (isVisible) {
                stopAndHideProgress()
                if (succeess) {
                    if (activity is TAPLoginActivity) {
                        try {
                            val phoneNumber = "+$phone"
                            val loginActivity = activity as TAPLoginActivity
                            loginActivity.setLastLoginData(otpID, otpKey, checkAndEditPhoneNumber(), phoneNumber, defaultCountryID, defaultCallingCode, channel)
                            loginActivity.showOTPVerification(otpID, otpKey, checkAndEditPhoneNumber(), phoneNumber, defaultCountryID, defaultCallingCode, countryFlagUrl, channel, nextRequestSeconds)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }else {
                    enableContinueButton()
                    if (whatsAppFailureReason == "") {
                        showDialog(getString(R.string.tap_error), message)
                    } else {
                        showDialog(getString(R.string.tap_currently_unavailable), getString(R.string.tap_error_we_are_experiencing_some_issues))
                    }
                }
            }
        }

        override fun onRequestFailed(errorMessage: String?, errorCode: String?) {
            enableContinueButton()
            if (TAPNetworkStateManager.getInstance("").hasNetworkConnection(context)) {
                showDialog(getString(R.string.tap_error), errorMessage ?: generalErrorMessage)
            } else {
                TAPUtils.showNoInternetErrorDialog(context)
                stopAndHideProgress()
            }
        }
    }

    private fun callCountryListFromAPI() {
        TAPDataManager.getInstance((activity as TAPBaseActivity).instanceKey).getCountryList(object : TAPDefaultDataView<TAPCountryListResponse>() {
            override fun startLoading() {
                et_phone_number.isEnabled = false
                tv_country_code.visibility = View.GONE
                iv_loading_progress_country.visibility = View.VISIBLE
                TAPUtils.rotateAnimateInfinitely(context, iv_loading_progress_country)
            }

            @SuppressLint("SetTextI18n")
            override fun onSuccess(response: TAPCountryListResponse?) {
                et_phone_number.isEnabled = true
                countryListitems.clear()
                TAPDataManager.getInstance((activity as TAPBaseActivity).instanceKey).saveLastCallCountryTimestamp(System.currentTimeMillis())
                setCountry(0, "", "")
                Thread {
                    var defaultCountry: TAPCountryListItem? = null
                    response?.countries?.forEach {
                        countryListitems.add(it)
                        countryHashMap.put(it.iso2Code, it)
                        if (countryIsoCode.toLowerCase() == it.iso2Code.toLowerCase() && it.iso2Code.toLowerCase() == "id") {
                            defaultCountry = it
                            activity?.runOnUiThread {
                                setCountry(it.countryID, it.callingCode, it.flagIconUrl)
                            }
                        } else if (countryIsoCode.toLowerCase() == it.iso2Code.toLowerCase()) {
                            activity?.runOnUiThread {
                                setCountry(it.countryID, it.callingCode, it.flagIconUrl)
                            }
                        } else if (it.iso2Code.toLowerCase() == "id") {
                            defaultCountry = it
                        }
                    }

                    if ("" == tv_country_code.text) {
                        val callingCode: String = defaultCountry?.callingCode ?: ""
                        activity?.runOnUiThread {
                            setCountry(defaultCountry?.countryID
                                    ?: 0, callingCode, defaultCountry?.flagIconUrl ?: "")
                        }
                    }

                    TAPDataManager.getInstance((activity as TAPBaseActivity).instanceKey).saveCountryList(countryListitems)

                    activity?.runOnUiThread {
                        iv_loading_progress_country.visibility = View.GONE
                        iv_loading_progress_country.clearAnimation()
                        tv_country_code.visibility = View.VISIBLE
                    }
                }.start()
            }

            override fun onError(error: TAPErrorModel?) {
                super.onError(error)
                iv_loading_progress_country.visibility = View.GONE
                iv_loading_progress_country.clearAnimation()
                tv_country_code.visibility = View.VISIBLE
                setCountry(0, "", "")
                showDialog("ERROR", error?.message ?: generalErrorMessage)
            }

            override fun onError(errorMessage: String?) {
                super.onError(errorMessage)
                iv_loading_progress_country.visibility = View.GONE
                iv_loading_progress_country.clearAnimation()
                tv_country_code.visibility = View.VISIBLE
                setCountry(0, "", "")
                showDialog("ERROR", errorMessage ?: generalErrorMessage)
            }
        })
    }

    private fun showDialog(title: String, message: String) {
        if (isVisible)
            TapTalkDialog.Builder(context)
                    .setDialogType(TapTalkDialog.DialogType.ERROR_DIALOG)
                    .setTitle(title)
                    .setMessage(message)
                    .setPrimaryButtonTitle(getString(R.string.tap_ok))
                    .setPrimaryButtonListener {
                        stopAndHideProgress()
                    }.show()
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            COUNTRY_PICK -> {
                when (resultCode) {
                    RESULT_OK -> {
                        val item = data?.getParcelableExtra<TAPCountryListItem>(TAPDefaultConstant.K_COUNTRY_PICK)
                        val callingCode: String = item?.callingCode ?: ""
                        setCountry(item?.countryID ?: 0, callingCode, item?.flagIconUrl ?: "")
                        val textCount = callingCode.length + checkAndEditPhoneNumber().length
                        when {
                            textCount > 15 -> {
                                et_phone_number.setText("")
                            }
                            textCount in 7..15 -> {
                                changeButtonContinueStateEnabled()
                            }
                            else -> {
                                changeButtonContinueStateDisabled()
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setCountry(countryID: Int, callingCode: String, flagIconUrl: String) {
        tv_country_code.text = "+$callingCode"
        defaultCountryID = countryID
        defaultCallingCode = callingCode
        countryFlagUrl = flagIconUrl

        if ("" != flagIconUrl)
            Glide.with(this).load(flagIconUrl).into(iv_country_flag)
        else iv_country_flag.setImageResource(R.drawable.tap_ic_default_flag)
    }

    private fun changeButtonContinueStateEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fl_continue_btn.background = ContextCompat.getDrawable(requireContext(), R.drawable.tap_bg_button_active_ripple)
        } else {
            fl_continue_btn.background = ContextCompat.getDrawable(requireContext(), R.drawable.tap_bg_button_active)
        }
        fl_continue_btn.setOnClickListener { attemptLogin() }
        fl_continue_btn.isClickable = true
    }

    private fun changeButtonContinueStateDisabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fl_continue_btn.background = ContextCompat.getDrawable(requireContext(), R.drawable.tap_bg_button_inactive_ripple)
        } else {
            fl_continue_btn.background = ContextCompat.getDrawable(requireContext(), R.drawable.tap_bg_button_inactive)
        }
        fl_continue_btn.setOnClickListener(null)
        fl_continue_btn.isClickable = false
    }

    private val phoneNumberTextWatcher = object : TextWatcher{
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            previousPhoneNumber = p0.toString()
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val textCount = /*s?.length ?: 0*/ checkAndEditPhoneNumber().length + defaultCallingCode.length
            if (textCount >= 7) {
                changeButtonContinueStateEnabled()
            } else if (textCount < 7) {
                changeButtonContinueStateDisabled()
            }
        }

        override fun afterTextChanged(p0: Editable?) {
            val textCount = /*s?.length ?: 0*/ checkAndEditPhoneNumber().length + defaultCallingCode.length
            if (textCount > 15) {
                et_phone_number.removeTextChangedListener(this)
                et_phone_number.setText(previousPhoneNumber)
                et_phone_number.addTextChangedListener(this)
                et_phone_number.setSelection(et_phone_number.length())
            }
        }
    }
}
