package com.deadlock.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_providers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class UserProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    @Setter(AccessLevel.NONE)
    private String provider;

    @Column(name = "provider_id", nullable = false)
    @Setter(AccessLevel.NONE)
    private String providerId;

    public UserProvider(String provider, String providerId) {
        this.provider = provider;
        this.providerId = providerId;
    }
}
