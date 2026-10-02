INSERT INTO site_settings (setting_key, setting_value)
VALUES ('registration.open', 'true')
ON CONFLICT (setting_key) DO NOTHING;
