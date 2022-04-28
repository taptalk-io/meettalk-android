package io.taptalk.meettalk.model;

import static io.taptalk.meettalk.constant.MeetTalkConstant.ConferenceInfoKey.CONFERENCE_MESSAGE_DATA;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.taptalk.TapTalk.Helper.TAPUtils;
import io.taptalk.TapTalk.Model.TAPMessageModel;

public class MeetTalkConferenceInfo implements Parcelable {

    @JsonProperty("callID") private String callID;
    @JsonProperty("roomID") private String roomID;
    @JsonProperty("hostUserID") private String hostUserID;
    @JsonProperty("callInitiatedTime") private Long callInitiatedTime;
    @JsonProperty("callStartedTime") private Long callStartedTime;
    @JsonProperty("callEndedTime") private Long callEndedTime;
    @JsonProperty("callDuration") private Long callDuration;
    @JsonProperty("lastUpdated") private Long lastUpdated;
    @JsonProperty("startWithAudioMuted") private Boolean startWithAudioMuted;
    @JsonProperty("startWithVideoMuted") private Boolean startWithVideoMuted;
    @JsonProperty("participants") private List<MeetTalkParticipantInfo> participants;

    public MeetTalkConferenceInfo() {

    }

    public MeetTalkConferenceInfo(
            String callID,
            String roomID,
            String hostUserID,
            Long callInitiatedTime,
            Long callStartedTime,
            Long callEndedTime,
            Long callDuration,
            Long lastUpdated,
            Boolean startWithAudioMuted,
            Boolean startWithVideoMuted,
            List<MeetTalkParticipantInfo> participants
    ) {
        this.callID = callID;
        this.roomID = roomID;
        this.hostUserID = hostUserID;
        this.callInitiatedTime = callInitiatedTime;
        this.callStartedTime = callStartedTime;
        this.callEndedTime = callEndedTime;
        this.callDuration = callDuration;
        this.lastUpdated = lastUpdated;
        this.startWithAudioMuted = startWithAudioMuted;
        this.startWithVideoMuted = startWithVideoMuted;
        this.participants = participants;
    }

