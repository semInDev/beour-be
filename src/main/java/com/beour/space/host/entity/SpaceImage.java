package com.beour.space.host.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space space;

    private String imageUrl;

    private LocalDateTime deletedAt;

    // 생성자: id 없이 만드는 용도
    public SpaceImage(Space space, String imageUrl) {
        this.space = space;
        this.imageUrl = imageUrl;
    }
}
