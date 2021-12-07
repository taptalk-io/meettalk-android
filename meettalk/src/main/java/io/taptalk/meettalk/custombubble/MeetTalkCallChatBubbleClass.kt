package io.taptalk.meettalk.custombubble

import android.view.ViewGroup
import io.taptalk.TapTalk.Helper.TAPBaseCustomBubble
import io.taptalk.TapTalk.Model.TAPUserModel
import io.taptalk.TapTalk.View.Adapter.TAPMessageAdapter

class MeetTalkCallChatBubbleClass(
    private val instanceKey: String,
    customBubbleLayoutRes: Int,
    messageType: Int,
    listener: MeetTalkCallChatBubbleListener
) : TAPBaseCustomBubble<MeetTalkCallChatBubbleViewHolder, MeetTalkCallChatBubbleListener>(
    customBubbleLayoutRes,
    messageType,
    listener
) {
    override fun createCustomViewHolder(
        parent: ViewGroup,
        adapter: TAPMessageAdapter,
        activeUser: TAPUserModel?,
        customBubbleListener: MeetTalkCallChatBubbleListener
    ): MeetTalkCallChatBubbleViewHolder {
        return MeetTalkCallChatBubbleViewHolder(parent, customBubbleLayoutRes, instanceKey, customBubbleListener)
    }
}
