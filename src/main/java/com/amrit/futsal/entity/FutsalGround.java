package com.amrit.futsal.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "futsal_grounds")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FutsalGround {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ground_id")
    private Long groundId;

    @ManyToOne
    @JoinColumn(name = "futsal_id", nullable = false)
    private FutsalCompany futsalCompany;

    @Column(name = "ground_name", nullable = false)
    private String groundName;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
