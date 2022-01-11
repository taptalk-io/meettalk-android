package io.taptalk.meettalk.view

import android.app.Activity
import android.content.Context
import android.util.Log
import com.facebook.react.bridge.ReadableMap
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.meettalk.BuildConfig
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetView
import java.lang.Exception

class MeetTalkCallView(context: Context) : JitsiMeetView(context) {
    override fun onDetachedFromWindow() {
        if (BuildConfig.DEBUG) {
            Log.e(">>>>> MeetTalkCallView", "onDetachedFromWindow: ")
        }
        super.onDetachedFromWindow()
    }

    override fun dispose() {
        if (BuildConfig.DEBUG) {
            Log.e(">>>>> MeetTalkCallView", "dispose: ")
        }
        super.dispose()
    }

    override fun onCurrentConferenceChanged(conferenceUrl: String?) {
        if (BuildConfig.DEBUG) {
            Log.e(">>>>> MeetTalkCallView", "onCurrentConferenceChanged: $conferenceUrl")
        }
        super.onCurrentConferenceChanged(conferenceUrl)
    }

    override fun enterPictureInPicture() {
        if (BuildConfig.DEBUG) {
            Log.e(">>>>> MeetTalkCallView", "enterPictureInPicture: ")
        }
        try {
            super.enterPictureInPicture()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun join(options: JitsiMeetConferenceOptions?) {
        if (BuildConfig.DEBUG) {
            Log.e(">>>>> MeetTalkCallView", "join:")
        }
        super.join(options)
    }

    override fun leave() {
        if (BuildConfig.DEBUG) {
            Log.e(">>>>> MeetTalkCallView", "leave: ")
        }
        super.leave()
    }
}
