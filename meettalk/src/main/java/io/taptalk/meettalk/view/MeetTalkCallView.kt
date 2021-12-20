package io.taptalk.meettalk.view

import android.app.Activity
import android.content.Context
import android.util.Log
import com.facebook.react.bridge.ReadableMap
import io.taptalk.TapTalk.Helper.TAPUtils
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetView
import java.lang.Exception

class MeetTalkCallView(context: Context) : JitsiMeetView(context) {
    override fun onDetachedFromWindow() {
        Log.e(">>>>> MeetTalkCallView", "onDetachedFromWindow: ")
        super.onDetachedFromWindow()
    }

    override fun dispose() {
        Log.e(">>>>> MeetTalkCallView", "dispose: ")
        super.dispose()
    }

    override fun onExternalAPIEvent(name: String?, data: ReadableMap?) {
//        Log.e(">>>>> MeetTalkCallView", "onExternalAPIEvent: $name - ${TAPUtils.toJsonString(data)}")
        super.onExternalAPIEvent(name, data)
    }

    override fun onCurrentConferenceChanged(conferenceUrl: String?) {
        Log.e(">>>>> MeetTalkCallView", "onCurrentConferenceChanged: $conferenceUrl")
        super.onCurrentConferenceChanged(conferenceUrl)
    }

    override fun enterPictureInPicture() {
        Log.e(">>>>> MeetTalkCallView", "enterPictureInPicture: ")
        try {
            super.enterPictureInPicture()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun join(options: JitsiMeetConferenceOptions?) {
        Log.e(">>>>> MeetTalkCallView", "join:")
//        Log.e(">>>>> MeetTalkCallView", "join serverURL: ${options?.serverURL.toString()}")
//        Log.e(">>>>> MeetTalkCallView", "join room: ${options?.room}")
//        Log.e(">>>>> MeetTalkCallView", "join subject: ${options?.subject}")
//        Log.e(">>>>> MeetTalkCallView", "join token: ${options?.token}")
//        Log.e(">>>>> MeetTalkCallView", "join audioMuted: ${options?.audioMuted}")
//        Log.e(">>>>> MeetTalkCallView", "join audioOnly: ${options?.audioOnly}")
//        Log.e(">>>>> MeetTalkCallView", "join videoMuted: ${options?.videoMuted}")
//        Log.e(">>>>> MeetTalkCallView", "join colorScheme: ${TAPUtils.toJsonString(options?.colorScheme)}")
//        Log.e(">>>>> MeetTalkCallView", "join featureFlags: ${TAPUtils.toJsonString(options?.featureFlags)}")
//        Log.e(">>>>> MeetTalkCallView", "join userInfo: ${TAPUtils.toJsonString(options?.userInfo)}")
        super.join(options)
    }

    override fun leave() {
        Log.e(">>>>> MeetTalkCallView", "leave: ")
        super.leave()
        if (context is Activity) {
            Log.e(">>>>> MeetTalkCallView", "(context as Activity).onBackPressed()")
            (context as Activity).onBackPressed()
        }
    }
}
