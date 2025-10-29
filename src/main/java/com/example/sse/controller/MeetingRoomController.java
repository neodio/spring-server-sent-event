package com.example.sse.controller;

import com.example.sse.dto.MeetingRoomCreateRequest;
import com.example.sse.dto.MeetingRoomResponse;
import com.example.sse.dto.MeetingRoomUpdateRequest;
import com.example.sse.service.MeetingRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
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
        log.info("[POST /api/meeting-rooms] 회의실 생성 요청: {}", request.getName());
        MeetingRoomResponse response = meetingRoomService.createMeetingRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 전체 회의실 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<MeetingRoomResponse>> getAllMeetingRooms() {
        log.info("[GET /api/meeting-rooms] 전체 회의실 목록 조회");
        List<MeetingRoomResponse> responses = meetingRoomService.getAllMeetingRooms();
        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 회의실 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<MeetingRoomResponse> getMeetingRoom(@PathVariable Long id) {
        log.info("[GET /api/meeting-rooms/{}] 회의실 조회", id);
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
        log.info("[PATCH /api/meeting-rooms/{}] 회의실 업데이트 요청: airConditioner={}, tv={}, light={}, trash={}", 
                id, request.getAirConditionerOff(), request.getTvOff(), 
                request.getLightOff(), request.getTrashCleaned());
        
        MeetingRoomResponse response = meetingRoomService.updateMeetingRoom(id, request);
        log.info("[PATCH /api/meeting-rooms/{}] 회의실 업데이트 완료", id);
        
        return ResponseEntity.ok(response);
    }

    /**
     * SSE 구독 (실시간 업데이트 수신)
     */
    @GetMapping(value = "/{id}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long id) {
        log.info("[GET /api/meeting-rooms/{}/subscribe] SSE 구독 요청", id);
        return meetingRoomService.subscribe(id);
    }
}
