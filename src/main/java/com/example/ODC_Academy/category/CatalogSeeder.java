package com.example.ODC_Academy.category;

import com.example.ODC_Academy.formation.Formation;
import com.example.ODC_Academy.formation.FormationRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Amorçage idempotent : catégories du menu + formations « Développement ». L'admin ajoute le reste. */
@Component
public class CatalogSeeder implements CommandLineRunner {
    private final CategoryRepository categories;
    private final FormationRepository formations;

    public CatalogSeeder(CategoryRepository categories, FormationRepository formations) {
        this.categories = categories;
        this.formations = formations;
    }

    @Override
    public void run(String... args) {
        if (categories.count() > 0) return;
        Category dev = categories.save(Category.builder().name("Développement").build());
        for (String n : List.of("IA et Data", "E-business", "Technologies émergentes et cybersécurité")) {
            categories.save(Category.builder().name(n).build());
        }
        for (String t : List.of("Développement logiciel Java", "Développement PHP", "Développement Python",
                "Low code / No code", "Développement Front-end", "Développement Mobile")) {
            formations.save(Formation.builder().title("Certificat en " + t).category(dev).build());
        }
    }
}
