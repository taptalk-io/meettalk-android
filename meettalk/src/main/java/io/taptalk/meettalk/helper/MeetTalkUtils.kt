package io.taptalk.meettalk.helper

import java.util.concurrent.TimeUnit

class MeetTalkUtils {
    companion object {
        fun getCallDurationString(duration: Long?) : String{
            return  if (duration == null || duration == 0L) {
                ""
            }
            else if (duration < TimeUnit.MINUTES.toMillis(1)) {
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
    }
}
