package com.beour.space.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space space;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private LocalDateTime deletedAt;

    // 생성자: id 없이 만드는 용도
    public AvailableTime(Space space, LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.space = space;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
