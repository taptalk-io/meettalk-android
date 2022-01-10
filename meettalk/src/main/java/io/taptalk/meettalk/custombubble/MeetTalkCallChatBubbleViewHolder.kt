package io.taptalk.meettalk.custombubble

import android.app.Activity
import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import io.taptalk.TapTalk.Const.TAPDefaultConstant.RoomType.TYPE_PERSONAL
import io.taptalk.TapTalk.Helper.CircleImageView
import io.taptalk.TapTalk.Helper.TAPTimeFormatter
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.TapTalk.Model.TAPMessageModel
import io.taptalk.TapTalk.View.Adapter.TAPBaseChatViewHolder
import io.taptalk.meettalk.BuildConfig
import io.taptalk.meettalk.R
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.CALL_CANCELLED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.CALL_ENDED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.RECIPIENT_BUSY
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.RECIPIENT_MISSED_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageAction.RECIPIENT_REJECTED_CALL
import io.taptalk.meettalk.helper.MeetTalkUtils
import io.taptalk.meettalk.model.MeetTalkConferenceInfo

class MeetTalkCallChatBubbleViewHolder internal constructor(
        parent: ViewGroup,
        itemLayoutId: Int,
        private val instanceKey: String,
        private val listener: MeetTalkCallChatBubbleListener) :
    TAPBaseChatViewHolder(parent, itemLayoutId) {

    private val clContainer: ConstraintLayout = itemView.findViewById(R.id.cl_container)
    private val clBubble: ConstraintLayout = itemView.findViewById(R.id.cl_bubble)
    private val flBubble: FrameLayout = itemView.findViewById(R.id.fl_bubble)
    private val civAvatar: CircleImageView = itemView.findViewById(R.id.civ_avatar)
    private val ivCallArrowIcon: ImageView = itemView.findViewById(R.id.iv_call_arrow_icon)
    private val ivButtonCall: ImageView = itemView.findViewById(R.id.iv_button_call)
    private val tvAvatarLabel: TextView = itemView.findViewById(R.id.tv_avatar_label)
    private val tvUserName: TextView = itemView.findViewById(R.id.tv_user_name)
    private val tvMessageBody: TextView = itemView.findViewById(R.id.tv_message_body)
    private val tvCallTimeDuration: TextView = itemView.findViewById(R.id.tv_call_time_duration)
    private val vMarginLeft: View = itemView.findViewById(R.id.v_margin_left)
    private val vMarginRight: View = itemView.findViewById(R.id.v_margin_right)

    private val user = TapTalk.getTapTalkActiveUser(instanceKey)

    private fun isCallHostedByActiveUser(messageModel: TAPMessageModel): Boolean {
        val conferenceInfo = MeetTalkConferenceInfo.fromMessageModel(messageModel) ?: return false
        return user.userID == conferenceInfo.hostUserID
    }

    override fun onBind(item: TAPMessageModel?, position: Int) {
        if (null == item) {
            return
        }

        if (item.action != CALL_ENDED &&
            item.action != CALL_CANCELLED &&
            item.action != RECIPIENT_BUSY &&
            item.action != RECIPIENT_REJECTED_CALL &&
            item.action != RECIPIENT_MISSED_CALL
        ) {
            clContainer.visibility = View.GONE
            clContainer.layoutParams.height = 0
            return
        }

        clContainer.visibility = View.VISIBLE
        clContainer.layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT

        if (isCallHostedByActiveUser(item)) {
            // Active user is the call host
            clBubble.background = ContextCompat.getDrawable(itemView.context, R.drawable.tap_bg_chat_bubble_right_default)
            ivButtonCall.imageTintList = ColorStateList.valueOf(itemView.context.getColor(R.color.meetTalkRightCallBubblePhoneIconColor))
            ivButtonCall.backgroundTintList = ColorStateList.valueOf(itemView.context.getColor(R.color.meetTalkRightCallBubblePhoneIconBackgroundColor))
            tvMessageBody.setTextAppearance(R.style.meetTalkRightCallBubbleMessageBodyStyle)
            tvCallTimeDuration.setTextAppearance(R.style.meetTalkRightCallBubbleTimestampDurationStyle)
            ivCallArrowIcon.rotation = 180f
            civAvatar.visibility = View.GONE
            tvAvatarLabel.visibility = View.GONE
            tvUserName.visibility = View.GONE
            vMarginRight.visibility = View.GONE
            vMarginLeft.visibility = View.VISIBLE
        }
        else {
            // Active user is not host
            clBubble.background = ContextCompat.getDrawable(itemView.context, R.drawable.tap_bg_chat_bubble_left_default)
            ivButtonCall.imageTintList = ColorStateList.valueOf(itemView.context.getColor(R.color.meetTalkLeftCallBubblePhoneIconColor))
            ivButtonCall.backgroundTintList = ColorStateList.valueOf(itemView.context.getColor(R.color.meetTalkLeftCallBubblePhoneIconBackgroundColor))
            tvMessageBody.setTextAppearance(R.style.meetTalkLeftCallBubbleMessageBodyStyle)
            tvCallTimeDuration.setTextAppearance(R.style.meetTalkLeftCallBubbleTimestampDurationStyle)
            ivCallArrowIcon.rotation = 0f
            vMarginRight.visibility = View.VISIBLE
            vMarginLeft.visibility = View.GONE

            if (item.room.type == TYPE_PERSONAL) {
                // Hide avatar and name for personal room
                civAvatar.visibility = View.GONE
                tvAvatarLabel.visibility = View.GONE
                tvUserName.visibility = View.GONE
            }
            else {
                // Load avatar and name for other room types
                if (null != user && null != user.imageURL && user.imageURL.thumbnail.isNotEmpty()) {
                    Glide.with(itemView.context).load(user.imageURL.thumbnail).into(civAvatar)
                    civAvatar.imageTintList = null
                    civAvatar.visibility = View.VISIBLE
                    tvAvatarLabel.visibility = View.GONE
                }
                else if (null != item.user.imageURL && item.user.imageURL.thumbnail.isNotEmpty()) {
                    Glide.with(itemView.context).load(item.user.imageURL.thumbnail).into(civAvatar)
                    civAvatar.imageTintList = null
                    civAvatar.visibility = View.VISIBLE
                    tvAvatarLabel.visibility = View.GONE
                }
                else {
                    civAvatar.imageTintList = ColorStateList.valueOf(TAPUtils.getRandomColor(itemView.context, item.user.fullname))
                    civAvatar.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.tap_bg_circle_9b9b9b))
                    tvAvatarLabel.text = TAPUtils.getInitials(item.user.fullname, 2)
                    civAvatar.visibility = View.VISIBLE
                    tvAvatarLabel.visibility = View.VISIBLE
                }
                tvUserName.text = item.user.fullname
                tvUserName.visibility = View.VISIBLE
            }
        }

        when (item.action) {
            CALL_ENDED -> {
                // Call successfully ended
                if (isCallHostedByActiveUser(item)) {
                    tvMessageBody.text = itemView.context.getString(R.string.meettalk_outgoing_call)
                }
                else {
                    tvMessageBody.text = itemView.context.getString(R.string.meettalk_incoming_call)
                }
                val conferenceInfo = MeetTalkConferenceInfo.fromMessageModel(item)
                val durationString = MeetTalkUtils.getCallDurationString(conferenceInfo?.callDuration)
                tvCallTimeDuration.text = String.format("%s - %s", TAPTimeFormatter.formatClock(item.created), durationString)
                ivCallArrowIcon.imageTintList = ColorStateList.valueOf(itemView.context.getColor(R.color.meetTalkIconArrowCallSuccess))
            }
            CALL_CANCELLED, RECIPIENT_BUSY -> {
                // Caller cancelled the call or recipient is in another call
                if (isCallHostedByActiveUser(item)) {
                    tvMessageBody.text = itemView.context.getString(R.string.meettalk_cancelled_call)
                }
                else {
                    tvMessageBody.text = itemView.context.getString(R.string.meettalk_missed_call)
                }
                tvCallTimeDuration.text = TAPTimeFormatter.formatClock(item.created)
                ivCallArrowIcon.imageTintList = ColorStateList.valueOf(itemView.context.getColor(R.color.meetTalkIconArrowCallFailure))
            }
            RECIPIENT_REJECTED_CALL, RECIPIENT_MISSED_CALL -> {
                // Recipient rejected or missed the call
                if (isCallHostedByActiveUser(item)) {
                    tvMessageBody.text = itemView.context.getString(R.string.meettalk_outgoing_call)
                }
                else {
                    tvMessageBody.text = itemView.context.getString(R.string.meettalk_missed_call)
                }
                tvCallTimeDuration.text = TAPTimeFormatter.formatClock(item.created)
                ivCallArrowIcon.imageTintList = ColorStateList.valueOf(itemView.context.getColor(R.color.meetTalkIconArrowCallFailure))
            }
            else -> {
                tvMessageBody.text = ""
                tvCallTimeDuration.text = ""
                ivCallArrowIcon.imageTintList = null
            }
        }

        ivButtonCall.setOnClickListener { onCallButtonTapped() }

        markMessageAsRead(item, TapTalk.getTapTalkActiveUser(instanceKey))

        if (BuildConfig.DEBUG) {
            itemView.setOnLongClickListener{
                Log.d(this.javaClass.simpleName, "Message model: " + TAPUtils.toJsonString(item))
                return@setOnLongClickListener true
            }
        }
    }

    private fun onCallButtonTapped() {
        if (itemView.context is Activity) {
            listener.onCallButtonTapped(itemView.context as Activity, item)
        }
    }
}
