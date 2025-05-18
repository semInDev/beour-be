package com.beour.space.host.entity;

import com.beour.space.host.enums.SpaceCategory;
import com.beour.space.host.enums.UseCategory;
import com.beour.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    private String name;

    @Enumerated(EnumType.STRING)
    private SpaceCategory spaceCategory;

    @Enumerated(EnumType.STRING)
    private UseCategory useCategory;

    private int maxCapacity;

    private String address;
    private String detailAddress;

    private int pricePerHour;

    private String thumbnailUrl;

    private Double latitude;
    private Double longitude;

    private Double avgRating;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // 연관관계 매핑
    @OneToOne(mappedBy = "space", cascade = CascadeType.ALL)
    private Description description;

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL)
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL)
    private List<AvailableTime> availableTimes = new ArrayList<>();

    @OneToMany(mappedBy = "space", cascade = CascadeType.ALL)
    private List<SpaceImage> spaceImages = new ArrayList<>();


    public void update(String name, String address, String detailAddress, int pricePerHour,
                       int maxCapacity, SpaceCategory spaceCategory, UseCategory useCategory,
                       String thumbnailUrl, double lat, double lng) {
        this.name = name;
        this.address = address;
        this.detailAddress = detailAddress;
        this.pricePerHour = pricePerHour;
        this.maxCapacity = maxCapacity;
        this.spaceCategory = spaceCategory;
        this.useCategory = useCategory;
        this.thumbnailUrl = thumbnailUrl;
        this.latitude = lat;
        this.longitude = lng;
    }

}
