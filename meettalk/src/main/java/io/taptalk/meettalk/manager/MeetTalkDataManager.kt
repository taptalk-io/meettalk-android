package io.taptalk.meettalk.manager

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import io.taptalk.meettalk.constant.MeetTalkConstant.Preference.ENABLE_PHONE_ACCOUNT_SETTINGS_REQUESTED_APP_NAME
import io.taptalk.meettalk.helper.MeetTalk

class MeetTalkDataManager (val instanceKey: String) {

    companion object {
        private var instances: HashMap<String, MeetTalkDataManager>? = null

        private fun getInstances(): HashMap<String, MeetTalkDataManager> {
            if (instances == null) {
                instances = HashMap()
            }
            return instances!!
        }

        fun getInstance(instanceKey: String): MeetTalkDataManager {
            if (!getInstances().containsKey(instanceKey)) {
                val instance = MeetTalkDataManager(instanceKey)
                getInstances()[instanceKey] = instance
            }
            return getInstances()[instanceKey]!!
        }
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getEnablePhoneAccountSettingsRequestedAppName(): String {
        if (MeetTalk.appContext == null) {
            return ""
        }
        return getSharedPreferences(MeetTalk.appContext).getString(instanceKey + ENABLE_PHONE_ACCOUNT_SETTINGS_REQUESTED_APP_NAME, "") ?: ""
//        if (null == userString) {
//            return null
//        }
//        else {
//            return TAPUtils.fromJSON(object : TypeReference<User?>() {}, userString)
//        }
    }

    fun setEnablePhoneAccountSettingsRequestedAppName(appName: String) {
        if (MeetTalk.appContext == null) {
            return
        }
//        val userString: String = TAPUtils.toJsonString(user)
        getSharedPreferences(MeetTalk.appContext).edit().putString(instanceKey + ENABLE_PHONE_ACCOUNT_SETTINGS_REQUESTED_APP_NAME, appName).apply()
    }

    fun clearAllPreferences() {
        if (MeetTalk.appContext == null) {
            return
        }
        getSharedPreferences(MeetTalk.appContext).edit().remove(instanceKey + ENABLE_PHONE_ACCOUNT_SETTINGS_REQUESTED_APP_NAME).apply()
        // Update when adding more methods
    }
}
