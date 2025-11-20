package org.igdevx.spring_boot_account_microservice.repository;

import org.igdevx.spring_boot_account_microservice.model.Profession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessionRepository extends JpaRepository<Profession, Long> {
    // Optionally, lookup by code
    Profession findByCode(String code);
}
