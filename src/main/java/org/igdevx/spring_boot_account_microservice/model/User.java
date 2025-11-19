package org.igdevx.spring_boot_account_microservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "professions")
@ToString(exclude = "professions")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_id", nullable = false, unique = true, columnDefinition = "UUID")
    private UUID keycloakId;

    private String biography;
    private String website;
    private String facebook;
    private String instagram;
    private String linkedin;

    // Producer-specific fields
    private String siret;
    private String organizationType;
    private Integer installationYear;
    private Integer employeesCount;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_professions",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "profession_id")
    )
    private Set<Profession> professions;

    // Restaurant-specific fields
    private String serviceType;
    private String cuisineType;
    private String hygieneCertifications;
    private String awards;

    // Stripe Connect integration
    @Column(name = "stripe_account_id")
    private String stripeAccountId;

    @Column(name = "stripe_account_status")
    private String stripeAccountStatus; // pending, active, rejected, etc.

    @Column(name = "stripe_onboarding_complete")
    private Boolean stripeOnboardingComplete;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.professions == null) {
            this.professions = new HashSet<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}