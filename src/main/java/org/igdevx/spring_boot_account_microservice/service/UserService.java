package org.igdevx.spring_boot_account_microservice.service;

import org.igdevx.spring_boot_account_microservice.dto.*;
import org.igdevx.spring_boot_account_microservice.exception.BadRequestException;
import org.igdevx.spring_boot_account_microservice.exception.ResourceNotFoundException;
import org.igdevx.spring_boot_account_microservice.model.Profession;
import org.igdevx.spring_boot_account_microservice.model.User;
import org.igdevx.spring_boot_account_microservice.repository.ProfessionRepository;
import org.igdevx.spring_boot_account_microservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ProfessionRepository professionRepository;

    public UserService(UserRepository userRepository, ProfessionRepository professionRepository) {
        this.userRepository = userRepository;
        this.professionRepository = professionRepository;
    }

    public Optional<User> findByKeycloakId(UUID keycloakId) {
        return userRepository.findByKeycloakId(keycloakId);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    // Get keycloak ID by user ID
    public UUID getKeycloakIdByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return user.getKeycloakId();
    }

    // Get or create user profile (thread-safe for race conditions)
    // Used by internal service-to-service communication
    @Transactional
    public UserProfileResponse getOrCreateUserProfile(UUID keycloakId) {
        // Try to find existing user first
        Optional<User> existingUser = userRepository.findByKeycloakIdWithProfessions(keycloakId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            return mapToUserProfileResponse(user);
        }
        
        // User doesn't exist - create with optimistic locking to handle race conditions
        try {
            User newUser = User.builder()
                    .keycloakId(keycloakId)
                    .build();
            // Initialize professions collection for new users
            if (newUser.getProfessions() == null) {
                newUser.setProfessions(new HashSet<>());
            }
            User savedUser = userRepository.save(newUser);
            return mapToUserProfileResponse(savedUser);
        } catch (Exception e) {
            // Handle race condition: another thread created the user between our check and insert
            // This catches DataIntegrityViolationException from unique constraint
            User user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found after creation race condition"));
            return mapToUserProfileResponse(user);
        }
    }

    // Get user profile by keycloak ID
    public UserProfileResponse getUserProfile(UUID keycloakId) {
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloak ID: " + keycloakId));
        
        return mapToUserProfileResponse(user);
    }

    // Update personal information
    public UserProfileResponse updatePersonalInfo(UUID keycloakId, UpdatePersonalInfoRequest request) {
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId).orElse(null);
        
        // If user doesn't exist, create it first to avoid race conditions
        if (user == null) {
            try {
                User newUser = User.builder()
                        .keycloakId(keycloakId)
                        .professions(new HashSet<>())
                        .build();
                user = userRepository.save(newUser);
            } catch (Exception e) {
                // Handle race condition: another thread created the user
                user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found after creation race condition"));
            }
        }
        
        updateCommonFields(user, request.getBiography(), request.getWebsite(), 
                          request.getFacebook(), request.getInstagram(), request.getLinkedin());
        
        User savedUser = userRepository.save(user);
        return mapToUserProfileResponse(savedUser);
    }

    // Get restaurant profile by ID
    public RestaurantPublicProfileResponse getRestaurantProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));
        
        if (user.getServiceType() == null && user.getCuisineType() == null) {
            throw new BadRequestException("User with ID " + id + " is not a restaurant");
        }
        
        return mapToRestaurantPublicProfile(user);
    }

    // Create or update restaurant profile
    public UserProfileResponse createOrUpdateRestaurantProfile(UUID keycloakId, RestaurantProfileRequest request) {
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId).orElse(null);
        
        // If user doesn't exist, create it first to avoid race conditions
        if (user == null) {
            try {
                User newUser = User.builder()
                        .keycloakId(keycloakId)
                        .professions(new HashSet<>())
                        .build();
                user = userRepository.save(newUser);
            } catch (Exception e) {
                // Handle race condition: another thread created the user
                user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found after creation race condition"));
            }
        }
        
        updateCommonFields(user, request.getBiography(), request.getWebsite(), 
                          request.getFacebook(), request.getInstagram(), request.getLinkedin());
        
        // Update restaurant-specific fields
        user.setServiceType(request.getServiceType());
        user.setCuisineType(request.getCuisineType());
        user.setHygieneCertifications(request.getHygieneCertifications());
        user.setAwards(request.getAwards());
        
        // Clear producer fields if any
        clearProducerFields(user);
        
        User savedUser = userRepository.save(user);
        return mapToUserProfileResponse(savedUser);
    }

    // Update restaurant profile by ID
    public UserProfileResponse updateRestaurantProfile(Long id, RestaurantProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));
        
        updateCommonFields(user, request.getBiography(), request.getWebsite(), 
                          request.getFacebook(), request.getInstagram(), request.getLinkedin());
        
        // Update restaurant-specific fields
        user.setServiceType(request.getServiceType());
        user.setCuisineType(request.getCuisineType());
        user.setHygieneCertifications(request.getHygieneCertifications());
        user.setAwards(request.getAwards());
        
        User savedUser = userRepository.save(user);
        return mapToUserProfileResponse(savedUser);
    }

    // Delete restaurant profile
    public void deleteRestaurantProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));
        
        userRepository.delete(user);
    }

    // Delete restaurant profile by keycloak ID
    public void deleteRestaurantProfileByKeycloakId(UUID keycloakId) {
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with keycloak ID: " + keycloakId));
        
        userRepository.delete(user);
    }

    // Get producer profile by ID
    public ProducerPublicProfileResponse getProducerProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found with ID: " + id));
        
        if (user.getSiret() == null) {
            throw new BadRequestException("User with ID " + id + " is not a producer");
        }
        
        return mapToProducerPublicProfile(user);
    }

    // Create or update producer profile
    public UserProfileResponse createOrUpdateProducerProfile(UUID keycloakId, ProducerProfileRequest request) {
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId).orElse(null);
        
        // If user doesn't exist, create it first to avoid race conditions
        if (user == null) {
            try {
                User newUser = User.builder()
                        .keycloakId(keycloakId)
                        .professions(new HashSet<>())
                        .build();
                user = userRepository.save(newUser);
            } catch (Exception e) {
                // Handle race condition: another thread created the user
                user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found after creation race condition"));
            }
        }
        
        updateCommonFields(user, request.getBiography(), request.getWebsite(), 
                          request.getFacebook(), request.getInstagram(), request.getLinkedin());
        
        // Update producer-specific fields
        user.setSiret(request.getSiret());
        user.setOrganizationType(request.getOrganizationType());
        user.setInstallationYear(request.getInstallationYear());
        user.setEmployeesCount(request.getEmployeesCount());

        // Handle professions (many-to-many)
        updateProfessions(user, request.getProfessionIds());
        
        // Clear restaurant fields if any
        clearRestaurantFields(user);
        
        User savedUser = userRepository.save(user);
        return mapToUserProfileResponse(savedUser);
    }

    // Update producer profile by ID
    public UserProfileResponse updateProducerProfile(Long id, ProducerProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found with ID: " + id));
        
        updateCommonFields(user, request.getBiography(), request.getWebsite(), 
                          request.getFacebook(), request.getInstagram(), request.getLinkedin());
        
        // Update producer-specific fields
        user.setSiret(request.getSiret());
        user.setOrganizationType(request.getOrganizationType());
        user.setInstallationYear(request.getInstallationYear());
        user.setEmployeesCount(request.getEmployeesCount());

        // Handle professions (many-to-many)
        updateProfessions(user, request.getProfessionIds());
        
        User savedUser = userRepository.save(user);
        return mapToUserProfileResponse(savedUser);
    }

    // Delete producer profile
    public void deleteProducerProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found with ID: " + id));
        
        userRepository.delete(user);
    }

    // Delete producer profile by keycloak ID
    public void deleteProducerProfileByKeycloakId(UUID keycloakId) {
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("Producer not found with keycloak ID: " + keycloakId));
        
        userRepository.delete(user);
    }

    // Add profession to producer
    @Transactional
    public UserProfileResponse addProfessionToProducer(UUID keycloakId, Long professionId) {
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloak ID: " + keycloakId));
        
        if (user.getSiret() == null) {
            throw new BadRequestException("User is not a producer. Create a producer profile first.");
        }
        
        Profession profession = professionRepository.findById(professionId)
                .orElseThrow(() -> new BadRequestException("Profession not found with ID: " + professionId));
        
        user.getProfessions().add(profession);
        User savedUser = userRepository.save(user);
        return mapToUserProfileResponse(savedUser);
    }

    // Remove profession from producer
    @Transactional
    public UserProfileResponse removeProfessionFromProducer(UUID keycloakId, Long professionId) {
        User user = userRepository.findByKeycloakIdWithProfessions(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloak ID: " + keycloakId));
        
        if (user.getSiret() == null) {
            throw new BadRequestException("User is not a producer");
        }
        
        user.getProfessions().removeIf(p -> p.getId().equals(professionId));
        User savedUser = userRepository.save(user);
        return mapToUserProfileResponse(savedUser);
    }

    // Helper methods for mapping
    
    // Map Profession entities to DTOs
    private List<ProfessionDto> mapProfessionsToDtos(Set<Profession> professions) {
        if (professions == null || professions.isEmpty()) {
            return List.of();
        }
        
        // Create a defensive copy to avoid ConcurrentModificationException
        // when Hibernate is initializing the collection
        return new java.util.ArrayList<>(professions).stream()
                .map(p -> ProfessionDto.builder()
                        .id(p.getId())
                        .code(p.getCode())
                        .nameEn(p.getNameEn())
                        .nameFr(p.getNameFr())
                        .build()
                )
                .collect(Collectors.toList());
    }
    
    // Update common fields shared across all profile types
    private void updateCommonFields(User user, String biography, String website, 
                                    String facebook, String instagram, String linkedin) {
        user.setBiography(biography);
        user.setWebsite(website);
        user.setFacebook(facebook);
        user.setInstagram(instagram);
        user.setLinkedin(linkedin);
    }
    
    // Update user's professions from profession IDs
    private void updateProfessions(User user, List<Long> professionIds) {
        // ONLY update professions if professionIds is explicitly provided (not null)
        // If null, it means the client didn't send this field, so we should preserve existing professions
        if (professionIds == null) {
            return; // Don't touch professions if not provided
        }
        
        Set<Profession> professions = new HashSet<>();
        if (!professionIds.isEmpty()) {
            // Validate all profession IDs exist
            List<Long> invalidIds = professionIds.stream()
                .filter(id -> professionRepository.findById(id).isEmpty())
                .toList();
            
            if (!invalidIds.isEmpty()) {
                throw new BadRequestException(
                    "Invalid profession IDs: " + invalidIds + ". Please provide valid profession IDs."
                );
            }
            
            professions = professionIds.stream()
                .map(professionRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        }
        // If professionIds is empty list [], clear professions (intentional)
        user.setProfessions(professions);
    }
    
    // Clear producer-specific fields (when converting to restaurant)
    private void clearProducerFields(User user) {
        user.setSiret(null);
        user.setOrganizationType(null);
        user.setInstallationYear(null);
        user.setEmployeesCount(null);
        user.setProfessions(new HashSet<>());
    }
    
    // Clear restaurant-specific fields (when converting to producer)
    private void clearRestaurantFields(User user) {
        user.setServiceType(null);
        user.setCuisineType(null);
        user.setHygieneCertifications(null);
        user.setAwards(null);
    }
    
    private UserProfileResponse mapToUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId() != null ? user.getKeycloakId().toString() : null)
                .biography(user.getBiography())
                .website(user.getWebsite())
                .facebook(user.getFacebook())
                .instagram(user.getInstagram())
                .linkedin(user.getLinkedin())
                .siret(user.getSiret())
                .organizationType(user.getOrganizationType())
                .installationYear(user.getInstallationYear())
                .employeesCount(user.getEmployeesCount())
                .professions(mapProfessionsToDtos(user.getProfessions()))
                .serviceType(user.getServiceType())
                .cuisineType(user.getCuisineType())
                .hygieneCertifications(user.getHygieneCertifications())
                .awards(user.getAwards())
                .stripeAccountId(user.getStripeAccountId())
                .stripeAccountStatus(user.getStripeAccountStatus())
                .stripeOnboardingComplete(user.getStripeOnboardingComplete())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private RestaurantPublicProfileResponse mapToRestaurantPublicProfile(User user) {
        return RestaurantPublicProfileResponse.builder()
                .id(user.getId())
                .biography(user.getBiography())
                .website(user.getWebsite())
                .facebook(user.getFacebook())
                .instagram(user.getInstagram())
                .linkedin(user.getLinkedin())
                .serviceType(user.getServiceType())
                .cuisineType(user.getCuisineType())
                .hygieneCertifications(user.getHygieneCertifications())
                .awards(user.getAwards())
                .build();
    }

    private ProducerPublicProfileResponse mapToProducerPublicProfile(User user) {
        return ProducerPublicProfileResponse.builder()
                .id(user.getId())
                .biography(user.getBiography())
                .website(user.getWebsite())
                .facebook(user.getFacebook())
                .instagram(user.getInstagram())
                .linkedin(user.getLinkedin())
                .siret(user.getSiret())
                .organizationType(user.getOrganizationType())
                .installationYear(user.getInstallationYear())
                .employeesCount(user.getEmployeesCount())
                .professions(mapProfessionsToDtos(user.getProfessions()))
        .build();
    }
}