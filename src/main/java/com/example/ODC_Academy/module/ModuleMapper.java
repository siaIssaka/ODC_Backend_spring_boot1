package com.example.ODC_Academy.module;

public final class ModuleMapper {

    private ModuleMapper() {
    }

    public static ModuleDTO toDto(Module module) {
        return new ModuleDTO(
                module.getId(),
                module.getCourse() != null ? module.getCourse().getId() : null,
                module.getTitle(),
                module.getDescription(),
                module.getOrderIndex()
        );
    }
}
