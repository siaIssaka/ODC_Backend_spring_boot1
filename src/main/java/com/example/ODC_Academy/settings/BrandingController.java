package com.example.ODC_Academy.settings;

import com.example.ODC_Academy.exception.BadRequestException;
import com.example.ODC_Academy.media.MediaStorageService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Logos du site modifiables par l'administrateur (en-tête et pied de page).
 * Lecture publique (le front les affiche à tous) ; envoi et rétablissement réservés à l'ADMIN.
 * Sans logo personnalisé, le front affiche le logo Orange par défaut.
 */
@RestController
@RequestMapping("/api/v1/settings/branding")
public class BrandingController {
    public record Branding(String headerLogoKey, String footerLogoKey) { }

    private static final Map<String, String> SLOTS = Map.of("header", "logo.header", "footer", "logo.footer");

    private final SiteSettingRepository settings;
    private final MediaStorageService storage;

    public BrandingController(SiteSettingRepository settings, MediaStorageService storage) {
        this.settings = settings; this.storage = storage;
    }

    private static String settingKey(String slot) {
        String k = SLOTS.get(slot);
        if (k == null) throw new BadRequestException("Emplacement inconnu : utilisez « header » ou « footer ».");
        return k;
    }

    private String value(String key) { return settings.findById(key).map(SiteSetting::getSettingValue).orElse(null); }

    @GetMapping
    public Branding get() { return new Branding(value("logo.header"), value("logo.footer")); }

    @PostMapping(value = "/{slot}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Branding upload(@PathVariable String slot, @RequestParam("file") MultipartFile file) {
        String key = settingKey(slot);
        String newFile = storage.storeImage(file); // PNG / JPG / WEBP, 2 Mo max, type vérifié (pas de SVG : risque de script)
        String old = value(key);
        settings.save(new SiteSetting(key, newFile));
        if (old != null) storage.delete(old);
        return get();
    }

    @DeleteMapping("/{slot}/logo")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Branding reset(@PathVariable String slot) {
        String key = settingKey(slot);
        String old = value(key);
        if (old != null) { settings.deleteById(key); storage.delete(old); }
        return get();
    }
}