    public static MeetTalkConferenceInfo fromHashMap(HashMap<String, Object> hashMap) {
        try {
            return TAPUtils.convertObject(hashMap, new TypeReference<MeetTalkConferenceInfo>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    public static MeetTalkConferenceInfo fromMessageModel(TAPMessageModel message) {
        try {
            if (message.getData() == null || message.getData().get(CONFERENCE_MESSAGE_DATA) == null) {
                return null;
            }
            HashMap<String, Object> conferenceInfoMap = (HashMap<String, Object>) message.getData().get(CONFERENCE_MESSAGE_DATA);
            return fromHashMap(conferenceInfoMap);
        } catch (Exception e) {
            return null;
        }
    }

    public HashMap<String, Object> toHashMap() {
        return TAPUtils.toHashMap(this);
    }

    public TAPMessageModel attachToMessage(TAPMessageModel message) {
        if (message.getData() == null) {
            message.setData(new HashMap<>());
        }
        MeetTalkConferenceInfo existingConferenceInfo = fromMessageModel(message);
        if (existingConferenceInfo != null) {
            existingConferenceInfo.updateValue(this);
            message.getData().put(CONFERENCE_MESSAGE_DATA, existingConferenceInfo.toHashMap());
        }
        else {
            message.getData().put(CONFERENCE_MESSAGE_DATA, toHashMap());
        }
        return message;
    }

    public void updateValue(MeetTalkConferenceInfo updatedConferenceInfo) {
        if (!updatedConferenceInfo.getCallID().isEmpty()) {
            setCallID(updatedConferenceInfo.getCallID());
        }
        if (!updatedConferenceInfo.getRoomID().isEmpty()) {
            setRoomID(updatedConferenceInfo.getRoomID());
        }
        if (!updatedConferenceInfo.getHostUserID().isEmpty()) {
            setRoomID(updatedConferenceInfo.getHostUserID());
        }
        if (updatedConferenceInfo.getCallInitiatedTime() > 0L) {
            setCallInitiatedTime(updatedConferenceInfo.getCallInitiatedTime());
        }
        if (updatedConferenceInfo.getCallStartedTime() > 0L) {
            setCallStartedTime(updatedConferenceInfo.getCallStartedTime());
        }
        if (updatedConferenceInfo.getCallEndedTime() > 0L) {
            setCallEndedTime(updatedConferenceInfo.getCallEndedTime());
        }
        if (updatedConferenceInfo.getCallDuration() > 0L) {
            setCallDuration(updatedConferenceInfo.getCallDuration());
        }
        if (updatedConferenceInfo.getLastUpdated() > 0L) {
            setCallDuration(updatedConferenceInfo.getLastUpdated());
        }
        if (updatedConferenceInfo.getStartWithAudioMuted() != null) {
            setStartWithAudioMuted(updatedConferenceInfo.getStartWithAudioMuted());
        }
        if (updatedConferenceInfo.getStartWithVideoMuted() != null) {
            setStartWithVideoMuted(updatedConferenceInfo.getStartWithVideoMuted());
        }
//        if (!updatedConferenceInfo.getParticipants().isEmpty()) {
//            for (String key : updatedConferenceInfo.getParticipants().keySet()) {
//                MeetTalkParticipantInfo participant = updatedConferenceInfo.getParticipants().get(key);
//                if (!getParticipants().containsKey(key) ||
//                        getParticipants().get(key).getLastUpdated() <= participant.getLastUpdated()
//                ) {
//                    getParticipants().put(key, participant);
//                }
//            }
//        }
        if (!updatedConferenceInfo.getParticipants().isEmpty()) {
            for (MeetTalkParticipantInfo updatedParticipant : updatedConferenceInfo.getParticipants()) {
                updateParticipant(updatedParticipant);
            }
        }
    }

    public MeetTalkConferenceInfo copy()  {
        return new MeetTalkConferenceInfo(
                this.callID,
                this.roomID,
                this.hostUserID,
                this.callInitiatedTime,
                this.callStartedTime,
                this.callEndedTime,
                this.callDuration,
                this.lastUpdated,
                this.startWithAudioMuted,
                this.startWithVideoMuted,
                this.participants
        );
    }

    public String getCallID() {
        if (callID == null) {
            return "";
        }
        return callID;
    }

    public void setCallID(String callID) {
        this.callID = callID;
    }

    public String getRoomID() {
        if (roomID == null) {
            return "";
        }
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getHostUserID() {
        if (hostUserID == null) {
            return "";
        }
        return hostUserID;
    }

    public void setHostUserID(String hostUserID) {
        this.hostUserID = hostUserID;
    }

    public Long getCallInitiatedTime() {
        if (callInitiatedTime == null) {
            return 0L;
        }
        return callInitiatedTime;
    }

    public void setCallInitiatedTime(Long callInitiatedTime) {
        this.callInitiatedTime = callInitiatedTime;
    }

    public Long getCallStartedTime() {
        if (callStartedTime == null) {
            return 0L;
        }
        return callStartedTime;
    }

    public void setCallStartedTime(Long callStartedTime) {
        this.callStartedTime = callStartedTime;
    }

    public Long getCallEndedTime() {
        if (callEndedTime == null) {
            return 0L;
        }
        return callEndedTime;
    }

    public void setCallEndedTime(Long callEndedTime) {
        this.callEndedTime = callEndedTime;
    }

    public Long getCallDuration() {
        if (callDuration == null) {
            return 0L;
        }
        return callDuration;
    }

    public void setCallDuration(Long callDuration) {
        this.callDuration = callDuration;
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
    public Boolean getStartWithAudioMuted() {
        return startWithAudioMuted;
    }

    public void setStartWithAudioMuted(Boolean startWithAudioMuted) {
        this.startWithAudioMuted = startWithAudioMuted;
    }

    @Nullable
    public Boolean getStartWithVideoMuted() {
        return startWithVideoMuted;
    }

    public void setStartWithVideoMuted(Boolean startWithVideoMuted) {
        this.startWithVideoMuted = startWithVideoMuted;
    }

    public List<MeetTalkParticipantInfo> getParticipants() {
        if (participants == null) {
            return new ArrayList<>();
        }
        return participants;
    }

    public void setParticipants(List<MeetTalkParticipantInfo> participants) {
        this.participants = participants;
    }

    public void updateParticipant(MeetTalkParticipantInfo updatedParticipant) {
        boolean isExistingParticipant = false;
        for (MeetTalkParticipantInfo participant : getParticipants()) {
            if (participant.getUserID().equals(updatedParticipant.getUserID())) {
                isExistingParticipant = true;
                if (participant.getLastUpdated() <= updatedParticipant.getLastUpdated()) {
                    getParticipants().set(getParticipants().indexOf(participant), updatedParticipant);
                }
                break;
            }
        }
        if (!isExistingParticipant) {
            getParticipants().add(updatedParticipant);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.callID);
        dest.writeString(this.roomID);
        dest.writeString(this.hostUserID);
        dest.writeValue(this.callInitiatedTime);
        dest.writeValue(this.callStartedTime);
        dest.writeValue(this.callEndedTime);
        dest.writeValue(this.callDuration);
        dest.writeValue(this.lastUpdated);
        dest.writeValue(this.startWithAudioMuted);
        dest.writeValue(this.startWithVideoMuted);
        dest.writeTypedList(this.participants);
    }

    public void readFromParcel(Parcel source) {
        this.callID = source.readString();
        this.roomID = source.readString();
        this.hostUserID = source.readString();
        this.callInitiatedTime = (Long) source.readValue(Long.class.getClassLoader());
        this.callStartedTime = (Long) source.readValue(Long.class.getClassLoader());
        this.callEndedTime = (Long) source.readValue(Long.class.getClassLoader());
        this.callDuration = (Long) source.readValue(Long.class.getClassLoader());
        this.lastUpdated = (Long) source.readValue(Long.class.getClassLoader());
        this.startWithAudioMuted = (Boolean) source.readValue(Boolean.class.getClassLoader());
        this.startWithVideoMuted = (Boolean) source.readValue(Boolean.class.getClassLoader());
        this.participants = source.createTypedArrayList(MeetTalkParticipantInfo.CREATOR);
    }

    protected MeetTalkConferenceInfo(Parcel in) {
        this.callID = in.readString();
        this.roomID = in.readString();
        this.hostUserID = in.readString();
        this.callInitiatedTime = (Long) in.readValue(Long.class.getClassLoader());
        this.callStartedTime = (Long) in.readValue(Long.class.getClassLoader());
        this.callEndedTime = (Long) in.readValue(Long.class.getClassLoader());
        this.callDuration = (Long) in.readValue(Long.class.getClassLoader());
        this.lastUpdated = (Long) in.readValue(Long.class.getClassLoader());
        this.startWithAudioMuted = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.startWithVideoMuted = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.participants = in.createTypedArrayList(MeetTalkParticipantInfo.CREATOR);
    }

    public static final Creator<MeetTalkConferenceInfo> CREATOR = new Creator<MeetTalkConferenceInfo>() {
        @Override
        public MeetTalkConferenceInfo createFromParcel(Parcel source) {
            return new MeetTalkConferenceInfo(source);
        }

        @Override
        public MeetTalkConferenceInfo[] newArray(int size) {
            return new MeetTalkConferenceInfo[size];
        }
    };
}
