package com.beour.space.host.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Description {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "space_id")
    private Space space;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String priceGuide;
    private String facilityNotice;
    private String notice;
    private String locationDescription;
    private String refundPolicy;
    private String websiteUrl;
}
