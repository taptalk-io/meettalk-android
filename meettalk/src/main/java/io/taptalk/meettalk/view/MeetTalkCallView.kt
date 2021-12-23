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
        super.join(options)
    }

    override fun leave() {
        Log.e(">>>>> MeetTalkCallView", "leave: ")
        super.leave()
    }
}
