package com.example.sse.controller;

import com.example.sse.dto.MeetingRoomCreateRequest;
import com.example.sse.dto.MeetingRoomResponse;
import com.example.sse.dto.MeetingRoomUpdateRequest;
import com.example.sse.service.MeetingRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/meeting-rooms")
@RequiredArgsConstructor
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;

    /**
     * 회의실 생성
     */
    @PostMapping
    public ResponseEntity<MeetingRoomResponse> createMeetingRoom(
            @RequestBody MeetingRoomCreateRequest request) {
        MeetingRoomResponse response = meetingRoomService.createMeetingRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 전체 회의실 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<MeetingRoomResponse>> getAllMeetingRooms() {
        List<MeetingRoomResponse> responses = meetingRoomService.getAllMeetingRooms();
        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 회의실 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<MeetingRoomResponse> getMeetingRoom(@PathVariable Long id) {
        MeetingRoomResponse response = meetingRoomService.getMeetingRoom(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 회의실 상태 업데이트
     */
    @PatchMapping("/{id}")
    public ResponseEntity<MeetingRoomResponse> updateMeetingRoom(
            @PathVariable Long id,
            @RequestBody MeetingRoomUpdateRequest request) {
        MeetingRoomResponse response = meetingRoomService.updateMeetingRoom(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * SSE 구독 (실시간 업데이트 수신)
     */
    @GetMapping(value = "/{id}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long id) {
        return meetingRoomService.subscribe(id);
    }
}
