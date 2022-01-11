package io.taptalk.meettalk.helper

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.meettalk.BuildConfig

@RequiresApi(Build.VERSION_CODES.N)
class MeetTalkCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        if (BuildConfig.DEBUG) {
            Log.e(">>>>", "onScreenCall: ${TAPUtils.toJsonString(callDetails)}")
        }
    }
}
