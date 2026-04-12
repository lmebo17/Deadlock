package com.deadlock.repository;

import com.deadlock.model.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserProviderRepository extends JpaRepository<UserProvider, Long> {
    Optional<UserProvider> findByProviderAndProviderId(String provider, String providerId);
}
