package io.taptalk.meettalk.custombubble

import android.app.Activity
import io.taptalk.TapTalk.Interface.TapTalkBaseCustomInterface
import io.taptalk.TapTalk.Model.TAPMessageModel

interface MeetTalkCallChatBubbleListener : TapTalkBaseCustomInterface {
    fun onCallButtonTapped(activity: Activity, message: TAPMessageModel)
}
