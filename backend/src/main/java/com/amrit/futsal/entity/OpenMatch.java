package com.amrit.futsal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "open_matches")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OpenMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "booking_id", referencedColumnName = "id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "host_user_id", referencedColumnName = "id", nullable = false)
    private User host;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", nullable = false)
    private SkillLevel skillLevel;

    @Column(name = "desired_player_count", nullable = false)
    private Integer desiredPlayerCount;

    @Column(name = "notes", length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OpenMatchStatus status;

    @ManyToMany
    @JoinTable(
            name = "open_match_participants",
            joinColumns = @JoinColumn(name = "open_match_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum SkillLevel {
        ANY,
        CASUAL,
        INTERMEDIATE,
        COMPETITIVE
    }

    public enum OpenMatchStatus {
        OPEN,
        FULL,
        CANCELLED
    }
}
