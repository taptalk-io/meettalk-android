package io.taptalk.meettalk.model

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TapCallNotificationModel(
    @JsonProperty("callID") var callID: Int? = null,
    @JsonProperty("event") var event: Int? = null,
    @JsonProperty("jitsiRoomName") var jitsiRoomName: String? = null,
//    @JsonProperty("message") var message: String? = null,
//    @JsonProperty("roomName") var roomName: String? = null,
//    @JsonProperty("roomImageUrl") var roomImageUrl: String? = null,
//    @JsonProperty("callerPhoneNumber") var callerPhoneNumber: String? = null,

) : Parcelable
