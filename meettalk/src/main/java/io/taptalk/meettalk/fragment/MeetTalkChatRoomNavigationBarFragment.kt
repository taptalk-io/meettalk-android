package io.taptalk.meettalk.fragment

import io.taptalk.TapTalk.View.Fragment.TapBaseChatRoomCustomNavigationBarFragment
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.RequestManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import io.taptalk.meettalk.R
import com.bumptech.glide.Glide
import io.taptalk.TapTalk.Model.TAPRoomModel
import io.taptalk.TapTalk.Model.TAPUserModel
import io.taptalk.TapTalk.Model.TAPOnlineStatusModel
import io.taptalk.TapTalk.Model.TAPMessageModel
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.TapTalk.Manager.TapUI
import androidx.core.widget.ImageViewCompat
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.engine.GlideException
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import io.taptalk.TapTalk.Helper.TAPTimeFormatter
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.taptalk.TapTalk.Const.TAPDefaultConstant
import io.taptalk.TapTalk.Const.TAPDefaultConstant.RoomType.TYPE_GROUP
import io.taptalk.TapTalk.Const.TAPDefaultConstant.RoomType.TYPE_PERSONAL
import io.taptalk.TapTalk.Helper.CircleImageView
import io.taptalk.TapTalk.Manager.TAPChatManager
import io.taptalk.TapTalk.View.Activity.TapUIChatActivity
import io.taptalk.meettalk.helper.MeetTalk

