package com.example.sse.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingRoomUpdateRequest {

    private Boolean airConditionerOff;
    private Boolean tvOff;
    private Boolean lightOff;
    private Boolean trashCleaned;
}
