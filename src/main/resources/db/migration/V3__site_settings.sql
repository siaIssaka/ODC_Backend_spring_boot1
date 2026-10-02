-- Réglages du site (logos d'en-tête et de pied de page configurables par l'administrateur)
CREATE TABLE IF NOT EXISTS site_settings (
    setting_key   varchar(100) PRIMARY KEY,
    setting_value varchar(255)
);
