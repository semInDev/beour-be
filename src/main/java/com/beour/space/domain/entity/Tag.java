package com.beour.space.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "space_id")
    private Space space;

    private String contents;

    // 생성자: id 없이 만드는 용도
    public Tag(Space space, String contents) {
        this.space = space;
        this.contents = contents;
    }
}

