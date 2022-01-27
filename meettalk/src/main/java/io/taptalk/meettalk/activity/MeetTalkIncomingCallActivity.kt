package io.taptalk.meettalk.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.INSTANCE_KEY
import io.taptalk.TapTalk.Const.TAPDefaultConstant.Extras.MESSAGE
import io.taptalk.TapTalk.Const.TAPDefaultConstant.RoomType.TYPE_PERSONAL
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.TapTalk.Helper.TapTalk
import io.taptalk.TapTalk.Model.TAPMessageModel
import io.taptalk.TapTalk.R.anim.tap_fade_out
import io.taptalk.TapTalk.R.anim.tap_stay
import io.taptalk.meettalk.R
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_ANSWERED
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_NOTIFICATION_CONTENT
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_NOTIFICATION_TITLE
import io.taptalk.meettalk.constant.MeetTalkConstant.Extra.INCOMING_CALL_REJECTED
import io.taptalk.meettalk.helper.MeetTalk
import io.taptalk.meettalk.manager.MeetTalkCallManager
import kotlinx.android.synthetic.main.meettalk_activity_incoming_call.*

class MeetTalkIncomingCallActivity : AppCompatActivity() {

    private lateinit var instanceKey: String

    private var isNotificationSent = false
    private var shouldNotSendRejectCallNotification = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.meettalk_activity_incoming_call)

        MeetTalkCallManager.activeMeetTalkIncomingCallActivity = this

        initDataAndView()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleCallIntent(intent)
    }

    override fun onDestroy() {
        MeetTalkCallManager.closeIncomingCallNotification(this)
        MeetTalkCallManager.clearPendingIncomingCall()
        MeetTalkCallManager.activeMeetTalkIncomingCallActivity = null

        // Trigger listener callback
        for (meetTalkListener in MeetTalk.getMeetTalkListeners(MeetTalkCallManager.activeCallInstanceKey)) {
            meetTalkListener.onIncomingCallDisconnected()
        }

        super.onDestroy()
    }

    override fun finish() {
        if (isFinishing) {
            return
        }
        if (!isNotificationSent && !shouldNotSendRejectCallNotification) {
            rejectCall()
        }
        else {
            runOnUiThread {
                super.finish()
                overridePendingTransition(tap_stay, tap_fade_out)
            }
        }
    }

    private fun initDataAndView() {
        instanceKey = intent?.getStringExtra(INSTANCE_KEY) ?: ""
        handleCallIntent(intent)
        initView(
            intent.getParcelableExtra(MESSAGE),
            intent.getStringExtra(INCOMING_CALL_NOTIFICATION_TITLE) ?: "",
            intent.getStringExtra(INCOMING_CALL_NOTIFICATION_CONTENT) ?: ""
        )
    }

    private fun initView(message: TAPMessageModel?, title: String, content: String) {
        if (!message?.room?.imageURL?.thumbnail.isNullOrEmpty()) {
            loadRoomImage(message)
        } else {
            showInitial(
                message?.room?.name ?: TapTalk.getClientAppName(instanceKey),
                if (message?.room?.type == TYPE_PERSONAL) 2 else 1
            )
        }

        tv_incoming_call_title.text = title
        tv_incoming_call_content.text = content

        iv_button_answer_call.setOnClickListener { answerCall() }
        iv_button_reject_call.setOnClickListener { rejectCall() }
    }

    private fun loadRoomImage(message: TAPMessageModel?) {
        Glide.with(this)
            .load(message?.room?.imageURL?.thumbnail)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable?>, isFirstResource: Boolean): Boolean {
                    showInitial(
                        message?.room?.name ?: TapTalk.getClientAppName(instanceKey),
                        if (message?.room?.type == TYPE_PERSONAL) 2 else 1
                    )
                    return false
                }

                override fun onResourceReady(resource: Drawable?, model: Any, target: Target<Drawable?>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    return false
                }
            })
            .into(civ_avatar)
        ImageViewCompat.setImageTintList(civ_avatar, null)
        tv_avatar_label.visibility = View.GONE
    }

    private fun showInitial(roomName: String, length: Int) {
        val defaultAvatarBackgroundColor = TAPUtils.getRandomColor(TapTalk.appContext, roomName)
        Glide.with(this).clear(civ_avatar)
        ImageViewCompat.setImageTintList(civ_avatar, ColorStateList.valueOf(defaultAvatarBackgroundColor))
        civ_avatar.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.tap_bg_circle_9b9b9b))
        tv_avatar_label.text = TAPUtils.getInitials(roomName, length)
        tv_avatar_label.visibility = View.VISIBLE
    }

    private fun handleCallIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(INCOMING_CALL_ANSWERED, false) == true) {
            answerCall()
        }
        else if (intent?.getBooleanExtra(INCOMING_CALL_REJECTED, false) == true) {
            rejectCall()
        }
    }

    private fun answerCall() {
        MeetTalkCallManager.answerIncomingCall()
        isNotificationSent = true
        finish()
    }

    private fun rejectCall() {
        MeetTalkCallManager.rejectIncomingCall()
        isNotificationSent = true
        finish()
    }

    fun closeIncomingCall() {
        shouldNotSendRejectCallNotification = true
        finish()
    }
}
