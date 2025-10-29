package com.example.sse.service;

import com.example.sse.dto.MeetingRoomCreateRequest;
import com.example.sse.dto.MeetingRoomResponse;
import com.example.sse.dto.MeetingRoomUpdateRequest;
import com.example.sse.entity.MeetingRoom;
import com.example.sse.repository.MeetingRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingRoomService {

    private final MeetingRoomRepository meetingRoomRepository;
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private static final Long TIMEOUT = 60000L; // 1분

    @Transactional
    public MeetingRoomResponse createMeetingRoom(MeetingRoomCreateRequest request) {
        log.info("회의실 생성 요청: {}", request.getName());
        
        MeetingRoom meetingRoom = MeetingRoom.builder()
                .name(request.getName())
                .airConditionerOff(false)
                .tvOff(false)
                .lightOff(false)
                .trashCleaned(false)
                .build();

        MeetingRoom saved = meetingRoomRepository.save(meetingRoom);
        log.info("회의실 생성 완료: ID={}, Name={}", saved.getId(), saved.getName());
        
        return MeetingRoomResponse.from(saved);
    }

    public List<MeetingRoomResponse> getAllMeetingRooms() {
        log.info("전체 회의실 목록 조회");
        return meetingRoomRepository.findAll().stream()
                .map(MeetingRoomResponse::from)
                .collect(Collectors.toList());
    }

    public MeetingRoomResponse getMeetingRoom(Long id) {
        log.info("회의실 조회: ID={}", id);
        MeetingRoom meetingRoom = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회의실을 찾을 수 없습니다. ID: " + id));
        return MeetingRoomResponse.from(meetingRoom);
    }

    @Transactional
    public MeetingRoomResponse updateMeetingRoom(Long id, MeetingRoomUpdateRequest request) {
        log.info("회의실 업데이트 요청: ID={}, Request={}", id, request);
        
        MeetingRoom meetingRoom = meetingRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회의실을 찾을 수 없습니다. ID: " + id));

        // 각 필드별로 업데이트
        if (request.getAirConditionerOff() != null) {
            log.debug("에어컨 상태 변경: {} -> {}", meetingRoom.getAirConditionerOff(), request.getAirConditionerOff());
            meetingRoom.updateAirConditioner(request.getAirConditionerOff());
        }
        if (request.getTvOff() != null) {
            log.debug("TV 상태 변경: {} -> {}", meetingRoom.getTvOff(), request.getTvOff());
            meetingRoom.updateTv(request.getTvOff());
        }
        if (request.getLightOff() != null) {
            log.debug("불 상태 변경: {} -> {}", meetingRoom.getLightOff(), request.getLightOff());
            meetingRoom.updateLight(request.getLightOff());
        }
        if (request.getTrashCleaned() != null) {
            log.debug("쓰레기 상태 변경: {} -> {}", meetingRoom.getTrashCleaned(), request.getTrashCleaned());
            meetingRoom.updateTrash(request.getTrashCleaned());
        }

        MeetingRoomResponse response = MeetingRoomResponse.from(meetingRoom);
        log.info("회의실 업데이트 완료: {}", response);
        
        // SSE로 변경사항 전송
        sendToClients(id, response);
        
        return response;
    }

    public SseEmitter subscribe(Long meetingRoomId) {
        log.info("SSE 구독 요청: 회의실 ID={}", meetingRoomId);
        
        // 회의실 존재 확인
        meetingRoomRepository.findById(meetingRoomId)
                .orElseThrow(() -> new IllegalArgumentException("회의실을 찾을 수 없습니다. ID: " + meetingRoomId));

        SseEmitter emitter = new SseEmitter(TIMEOUT);
        
        // emitter 리스트 초기화
        emitters.putIfAbsent(meetingRoomId, new CopyOnWriteArrayList<>());
        emitters.get(meetingRoomId).add(emitter);
        
        log.info("SSE Emitter 추가: 회의실 ID={}, 현재 구독자 수={}", 
                meetingRoomId, emitters.get(meetingRoomId).size());

        // 초기 연결 이벤트 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("Connected to meeting room " + meetingRoomId));
            log.debug("SSE 초기 연결 이벤트 전송 완료");
        } catch (IOException e) {
            log.error("SSE 초기 연결 이벤트 전송 실패", e);
            emitters.get(meetingRoomId).remove(emitter);
            emitter.completeWithError(e);
        }

        // 타임아웃 시 emitter 제거
        emitter.onTimeout(() -> {
            log.info("SSE 타임아웃: 회의실 ID={}", meetingRoomId);
            emitters.get(meetingRoomId).remove(emitter);
            emitter.complete();
        });

        // 완료 시 emitter 제거
        emitter.onCompletion(() -> {
            log.info("SSE 연결 종료: 회의실 ID={}", meetingRoomId);
            emitters.get(meetingRoomId).remove(emitter);
        });

        // 에러 발생 시 emitter 제거
        emitter.onError((e) -> {
            log.error("SSE 에러 발생: 회의실 ID={}", meetingRoomId, e);
            emitters.get(meetingRoomId).remove(emitter);
            emitter.completeWithError(e);
        });

        return emitter;
    }

    private void sendToClients(Long meetingRoomId, MeetingRoomResponse data) {
        List<SseEmitter> roomEmitters = emitters.get(meetingRoomId);
        if (roomEmitters == null || roomEmitters.isEmpty()) {
            log.debug("SSE 구독자 없음: 회의실 ID={}", meetingRoomId);
            return;
        }

        log.info("SSE 이벤트 전송 시작: 회의실 ID={}, 구독자 수={}", meetingRoomId, roomEmitters.size());

        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        roomEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("update")
                        .data(data));
                log.debug("SSE 이벤트 전송 성공");
            } catch (IOException e) {
                log.error("SSE 이벤트 전송 실패", e);
                deadEmitters.add(emitter);
                emitter.completeWithError(e);
            }
        });

        // 죽은 emitter 제거
        if (!deadEmitters.isEmpty()) {
            roomEmitters.removeAll(deadEmitters);
            log.info("죽은 Emitter 제거: {} 개", deadEmitters.size());
        }
    }
}
