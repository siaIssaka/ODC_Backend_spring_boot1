package com.example.ODC_Academy.settings;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationSettingsService {
    public static final String CLOSED_MESSAGE =
            "Aucune formation n'est ouverte pour le moment. Veuillez attendre le prochain lancement des inscriptions.";
    private static final String REGISTRATION_OPEN_KEY = "registration.open";

    private final SiteSettingRepository settings;

    public RegistrationSettingsService(SiteSettingRepository settings) {
        this.settings = settings;
    }

    @Transactional(readOnly = true)
    public boolean isOpen() {
        return settings.findById(REGISTRATION_OPEN_KEY)
                .map(SiteSetting::getSettingValue)
                .map(Boolean::parseBoolean)
                .orElse(true);
    }

    @Transactional
    public boolean setOpen(boolean open) {
        settings.save(new SiteSetting(REGISTRATION_OPEN_KEY, Boolean.toString(open)));
        return open;
    }
}
