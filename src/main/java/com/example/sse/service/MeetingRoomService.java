package com.example.sse.service;

import com.example.sse.dto.MeetingRoomCreateRequest;
import com.example.sse.dto.MeetingRoomResponse;
import com.example.sse.dto.MeetingRoomUpdateRequest;
import com.example.sse.entity.MeetingRoom;
import com.example.sse.repository.MeetingRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingRoomService {

    private final MeetingRoomRepository meetingRoomRepository;
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private static final Long TIMEOUT = 60 * 60 * 1000L; // 1시간

    @Transactional
    public MeetingRoomResponse createMeetingRoom(MeetingRoomCreateRequest request) {
        MeetingRoom meetingRoom = MeetingRoom.builder()
                .name(request.getName())
                .airConditionerOff(false)
                .tvOff(false)
                .lightOff(false)
                .trashCleaned(false)
                .build();

        MeetingRoom saved = meetingRoomRepository.save(meetingRoom);
        return MeetingRoomResponse.from(saved);
    }

    public List<MeetingRoomResponse> getAllMeetingRooms() {
        return meetingRoomRepository.findAll().stream()
                .map(MeetingRoomResponse::from)
                .collect(Collectors.toList());
    }

    public MeetingRoomResponse getMeetingRoom(Long id) {
        MeetingRoom meetingRoom = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회의실을 찾을 수 없습니다."));
        return MeetingRoomResponse.from(meetingRoom);
    }

    @Transactional
    public MeetingRoomResponse updateMeetingRoom(Long id, MeetingRoomUpdateRequest request) {
        MeetingRoom meetingRoom = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회의실을 찾을 수 없습니다."));

        if (request.getAirConditionerOff() != null) {
            meetingRoom.updateAirConditioner(request.getAirConditionerOff());
        }
        if (request.getTvOff() != null) {
            meetingRoom.updateTv(request.getTvOff());
        }
        if (request.getLightOff() != null) {
            meetingRoom.updateLight(request.getLightOff());
        }
        if (request.getTrashCleaned() != null) {
            meetingRoom.updateTrash(request.getTrashCleaned());
        }

        MeetingRoomResponse response = MeetingRoomResponse.from(meetingRoom);
        
        // SSE로 변경사항 전송
        sendToClients(id, response);
        
        return response;
    }

    public SseEmitter subscribe(Long meetingRoomId) {
        // 회의실 존재 확인
        meetingRoomRepository.findById(meetingRoomId)
                .orElseThrow(() -> new IllegalArgumentException("회의실을 찾을 수 없습니다."));

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        
        // emitter 리스트 초기화
        emitters.putIfAbsent(meetingRoomId, new CopyOnWriteArrayList<>());
        emitters.get(meetingRoomId).add(emitter);

        // 초기 연결 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected to meeting room " + meetingRoomId));
        } catch (IOException e) {
            emitters.get(meetingRoomId).remove(emitter);
            emitter.completeWithError(e);
        }

        // 타임아웃 시 emitter 제거
        emitter.onTimeout(() -> {
            emitters.get(meetingRoomId).remove(emitter);
            emitter.complete();
        });

        // 완료 시 emitter 제거
        emitter.onCompletion(() -> emitters.get(meetingRoomId).remove(emitter));

        // 에러 발생 시 emitter 제거
        emitter.onError((e) -> {
            emitters.get(meetingRoomId).remove(emitter);
            emitter.completeWithError(e);
        });

        return emitter;
    }

    private void sendToClients(Long meetingRoomId, MeetingRoomResponse data) {
        List<SseEmitter> roomEmitters = emitters.get(meetingRoomId);
        if (roomEmitters == null || roomEmitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        roomEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("update")
                        .data(data));
            } catch (IOException e) {
                deadEmitters.add(emitter);
                emitter.completeWithError(e);
            }
        });

        // 죽은 emitter 제거
        roomEmitters.removeAll(deadEmitters);
    }
}
