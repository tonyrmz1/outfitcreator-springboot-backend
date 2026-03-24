package com.example.outfitcreator.feature.auth.repository;

import com.example.outfitcreator.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data repository for {@link com.example.outfitcreator.core.entity.User} persistence.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email address (case-sensitive per database collation).
     *
     * @param email the email to look up
     * @return the user if present
     */
    Optional<User> findByEmail(String email);

    /**
     * Returns whether an account already exists for the given email.
     *
     * @param email the email to check
     * @return {@code true} if a user with that email exists
     */
    boolean existsByEmail(String email);
}
