package io.taptalk.meettalk.view

import android.content.Context
import android.util.Log
import com.facebook.react.bridge.ReadableMap
import io.taptalk.TapTalk.Helper.TAPUtils
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetView
import java.lang.Exception

class MeetTalkCallView(context: Context) : JitsiMeetView(context) {
    override fun onDetachedFromWindow() {
        Log.e(">>>>>", "onDetachedFromWindow: ")
        super.onDetachedFromWindow()
    }

    override fun dispose() {
        Log.e(">>>>>", "dispose: ")
        super.dispose()
    }

    override fun onExternalAPIEvent(name: String?, data: ReadableMap?) {
        Log.e(">>>>>", "onExternalAPIEvent: $name - ${TAPUtils.toJsonString(data)}")
        super.onExternalAPIEvent(name, data)
    }

    override fun onCurrentConferenceChanged(conferenceUrl: String?) {
        Log.e(">>>>>", "onCurrentConferenceChanged: $conferenceUrl")
        super.onCurrentConferenceChanged(conferenceUrl)
    }

    override fun enterPictureInPicture() {
        Log.e(">>>>>", "enterPictureInPicture: ")
        try {
            super.enterPictureInPicture()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun join(options: JitsiMeetConferenceOptions?) {
        Log.e(">>>>>", "join:")
//        Log.e(">>>>>", "join serverURL: ${options?.serverURL.toString()}")
//        Log.e(">>>>>", "join room: ${options?.room}")
//        Log.e(">>>>>", "join subject: ${options?.subject}")
//        Log.e(">>>>>", "join token: ${options?.token}")
//        Log.e(">>>>>", "join audioMuted: ${options?.audioMuted}")
//        Log.e(">>>>>", "join audioOnly: ${options?.audioOnly}")
//        Log.e(">>>>>", "join videoMuted: ${options?.videoMuted}")
//        Log.e(">>>>>", "join colorScheme: ${TAPUtils.toJsonString(options?.colorScheme)}")
//        Log.e(">>>>>", "join featureFlags: ${TAPUtils.toJsonString(options?.featureFlags)}")
//        Log.e(">>>>>", "join userInfo: ${TAPUtils.toJsonString(options?.userInfo)}")
        super.join(options)
    }

    override fun leave() {
        Log.e(">>>>>", "leave: ")
        super.leave()
    }
}
