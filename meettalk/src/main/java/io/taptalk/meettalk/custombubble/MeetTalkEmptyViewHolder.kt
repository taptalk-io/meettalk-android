package io.taptalk.meettalk.custombubble

import android.view.ViewGroup
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.TapTalk.Model.TAPMessageModel
import io.taptalk.TapTalk.View.Adapter.TAPBaseChatViewHolder

class MeetTalkEmptyViewHolder internal constructor(
        parent: ViewGroup,
        itemLayoutId: Int,
        private val instanceKey: String,
) :
    TAPBaseChatViewHolder(parent, itemLayoutId) {

    override fun onBind(item: TAPMessageModel?, position: Int) {
        if (null == item) {
            return
        }

        markMessageAsRead(item, TapTalk.getTapTalkActiveUser(instanceKey))
    }
}
