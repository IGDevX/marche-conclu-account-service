package org.igdevx.spring_boot_account_microservice.repository;

import org.igdevx.spring_boot_account_microservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.professions WHERE u.keycloakId = :keycloakId")
    Optional<User> findByKeycloakId(@Param("keycloakId") UUID keycloakId);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.professions WHERE u.id = :id")
    @NonNull
    Optional<User> findById(@NonNull @Param("id") Long id);

    @Query("""
       SELECT u FROM User u
       LEFT JOIN FETCH u.professions
       WHERE u.keycloakId = :keycloakId
       """)
    Optional<User> findByKeycloakIdWithProfessions(UUID keycloakId);

}