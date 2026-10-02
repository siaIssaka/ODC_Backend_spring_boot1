package com.example.ODC_Academy.settings;

import jakarta.persistence.*;
import lombok.*;

/** Réglage clé/valeur du site (logos, et futurs réglages d'apparence). */
@Entity
@Table(name = "site_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SiteSetting {
    @Id
    @Column(name = "setting_key", length = 100)
    private String settingKey;
    @Column(name = "setting_value")
    private String settingValue;
}
