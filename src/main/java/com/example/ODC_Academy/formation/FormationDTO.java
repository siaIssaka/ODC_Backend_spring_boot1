package com.example.ODC_Academy.formation;

import java.util.List;

public record FormationDTO(Long id, String title, String description, boolean active,
                           Long categoryId, String categoryName, String imageKey, String categoryImageKey,
                           List<TrainerBrief> trainers) {
    public record TrainerBrief(Long id, String name, String photoKey) { }
}