class MeetTalkChatRoomNavigationBarFragment : TapBaseChatRoomCustomNavigationBarFragment() {
    private var clRoomStatus: ConstraintLayout? = null
    private var clRoomOnlineStatus: ConstraintLayout? = null
    private var clRoomTypingStatus: ConstraintLayout? = null
    private var civRoomImage: CircleImageView? = null
    private var ivButtonBack: ImageView? = null
    private var ivRoomIcon: ImageView? = null
    private var ivRoomTypingIndicator: ImageView? = null
    private var ivButtonVoiceCall: ImageView? = null
    private var ivButtonVideoCall: ImageView? = null
    private var tvRoomName: TextView? = null
    private var tvRoomStatus: TextView? = null
    private var tvRoomImageLabel: TextView? = null
    private var tvRoomTypingStatus: TextView? = null
    private var vRoomImage: View? = null
    private var vStatusBadge: View? = null
    private var glide: RequestManager? = null
    private var lastActivityHandler: Handler? = null
    get() = if (null == field) Handler().also { field = it } else field

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.meettalk_fragment_chat_room_navigation_bar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        glide = Glide.with(this)
        bindViews(view)
        setupNavigationView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lastActivityHandler?.removeCallbacks(lastActivityRunnable) // Stop offline timer
    }

    override fun onReceiveUpdatedChatRoomData(room: TAPRoomModel, recipientUser: TAPUserModel?) {
        setRoomName()
        setRoomStatus()
        setRoomProfilePicture()
        if (null != recipientUser) {
            onlineStatus = TAPOnlineStatusModel.Builder(recipientUser)
            setChatRoomStatus(onlineStatus)
        }
    }

    override fun onReceiveStartTyping(roomID: String, user: TAPUserModel) {
        setTypingIndicator()
    }

    override fun onReceiveStopTyping(roomID: String, user: TAPUserModel) {
        setTypingIndicator()
    }

    override fun onReceiveOnlineStatus(user: TAPUserModel, isOnline: Boolean, lastActive: Long) {
        setChatRoomStatus(TAPOnlineStatusModel(user, isOnline, lastActive))
    }

    override fun onShowMessageSelection(selectedMessages: List<TAPMessageModel>) {
        showSelectState()
    }

    override fun onHideMessageSelection() {
        hideSelectState()
    }

    private fun bindViews(view: View) {
        clRoomStatus = view.findViewById(R.id.cl_room_status)
        clRoomOnlineStatus = view.findViewById(R.id.cl_room_online_status)
        clRoomTypingStatus = view.findViewById(R.id.cl_room_typing_status)
        civRoomImage = view.findViewById(R.id.civ_room_image)
        vStatusBadge = view.findViewById(R.id.v_room_status_badge)
        ivButtonBack = view.findViewById(R.id.iv_button_back)
        ivRoomIcon = view.findViewById(R.id.iv_room_icon)
        ivRoomTypingIndicator = view.findViewById(R.id.iv_room_typing_indicator)
        ivButtonVideoCall = view.findViewById(R.id.iv_button_video_call)
        ivButtonVoiceCall = view.findViewById(R.id.iv_button_voice_call)
        tvRoomName = view.findViewById(R.id.tv_room_name)
        tvRoomStatus = view.findViewById(R.id.tv_room_status)
        tvRoomImageLabel = view.findViewById(R.id.tv_room_image_label)
        tvRoomTypingStatus = view.findViewById(R.id.tv_room_typing_status)
        vRoomImage = view.findViewById(R.id.v_room_image)
    }

    private fun setupNavigationView() {
        setRoomName()
        setRoomStatus()
        setRoomProfilePicture()
        setTypingIndicator()
        ivButtonBack?.setOnClickListener { v: View? -> onBackPressed() }
        vRoomImage?.setOnClickListener { v: View? -> openRoomProfile() }

        if (room?.type == TYPE_PERSONAL) {
            ivButtonVoiceCall?.setOnClickListener { v: View? -> triggerStartVoiceCall() }
            ivButtonVideoCall?.setOnClickListener { v: View? -> triggerStartVideoCall() }
        }
        else {
            ivButtonVoiceCall?.visibility = View.GONE
            ivButtonVideoCall?.visibility = View.GONE
        }
    }

    private fun setRoomName() {
        if (TAPUtils.isSavedMessagesRoom(room?.roomID, instanceKey)) {
            tvRoomName?.setText(R.string.tap_saved_messages)
        }
        else if (
            room?.type == TYPE_PERSONAL &&
            (null == recipientUser?.deleted || recipientUser?.deleted!! <= 0L) &&
            !recipientUser.fullname.isNullOrEmpty()
        ) {
            tvRoomName?.text = recipientUser.fullname
        }
        else {
            tvRoomName?.text = room.name
        }
    }

    private fun setRoomStatus() {
        if (room.type == TYPE_GROUP && !room.participants.isNullOrEmpty()) {
            // Show number of participants for group room
            tvRoomStatus?.text = String.format(getString(R.string.tap_format_d_group_member_count), room.participants?.size)
        }
    }

    private fun setRoomProfilePicture() {
        civRoomImage?.post {
            if (!TapUI.getInstance(instanceKey).isProfileButtonVisible) {
                civRoomImage?.visibility = View.GONE
                vRoomImage?.visibility = View.GONE
                tvRoomImageLabel?.visibility = View.GONE
            }
            else if (
                room?.type == TYPE_PERSONAL &&
                null != recipientUser?.deleted &&
                recipientUser?.deleted!! > 0L
            ) {
                glide?.load(R.drawable.tap_ic_deleted_user)?.fitCenter()?.into(civRoomImage!!)
                ImageViewCompat.setImageTintList(civRoomImage!!, null)
                tvRoomImageLabel?.visibility = View.GONE
                clRoomStatus?.visibility = View.GONE
            }
            else if (
                room?.type == TYPE_PERSONAL &&
                TAPUtils.isSavedMessagesRoom(room.roomID, instanceKey)
            ) {
                glide?.load(R.drawable.tap_ic_bookmark_round)?.fitCenter()?.into(civRoomImage!!)
                ImageViewCompat.setImageTintList(civRoomImage!!, null)
                tvRoomImageLabel?.visibility = View.GONE
                clRoomStatus?.visibility = View.GONE
            }
            else if (
                room?.type == TYPE_PERSONAL &&
                !recipientUser?.imageURL?.thumbnail.isNullOrEmpty()
            ) {
                // Load user avatar URL
                loadProfilePicture(recipientUser.imageURL.thumbnail, civRoomImage!!, tvRoomImageLabel!!)
                room.imageURL = recipientUser.imageURL
            }
            else if (
                null != room &&
                !room.isDeleted &&
                !room.imageURL?.thumbnail.isNullOrEmpty()
            ) {
                // Load room image
                loadProfilePicture(room.imageURL!!.thumbnail, civRoomImage!!, tvRoomImageLabel!!)
            }
            else {
                loadInitialsToProfilePicture(civRoomImage!!, tvRoomImageLabel!!)
            }

            if (null != ivRoomIcon) {
                if (room?.type == TYPE_PERSONAL && !recipientUser?.userRole?.iconURL.isNullOrEmpty()) {
                    glide?.load(recipientUser.userRole?.iconURL)?.into(ivRoomIcon!!)
                    ivRoomIcon?.visibility = View.VISIBLE
                }
                else {
                    ivRoomIcon?.visibility = View.GONE
                }
            }
        }
    }

    private fun loadProfilePicture(image: String, imageView: ImageView, tvAvatarLabel: TextView) {
        if (imageView.visibility == View.GONE) {
            return
        }
        glide?.load(image)?.listener(object : RequestListener<Drawable?> {
            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                activity?.runOnUiThread {
                    loadInitialsToProfilePicture(imageView, tvAvatarLabel)
                }
                return false
            }

            override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                ImageViewCompat.setImageTintList(imageView, null)
                tvAvatarLabel.visibility = View.GONE
                return false
            }
        })?.into(imageView)
    }

    private fun loadInitialsToProfilePicture(imageView: ImageView, tvAvatarLabel: TextView) {
        if (imageView.visibility == View.GONE || null == context) {
            return
        }
        ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(TAPUtils.getRandomColor(context, room.name)))
        tvAvatarLabel.text = TAPUtils.getInitials(room.name, if (room.type == TYPE_PERSONAL) 2 else 1)
        imageView.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.tap_bg_circle_9b9b9b))
        tvAvatarLabel.visibility = View.VISIBLE
    }

    private fun setChatRoomStatus(onlineStatus: TAPOnlineStatusModel) {
        setOnlineStatus(onlineStatus)
        if (onlineStatus.user.userID == recipientUser.userID && onlineStatus.online) {
            // User is online
            showUserOnline()
        }
        else if (onlineStatus.user.userID == recipientUser.userID && !onlineStatus.online) {
            // User is offline
            showUserOffline()
        }
    }

    private fun showUserOnline() {
        activity?.runOnUiThread {
            if (0 >= typingUsers.size) {
                clRoomStatus?.visibility = View.VISIBLE
                clRoomTypingStatus?.visibility = View.GONE
                clRoomOnlineStatus?.visibility = View.VISIBLE
            }
            vStatusBadge?.visibility = View.VISIBLE
            vStatusBadge?.background = ContextCompat.getDrawable(activity!!, R.drawable.tap_bg_circle_active)
            tvRoomStatus?.text = getString(io.taptalk.TapTalk.R.string.tap_active_now)
            lastActivityHandler?.removeCallbacks(lastActivityRunnable)
        }
    }

    private fun showUserOffline() {
        activity?.runOnUiThread {
            if (0 >= typingUsers.size) {
                clRoomStatus?.visibility = View.VISIBLE
                clRoomTypingStatus?.visibility = View.GONE
                clRoomOnlineStatus?.visibility = View.VISIBLE
            }
            lastActivityRunnable.run()
        }
    }

    private val lastActivityRunnable: Runnable = object : Runnable {

        val INTERVAL = 1000 * 60

        override fun run() {
            val lastActive = onlineStatus?.lastActive
            if (null != lastActive && lastActive == 0L) {
                activity?.runOnUiThread {
                    vStatusBadge?.visibility = View.VISIBLE
                    vStatusBadge?.background = null
                    tvRoomStatus?.text = ""
                }
            }
            else {
                activity?.runOnUiThread {
                    vStatusBadge?.visibility = View.GONE
                    tvRoomStatus?.text = TAPTimeFormatter.getLastActivityString(activity, lastActive!!)
                }
            }
            lastActivityHandler?.postDelayed(this, INTERVAL.toLong())
        }
    }

    private fun setTypingIndicator() {
        if (typingUsers.size > 0) {
            showTypingIndicator()
        }
        else {
            hideTypingIndicator()
        }
    }

    private fun showTypingIndicator() {
        if (null == activity) {
            return
        }
        typingIndicatorTimeoutTimer.cancel()
        typingIndicatorTimeoutTimer.start()
        activity?.runOnUiThread {
            clRoomStatus?.visibility = View.VISIBLE
            clRoomTypingStatus?.visibility = View.VISIBLE
            clRoomOnlineStatus?.visibility = View.GONE
            if (TYPE_PERSONAL == room.type) {
                glide?.load(R.raw.gif_typing_indicator)?.into(ivRoomTypingIndicator!!)
                tvRoomTypingStatus?.text = getString(R.string.tap_typing)
            }
            else if (1 < typingUsers.size) {
                glide?.load(R.raw.gif_typing_indicator)?.into(ivRoomTypingIndicator!!)
                tvRoomTypingStatus?.text = String.format(getString(R.string.tap_format_d_people_typing), typingUsers.size)
            }
            else {
                glide?.load(R.raw.gif_typing_indicator)?.into(ivRoomTypingIndicator!!)
                var firstTypingUserName = ""
                if (typingUsers.size > 0) {
                    val firstTypingUser = typingUsers.entries.iterator().next().value
                    firstTypingUserName = firstTypingUser.fullname.split(" ").toTypedArray()[0]
                }
                tvRoomTypingStatus?.text = String.format(getString(R.string.tap_format_s_typing_single), firstTypingUserName)
            }
        }
    }

    private fun hideTypingIndicator() {
        if (null == activity) {
            return
        }
        typingIndicatorTimeoutTimer.cancel()
        activity?.runOnUiThread {
            typingUsers.clear()
            clRoomStatus?.visibility = View.VISIBLE
            clRoomTypingStatus?.visibility = View.GONE
            clRoomOnlineStatus?.visibility = View.VISIBLE
        }
    }

    private val typingIndicatorTimeoutTimer: CountDownTimer =
        object : CountDownTimer(TAPDefaultConstant.TYPING_INDICATOR_TIMEOUT, 1000L) {

            override fun onTick(l: Long) {

            }

            override fun onFinish() {
                hideTypingIndicator()
            }
        }

    private fun showSelectState() {
        vRoomImage?.isEnabled = false
        if (null != context) {
            ivButtonBack?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.tap_ic_close_grey))
        }
    }

    private fun hideSelectState() {
        vRoomImage?.isEnabled = true
        if (null != context) {
            ivButtonBack?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.tap_ic_chevron_left_white))
        }
    }

    private fun openRoomProfile() {
        if (null == activity) {
            return
        }
        TAPChatManager.getInstance(instanceKey).triggerChatRoomProfileButtonTapped(activity, room, recipientUser)
        if (activity is TapUIChatActivity) {
            (activity as TapUIChatActivity?)?.hideUnreadButton()
        }
    }

    private fun triggerStartVoiceCall() {
        if (null == instanceKey || null == activity || null == room) {
            return
        }
        for (listener in MeetTalk.getMeetTalkListeners(instanceKey)) {
            listener.onChatRoomVoiceCallButtonTapped(instanceKey, activity, room)
        }
    }

    private fun triggerStartVideoCall() {
        if (null == instanceKey || null == activity || null == room) {
            return
        }
        for (listener in MeetTalk.getMeetTalkListeners(instanceKey)) {
            listener.onChatRoomVideoCallButtonTapped(instanceKey, activity, room)
        }
    }
}
