package org.igdevx.spring_boot_account_microservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "professions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Optional code to identify the profession (e.g., FARMER)
    @Column(unique = true)
    private String code;

    // Names in English and French (we store both in the same table for simplicity)
    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "name_fr")
    private String nameFr;

    @ManyToMany(mappedBy = "professions", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> users;
}
