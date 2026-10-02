package com.example.ODC_Academy.formation;

public final class FormationMapper {
    private FormationMapper() { }

    public static FormationDTO toDto(Formation f) {
        var c = f.getCategory();
        return new FormationDTO(f.getId(), f.getTitle(), f.getDescription(), f.isActive(),
                c == null ? null : c.getId(), c == null ? null : c.getName(),
                f.getImageKey(), c == null ? null : c.getImageKey(),
                f.getTrainers().stream().map(t -> new FormationDTO.TrainerBrief(t.getId(),
                        t.getPrenom() + " " + t.getNom(), t.getPhotoKey())).toList());
    }
}
