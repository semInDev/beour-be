<<<<<<< HEAD
package com.beour.review.domain.entity;

import com.beour.global.entity.BaseTimeEntity;
import com.beour.space.domain.entity.Space;
import com.beour.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    @ManyToOne
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    private int rating; // 별점 (1~5)

    @Column(length = 1000)
    private String content;

    private LocalDate reservedDate;

    // 연관 댓글
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewImage> images = new ArrayList<>();

    private LocalDate deletedAt;

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateRating(int rating) {
        this.rating = rating;
    }

    public void delete() {
        this.deletedAt = LocalDate.now();
    }
}
=======
package com.beour.review.domain.entity;

import com.beour.global.entity.BaseTimeEntity;
import com.beour.space.domain.entity.Space;
import com.beour.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    @ManyToOne
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;


    private LocalDate reservedDate;

}
>>>>>>> develop
