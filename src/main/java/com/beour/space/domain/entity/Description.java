package com.beour.space.domain.entity;

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

    // 전체 수정(PUT)
    public void update(String description, String priceGuide, String facilityNotice,
                       String notice, String locationDescription, String refundPolicy) {
        this.description = description;
        this.priceGuide = priceGuide;
        this.facilityNotice = facilityNotice;
        this.notice = notice;
        this.locationDescription = locationDescription;
        this.refundPolicy = refundPolicy;
    }

    // 부분 수정(PATCH)
    public void updateDescription(String description) {
        this.description = description;
    }

    public void updatePriceGuide(String priceGuide) {
        this.priceGuide = priceGuide;
    }

    public void updateFacilityNotice(String facilityNotice) {
        this.facilityNotice = facilityNotice;
    }

    public void updateNotice(String notice) {
        this.notice = notice;
    }

    public void updateLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public void updateRefundPolicy(String refundPolicy) {
        this.refundPolicy = refundPolicy;
    }
}
