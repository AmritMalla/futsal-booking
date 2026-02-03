package com.amrit.futsal.entity;

import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "futsal_grounds")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FutsalGround {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "id", nullable = false)
    private FutsalCompany company;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "surface_type")
    private String surfaceType;

    @Column(name = "price_per_hour", nullable = false)
    private BigDecimal pricePerHour;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
