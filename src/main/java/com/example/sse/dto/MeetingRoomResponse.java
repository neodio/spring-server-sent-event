package com.example.sse.dto;

import com.example.sse.entity.MeetingRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingRoomResponse {

    private Long id;
    private String name;
    private Boolean airConditionerOff;
    private Boolean tvOff;
    private Boolean lightOff;
    private Boolean trashCleaned;

    public static MeetingRoomResponse from(MeetingRoom meetingRoom) {
        return MeetingRoomResponse.builder()
                .id(meetingRoom.getId())
                .name(meetingRoom.getName())
                .airConditionerOff(meetingRoom.getAirConditionerOff())
                .tvOff(meetingRoom.getTvOff())
                .lightOff(meetingRoom.getLightOff())
                .trashCleaned(meetingRoom.getTrashCleaned())
                .build();
    }
}
