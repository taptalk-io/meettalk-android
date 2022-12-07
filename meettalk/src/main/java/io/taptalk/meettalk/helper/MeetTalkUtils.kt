package io.taptalk.meettalk.helper

import android.Manifest
import android.app.Activity
import androidx.core.app.ActivityCompat
import io.taptalk.TapTalk.Helper.TAPUtils
import io.taptalk.meettalk.constant.MeetTalkConstant.RequestCode.REQUEST_PERMISSION_AUDIO
import io.taptalk.meettalk.constant.MeetTalkConstant.RequestCode.REQUEST_PERMISSION_CAMERA
import java.util.concurrent.TimeUnit

class MeetTalkUtils {
    companion object {
        fun getCallDurationString(duration: Long?) : String{
            return  if (duration == null || duration == 0L) {
                ""
            }
            else if (duration < TimeUnit.HOURS.toMillis(1)) {
                String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                )
            }
            else {
                String.format(
                    "%d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(duration),
                    TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                    TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                )
            }
        }

        fun checkAndRequestAudioPermission(activity: Activity): Boolean {
            if (!TAPUtils.hasPermissions(activity, Manifest.permission.RECORD_AUDIO)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_PERMISSION_AUDIO
                )
                return false
            }
            return true
        }

        fun checkAndRequestVideoPermission(activity: Activity): Boolean {
            if (!TAPUtils.hasPermissions(activity, Manifest.permission.RECORD_AUDIO) ||
                !TAPUtils.hasPermissions(activity, Manifest.permission.CAMERA)
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA),
                    REQUEST_PERMISSION_CAMERA
                )
                return false
            }
            return true
        }
    }
}
