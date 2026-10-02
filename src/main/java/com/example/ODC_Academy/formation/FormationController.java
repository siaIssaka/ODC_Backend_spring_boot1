package com.example.ODC_Academy.formation;

import com.example.ODC_Academy.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/formations")
@Tag(name = "Formations", description = "Gestion des formations")
@SecurityRequirement(name = "bearerAuth")
public class FormationController {

    private final FormationService formationService;
    private final SecurityUtils securityUtils;

    public FormationController(FormationService formationService, SecurityUtils securityUtils) {
        this.formationService = formationService;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public List<FormationDTO> getAll() {
        return formationService.getAll().stream().map(FormationMapper::toDto).toList();
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('FORMATEUR')")
    @Operation(summary = "Formations attribuées au formateur connecté")
    public List<FormationDTO> getMine() {
        return formationService.getForTrainer(securityUtils.getCurrentUser().getId())
                .stream().map(FormationMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public FormationDTO getById(@PathVariable Long id) {
        return FormationMapper.toDto(formationService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Créer une formation (ADMIN uniquement)")
    public ResponseEntity<FormationDTO> create(@Valid @RequestBody FormationRequestDTO request) {
        Formation created = formationService.create(request, securityUtils.getCurrentUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(FormationMapper.toDto(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Modifier une formation (ADMIN uniquement)")
    public FormationDTO update(@PathVariable Long id, @Valid @RequestBody FormationRequestDTO request) {
        return FormationMapper.toDto(formationService.update(id, request, securityUtils.getCurrentUser().getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer une formation et son contenu associé (ADMIN uniquement)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        formationService.delete(id, securityUtils.getCurrentUser().getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{formationId}/trainers/{trainerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Attribuer un formateur à une formation (ADMIN uniquement)")
    public FormationDTO assignTrainer(@PathVariable Long formationId, @PathVariable Long trainerId) {
        return FormationMapper.toDto(formationService.addTrainer(
                formationId, trainerId, securityUtils.getCurrentUser().getId()));
    }
}
