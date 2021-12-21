package io.taptalk.meettalk.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;

import io.taptalk.TapTalk.Helper.TAPUtils;
import io.taptalk.TapTalk.Model.TAPMessageModel;

public class MeetTalkParticipantInfo implements Parcelable {

    @JsonProperty("userID") private String userID;
    @JsonProperty("participantID") private String participantID;
    @JsonProperty("displayName") private String displayName;
    @JsonProperty("imageURL") private String imageURL;
    @JsonProperty("role") private String role;
    @JsonProperty("lastUpdated") private Long lastUpdated;
    @Nullable @JsonProperty("isAudioMuted") private Boolean isAudioMuted;
    @Nullable @JsonProperty("isVideoMuted") private Boolean isVideoMuted;

    public MeetTalkParticipantInfo() {

    }

    public MeetTalkParticipantInfo(
            String userID,
            String participantID,
            String displayName,
            String imageURL,
            String role,
            Long lastUpdated,
            @Nullable Boolean isAudioMuted,
            @Nullable Boolean isVideoMuted
    ) {
        this.userID = userID;
        this.participantID = participantID;
        this.displayName = displayName;
        this.imageURL = imageURL;
        this.role = role;
        this.lastUpdated = lastUpdated;
        this.isAudioMuted = isAudioMuted;
        this.isVideoMuted = isVideoMuted;
    }

    public static MeetTalkParticipantInfo fromHashMap(HashMap<String, Object> hashMap) {
        try {
            return TAPUtils.convertObject(hashMap, new TypeReference<MeetTalkParticipantInfo>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    public static MeetTalkParticipantInfo fromMessageModel(TAPMessageModel message) {
        try {
            return fromHashMap(message.getData());
        } catch (Exception e) {
            return null;
        }
    }

    public String getUserID() {
        if (userID == null) {
            return "";
        }
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getParticipantID() {
        if (participantID == null) {
            return "";
        }
        return participantID;
    }

    public void setParticipantID(String participantID) {
        this.participantID = participantID;
    }

    public String getDisplayName() {
        if (displayName == null) {
            return "";
        }
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getImageURL() {
        if (imageURL == null) {
            return "";
        }
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getRole() {
        if (role == null) {
            return "";
        }
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getLastUpdated() {
        if (lastUpdated == null) {
            return 0L;
        }
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Nullable
    public Boolean getAudioMuted() {
        return isAudioMuted;
    }

    public void setAudioMuted(@Nullable Boolean audioMuted) {
        isAudioMuted = audioMuted;
    }

    @Nullable
    public Boolean getVideoMuted() {
        return isVideoMuted;
    }

    public void setVideoMuted(@Nullable Boolean videoMuted) {
        isVideoMuted = videoMuted;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userID);
        dest.writeString(this.participantID);
        dest.writeString(this.displayName);
        dest.writeString(this.imageURL);
        dest.writeString(this.role);
        dest.writeValue(this.lastUpdated);
        dest.writeValue(this.isAudioMuted);
        dest.writeValue(this.isVideoMuted);
    }

    protected MeetTalkParticipantInfo(Parcel in) {
        this.userID = in.readString();
        this.participantID = in.readString();
        this.displayName = in.readString();
        this.imageURL = in.readString();
        this.role = in.readString();
        this.lastUpdated = (Long) in.readValue(Long.class.getClassLoader());
        this.isAudioMuted = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.isVideoMuted = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Creator<MeetTalkParticipantInfo> CREATOR = new Creator<MeetTalkParticipantInfo>() {
        @Override
        public MeetTalkParticipantInfo createFromParcel(Parcel source) {
            return new MeetTalkParticipantInfo(source);
        }

        @Override
        public MeetTalkParticipantInfo[] newArray(int size) {
            return new MeetTalkParticipantInfo[size];
        }
    };
}
