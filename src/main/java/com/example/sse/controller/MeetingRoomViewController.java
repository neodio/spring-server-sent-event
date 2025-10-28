package com.example.sse.controller;

import com.example.sse.dto.MeetingRoomResponse;
import com.example.sse.service.MeetingRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MeetingRoomViewController {

    private final MeetingRoomService meetingRoomService;

    /**
     * 전체 회의실 목록 페이지
     */
    @GetMapping("/")
    public String index(Model model) {
        List<MeetingRoomResponse> meetingRooms = meetingRoomService.getAllMeetingRooms();
        model.addAttribute("meetingRooms", meetingRooms);
        return "index";
    }

    /**
     * 특정 회의실 상세 페이지
     */
    @GetMapping("/meeting-rooms/{id}")
    public String meetingRoomDetail(@PathVariable Long id, Model model) {
        MeetingRoomResponse meetingRoom = meetingRoomService.getMeetingRoom(id);
        model.addAttribute("meetingRoom", meetingRoom);
        return "meeting-room-detail";
    }
}
