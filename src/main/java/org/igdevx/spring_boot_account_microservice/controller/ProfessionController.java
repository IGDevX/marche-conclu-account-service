package org.igdevx.spring_boot_account_microservice.controller;

import org.igdevx.spring_boot_account_microservice.dto.ProfessionDto;
import org.igdevx.spring_boot_account_microservice.model.Profession;
import org.igdevx.spring_boot_account_microservice.repository.ProfessionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/account/professions")
public class ProfessionController {

    private final ProfessionRepository professionRepository;

    public ProfessionController(ProfessionRepository professionRepository) {
        this.professionRepository = professionRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProfessionDto>> getAll() {
        List<ProfessionDto> list = professionRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessionDto> getById(@PathVariable Long id) {
        return professionRepository.findById(id)
                .map(p -> ResponseEntity.ok(toDto(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    private ProfessionDto toDto(Profession p) {
        return ProfessionDto.builder()
                .id(p.getId())
                .code(p.getCode())
                .nameEn(p.getNameEn())
                .nameFr(p.getNameFr())
                .build();
    }
}
