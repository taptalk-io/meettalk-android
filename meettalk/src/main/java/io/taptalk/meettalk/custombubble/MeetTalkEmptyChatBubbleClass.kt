package io.taptalk.meettalk.custombubble

import android.view.ViewGroup
import io.taptalk.TapTalk.Helper.TAPBaseCustomBubble
import io.taptalk.TapTalk.Model.TAPUserModel
import io.taptalk.TapTalk.View.Adapter.TAPMessageAdapter

class MeetTalkEmptyChatBubbleClass(
    private val instanceKey: String,
    customBubbleLayoutRes: Int,
    messageType: Int,
    listener: MeetTalkEmptyListener
) : TAPBaseCustomBubble<MeetTalkEmptyViewHolder, MeetTalkEmptyListener>(
    customBubbleLayoutRes,
    messageType,
    listener
) {
    override fun createCustomViewHolder(
        parent: ViewGroup,
        adapter: TAPMessageAdapter,
        activeUser: TAPUserModel?,
        customBubbleListener: MeetTalkEmptyListener
    ): MeetTalkEmptyViewHolder {
        return MeetTalkEmptyViewHolder(parent, customBubbleLayoutRes, instanceKey)
    }
}
