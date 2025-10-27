package com.example.sse.config;

import com.example.sse.entity.MeetingRoom;
import com.example.sse.repository.MeetingRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MeetingRoomRepository meetingRoomRepository;

    @Override
    public void run(String... args) throws Exception {
        // 샘플 회의실 데이터 생성
        MeetingRoom room1 = MeetingRoom.builder()
                .name("마라")
                .airConditionerOff(false)
                .tvOff(false)
                .lightOff(false)
                .trashCleaned(false)
                .build();

        MeetingRoom room2 = MeetingRoom.builder()
                .name("목성 회의실")
                .airConditionerOff(false)
                .tvOff(false)
                .lightOff(false)
                .trashCleaned(false)
                .build();

        MeetingRoom room3 = MeetingRoom.builder()
                .name("금성 회의실")
                .airConditionerOff(false)
                .tvOff(false)
                .lightOff(false)
                .trashCleaned(false)
                .build();

        meetingRoomRepository.save(room1);
        meetingRoomRepository.save(room2);
        meetingRoomRepository.save(room3);
    }
}
