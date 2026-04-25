package com.deadlock.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id", nullable = false)
    private User player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id", nullable = false)
    private User player2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchStatus status = MatchStatus.WAITING;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "player1_elo_before", nullable = false)
    private int player1EloBefore;

    @Column(name = "player2_elo_before", nullable = false)
    private int player2EloBefore;

    @Column(name = "player1_elo_change")
    private Integer player1EloChange;

    @Column(name = "player2_elo_change")
    private Integer player2EloChange;

    @Column(name = "started_at", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Instant startedAt = Instant.now();

    @Column(name = "ended_at")
    private Instant endedAt;

    public boolean involves(Long userId) {
        return player1.getId().equals(userId) || player2.getId().equals(userId);
    }

    public User opponentOf(Long userId) {
        if (player1.getId().equals(userId)) return player2;
        if (player2.getId().equals(userId)) return player1;
        throw new IllegalArgumentException("User " + userId + " is not in this match");
    }
}
