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
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.CALL_CANCELLED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.CALL_ENDED
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.TARGET_BUSY
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.TARGET_MISSED_CALL
import io.taptalk.meettalk.constant.MeetTalkConstant.CallMessageType.TARGET_REJECTED_CALL

class MeetTalkCallChatBubbleViewHolder internal constructor(
        parent: ViewGroup,
        itemLayoutId: Int,
        private val instanceKey: String,
        private val listener: MeetTalkCallChatBubbleListener) :
    TAPBaseChatViewHolder(parent, itemLayoutId) {

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

    private fun isMessageFromMySelf(messageModel: TAPMessageModel): Boolean {
        return user.userID == messageModel.user.userID
    }

    override fun onBind(item: TAPMessageModel?, position: Int) {
        if (null == item) {
            return
        }
        Log.e(">>>>>>>>>>>>>", "onBind: " + TAPUtils.toJsonString(item))
        if (isMessageFromMySelf(item)) {
            // Message from active user
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

//            if (null != item.isRead && item.isRead!!) {
//                showMessageAsRead(item)
//            } else if (null != item.delivered && item.delivered!!) {
//                showMessageAsDelivered(item)
//            } else if (null != item.failedSend && item.failedSend!!) {
//                showMessageFailedToSend()
//            } else if (null != item.sending && !item.sending!!) {
//                showMessageAsSent(item)
//            } else {
//                showMessageAsSending()
//            }
        }
        else {
            // Message from others
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

        when (item.type) {
            CALL_ENDED -> {
                // Call successfully ended
                if (isMessageFromMySelf(item)) {
                    tvMessageBody.text = itemView.context.getString(R.string.meettalk_outgoing_call)
                }
                else {
                    tvMessageBody.text = itemView.context.getString(R.string.meettalk_incoming_call)
                }
                tvCallTimeDuration.text = String.format("%s - %s", TAPTimeFormatter.formatClock(item.created), "") // TODO: SET DURATION
                ivCallArrowIcon.imageTintList = ColorStateList.valueOf(itemView.context.getColor(R.color.meetTalkIconArrowCallSuccess))
            }
            CALL_CANCELLED, TARGET_BUSY -> {
                // Caller cancelled the call or target is in another call
                if (isMessageFromMySelf(item)) {
                    tvMessageBody.text = itemView.context.getString(R.string.meettalk_cancelled_call)
                }
                else {
                    tvMessageBody.text = itemView.context.getString(R.string.meettalk_missed_call)
                }
                tvCallTimeDuration.text = TAPTimeFormatter.formatClock(item.created)
                ivCallArrowIcon.imageTintList = ColorStateList.valueOf(itemView.context.getColor(R.color.meetTalkIconArrowCallFailure))
            }
            TARGET_REJECTED_CALL, TARGET_MISSED_CALL -> {
                // Target rejected or missed the call
                if (isMessageFromMySelf(item)) {
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

//    private fun showMessageAsSending() {
//        isNeedAnimateSend = true
//        flBubble.translationX = TAPUtils.dpToPx(-22).toFloat()
//        ivSending.translationX = 0f
//        ivSending.translationY = 0f
//        ivSending.alpha = 1f
//        ivMessageStatus.visibility = View.INVISIBLE
//        tvMessageStatus.visibility = View.GONE
//    }
//
//    private fun showMessageFailedToSend() {
//        ivMessageStatus.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.tap_ic_warning_red_circle_background))
//        ImageViewCompat.setImageTintList(ivMessageStatus, ColorStateList.valueOf(itemView.context.getColor(R.color.tapIconChatRoomMessageFailed)))
//        ivMessageStatus.visibility = View.VISIBLE
//        tvMessageStatus.text = itemView.context.getString(R.string.tap_message_send_failed)
//        tvMessageStatus.visibility = View.VISIBLE
//        ivSending.alpha = 0f
//        flBubble.translationX = 0f
//    }
//
//    private fun showMessageAsSent(message: TAPMessageModel?) {
//        if (!isMessageFromMySelf(message!!)) {
//            return
//        }
//        ivMessageStatus.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ttl_ic_sent_grey))
//        ImageViewCompat.setImageTintList(ivMessageStatus, ColorStateList.valueOf(itemView.context.getColor(R.color.ttlIconMessageSent)))
//        ivMessageStatus.visibility = View.VISIBLE
//        tvMessageStatus.visibility = View.VISIBLE
//        animateSend(item, flBubble, ivSending, ivMessageStatus)
//    }
//
//    private fun showMessageAsDelivered(message: TAPMessageModel?) {
//        if (isMessageFromMySelf(message!!)) {
//            ivMessageStatus.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ttl_ic_delivered_grey))
//            ImageViewCompat.setImageTintList(ivMessageStatus, ColorStateList.valueOf(itemView.context.getColor(R.color.ttlIconMessageDelivered)))
//            ivMessageStatus.visibility = View.VISIBLE
//            ivSending.alpha = 0f
//        }
//        flBubble.translationX = 0f
//        tvMessageStatus.visibility = View.VISIBLE
//    }
//
//    private fun showMessageAsRead(message: TAPMessageModel?) {
//        if (isMessageFromMySelf(message!!)) {
//            ivMessageStatus.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ttl_ic_read_orange))
//            ImageViewCompat.setImageTintList(ivMessageStatus, ColorStateList.valueOf(itemView.context.getColor(R.color.ttlIconMessageRead)))
//            ivMessageStatus.visibility = View.VISIBLE
//            ivSending.alpha = 0f
//        }
//        flBubble.translationX = 0f
//        tvMessageStatus.visibility = View.VISIBLE
//    }
//
//    private fun animateSend(item: TAPMessageModel, flBubble: FrameLayout, ivSending: ImageView, ivMessageStatus: ImageView) {
//        if (!isNeedAnimateSend) {
//            // Set bubble state to post-animation
//            flBubble.translationX = 0f
//            ivMessageStatus.translationX = 0f
//            ivSending.alpha = 0f
//        } else {
//            // Animate bubble
//            isNeedAnimateSend = false
//            isAnimating = true
//            flBubble.translationX = TAPUtils.dpToPx(-22).toFloat()
//            ivSending.translationX = 0f
//            ivSending.translationY = 0f
//            Handler().postDelayed({
//                flBubble.animate()
//                        .translationX(0f)
//                        .setDuration(160L)
//                        .start()
//                ivSending.animate()
//                        .translationX(TAPUtils.dpToPx(36).toFloat())
//                        .translationY(TAPUtils.dpToPx(-23).toFloat())
//                        .setDuration(360L)
//                        .setInterpolator(AccelerateInterpolator(0.5f))
//                        .withEndAction {
//                            ivSending.alpha = 0f
//                            isAnimating = false
//                            if (null != item.isRead && item.isRead!! ||
//                                    null != item.delivered && item.delivered!!) {
//                                //notifyItemChanged(getItems().indexOf(item))
//                                onBind(item, position)
//                            }
//                        }
//                        .start()
//            }, 200L)
//        }
//    }
}
