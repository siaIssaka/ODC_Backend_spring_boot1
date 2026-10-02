package com.example.ODC_Academy.module;

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
@RequestMapping("/api/v1/modules")
@Tag(name = "Modules", description = "Modules d'un cours")
@SecurityRequirement(name = "bearerAuth")
public class ModuleController {

    private final ModuleService moduleService;
    private final SecurityUtils securityUtils;

    public ModuleController(ModuleService moduleService, SecurityUtils securityUtils) {
        this.moduleService = moduleService;
        this.securityUtils = securityUtils;
    }

    @GetMapping("/course/{courseId}")
    public List<ModuleDTO> getByCourseId(@PathVariable Long courseId) {
        return moduleService.getByCourseId(courseId).stream().map(ModuleMapper::toDto).toList();
    }

    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('FORMATEUR', 'ADMIN')")
    @Operation(summary = "Créer un module dans un cours (FORMATEUR/ADMIN)")
    public ResponseEntity<ModuleDTO> create(@PathVariable Long courseId,
                                            @Valid @RequestBody ModuleRequestDTO request) {
        Module created = moduleService.create(courseId, securityUtils.getCurrentUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ModuleMapper.toDto(created));
    }
}
