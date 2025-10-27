package com.example.sse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meeting_room")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "air_conditioner_off", nullable = false)
    private Boolean airConditionerOff;

    @Column(name = "tv_off", nullable = false)
    private Boolean tvOff;

    @Column(name = "light_off", nullable = false)
    private Boolean lightOff;

    @Column(name = "trash_cleaned", nullable = false)
    private Boolean trashCleaned;

    public void updateAirConditioner(Boolean status) {
        this.airConditionerOff = status;
    }

    public void updateTv(Boolean status) {
        this.tvOff = status;
    }

    public void updateLight(Boolean status) {
        this.lightOff = status;
    }

    public void updateTrash(Boolean status) {
        this.trashCleaned = status;
    }

    public void updateAll(Boolean airConditioner, Boolean tv, Boolean light, Boolean trash) {
        this.airConditionerOff = airConditioner;
        this.tvOff = tv;
        this.lightOff = light;
        this.trashCleaned = trash;
    }
}
