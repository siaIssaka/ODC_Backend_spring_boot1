package com.example.ODC_Academy.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.ODC_Academy.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaStorageService {
    private static final String CLOUDINARY_IMAGE_FOLDER = "odc-academy/images";
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024;
    private static final long MAX_DOCUMENT_SIZE = 20L * 1024 * 1024;
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "webm", "mov");
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of("pdf", "docx", "pptx");
    private static final Set<String> SUBMISSION_EXTENSIONS = Set.of("pdf", "doc", "docx", "txt");

    private final Path root;
    private final Cloudinary cloudinary;
    private final String cloudinaryCloudName;

    public MediaStorageService(
            @Value("${app.storage.location:uploads}") String location,
            @Value("${app.storage.provider:local}") String provider,
            @Value("${app.cloudinary.cloud-name:}") String cloudName,
            @Value("${app.cloudinary.api-key:}") String apiKey,
            @Value("${app.cloudinary.api-secret:}") String apiSecret) {
        this.root = Paths.get(location).toAbsolutePath().normalize();
        String normalizedProvider = provider.trim().toLowerCase(Locale.ROOT);
        if (normalizedProvider.equals("local")) {
            this.cloudinary = null;
            this.cloudinaryCloudName = null;
        } else if (normalizedProvider.equals("cloudinary")) {
            if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
                throw new IllegalStateException("Cloudinary nécessite CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY et CLOUDINARY_API_SECRET");
            }
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true));
            this.cloudinaryCloudName = cloudName;
        } else {
            throw new IllegalArgumentException("MEDIA_STORAGE_PROVIDER doit valoir local ou cloudinary");
        }
    }

    public String store(MultipartFile file, boolean video) {
        if (file == null || file.isEmpty()) return null;
        long maxSize = video ? MAX_VIDEO_SIZE : MAX_DOCUMENT_SIZE;
        if (file.getSize() > maxSize) {
            throw new BadRequestException(video ? "La vidéo dépasse la limite de 100 Mo" : "Le document dépasse la limite de 20 Mo");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extension(original);
        Set<String> allowed = video ? VIDEO_EXTENSIONS : DOCUMENT_EXTENSIONS;
        if (!allowed.contains(extension)) {
            throw new BadRequestException(video ? "Formats vidéo acceptés : MP4, WEBM, MOV" : "Formats acceptés : PDF, DOCX, PPTX");
        }
        String expectedMime = mimeType(extension);
        if (file.getContentType() != null && !file.getContentType().equalsIgnoreCase(expectedMime)
                && !(extension.equals("mov") && file.getContentType().equalsIgnoreCase("video/quicktime"))) {
            throw new BadRequestException("Le type du fichier ne correspond pas à son extension");
        }
        String key = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(root);
            try (var input = file.getInputStream()) {
                Files.copy(input, root.resolve(key));
            }
            return key;
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'enregistrer le fichier", ex);
        }
    }

    public String storeSubmission(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("Choisissez un fichier à déposer");
        if (file.getSize() > MAX_DOCUMENT_SIZE) throw new BadRequestException("Le fichier dépasse la limite de 20 Mo");
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = extension(original);
        if (!SUBMISSION_EXTENSIONS.contains(ext)) {
            throw new BadRequestException("Formats acceptés pour un devoir : PDF, DOC, DOCX, TXT");
        }
        String expectedMime = mimeType(ext);
        String providedMime = file.getContentType();
        if (providedMime != null && !providedMime.equalsIgnoreCase(expectedMime)
                && !(ext.equals("txt") && providedMime.toLowerCase(Locale.ROOT).startsWith("text/plain"))) {
            throw new BadRequestException("Le type du fichier ne correspond pas à son extension");
        }
        String key = UUID.randomUUID() + "." + ext;
        try {
            Files.createDirectories(root);
            try (var input = file.getInputStream()) {
                Files.copy(input, root.resolve(key));
            }
            return key;
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'enregistrer le fichier", ex);
        }
    }

    public String storeTextSubmission(String text) {
        if (text == null || text.isBlank()) throw new BadRequestException("Saisissez votre réponse avant de l'envoyer");
        if (text.length() > 20_000) throw new BadRequestException("La réponse texte ne peut pas dépasser 20 000 caractères");
        String key = UUID.randomUUID() + ".txt";
        try {
            Files.createDirectories(root);
            Files.writeString(root.resolve(key), text, StandardCharsets.UTF_8);
            return key;
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'enregistrer la réponse texte", ex);
        }
    }

    /** Photo de profil : PNG/JPG/WEBP, 2 Mo max. */
    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("Image manquante");
        if (file.getSize() > 2L * 1024 * 1024) throw new BadRequestException("L'image dépasse 2 Mo");
        String ext = extension(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (!Set.of("png", "jpg", "jpeg", "webp").contains(ext)) throw new BadRequestException("Formats acceptés : PNG, JPG, WEBP");
        if (file.getContentType() == null || !file.getContentType().equalsIgnoreCase(mimeType(ext)))
            throw new BadRequestException("Le type du fichier ne correspond pas à son extension");
        if (cloudinary != null) return storeImageInCloudinary(file);
        String key = UUID.randomUUID() + "." + ext;
        try {
            Files.createDirectories(root);
            try (var in = file.getInputStream()) { Files.copy(in, root.resolve(key)); }
            return key;
        } catch (IOException ex) { throw new IllegalStateException("Impossible d'enregistrer l'image", ex); }
    }

    public Resource load(String key) {
        if (key == null || !key.matches("[a-fA-F0-9-]+\\.(mp4|webm|mov|pdf|doc|docx|pptx|txt|png|jpg|jpeg|webp)")) {
            throw new BadRequestException("Fichier invalide");
        }
        try {
            Path file = root.resolve(key).normalize();
            if (!file.startsWith(root) || !Files.isRegularFile(file)) throw new BadRequestException("Fichier introuvable");
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) throw new BadRequestException("Fichier introuvable");
            return resource;
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de lire le fichier", ex);
        }
    }

    public String contentType(String key) { return mimeType(extension(key)); }

    public void delete(String key) {
        if (isCloudinaryImage(key)) {
            deleteCloudinaryImage(key);
            return;
        }
        if (key == null || !key.matches("[a-fA-F0-9-]+\\.(mp4|webm|mov|pdf|doc|docx|pptx|txt|png|jpg|jpeg|webp)")) {
            throw new BadRequestException("Fichier invalide");
        }
        try {
            Path file = root.resolve(key).normalize();
            if (!file.startsWith(root)) throw new BadRequestException("Fichier invalide");
            Files.deleteIfExists(file);
        } catch (NoSuchFileException ignored) {
            // Idempotent cleanup: a previous Render instance may already have lost its ephemeral file.
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de supprimer le fichier", ex);
        }
    }

    public void deleteMediaUrl(String url) {
        if (url == null) return;
        try {
            String path = java.net.URI.create(url).getPath();
            if (path == null || !path.startsWith("/api/v1/media/")) return;
            String key = path.substring("/api/v1/media/".length());
            if (key.indexOf('/') < 0) delete(key);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Adresse de média invalide");
        }
    }

    private String extension(String name) {
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String storeImageInCloudinary(MultipartFile file) {
        byte[] image;
        try {
            image = file.getBytes();
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de lire l'image envoyée à Cloudinary", ex);
        }
        Map<?, ?> result;
        try {
            result = cloudinary.uploader().upload(image, ObjectUtils.asMap(
                    "folder", CLOUDINARY_IMAGE_FOLDER,
                    "public_id", UUID.randomUUID().toString(),
                    "resource_type", "image"));
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'envoyer l'image vers Cloudinary", ex);
        }
        Object secureUrl = result.get("secure_url");
        if (!(secureUrl instanceof String url) || url.isBlank()) {
            throw new IllegalStateException("Cloudinary n'a pas renvoyé l'adresse sécurisée de l'image");
        }
        return url;
    }

    private boolean isCloudinaryImage(String value) {
        if (value == null) return false;
        try {
            URI uri = URI.create(value);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && "res.cloudinary.com".equalsIgnoreCase(uri.getHost());
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private void deleteCloudinaryImage(String url) {
        if (cloudinary == null) {
            throw new IllegalStateException("Cette image est sur Cloudinary, mais MEDIA_STORAGE_PROVIDER=cloudinary et ses identifiants ne sont pas configurés");
        }
        URI uri = URI.create(url);
        String prefix = "/" + cloudinaryCloudName + "/image/upload/";
        String path = uri.getPath();
        if (path == null || !path.startsWith(prefix)) {
            throw new BadRequestException("L'adresse de l'image ne correspond pas au compte Cloudinary configuré");
        }
        String publicIdWithExtension = path.substring(prefix.length());
        publicIdWithExtension = publicIdWithExtension.replaceFirst("^v[0-9]+/", "");
        int extensionIndex = publicIdWithExtension.lastIndexOf('.');
        String publicId = extensionIndex < 0 ? publicIdWithExtension : publicIdWithExtension.substring(0, extensionIndex);
        if (!publicId.startsWith(CLOUDINARY_IMAGE_FOLDER + "/")
                || !publicId.matches("[A-Za-z0-9_./-]+")
                || publicId.contains("..")) {
            throw new BadRequestException("Identifiant d'image Cloudinary invalide");
        }
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", "image",
                    "invalidate", true));
            Object status = result.get("result");
            if (!"ok".equals(status) && !"not found".equals(status)) {
                throw new IllegalStateException("Cloudinary n'a pas confirmé la suppression de l'image");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible de supprimer l'image de Cloudinary", ex);
        }
    }

    private String mimeType(String extension) {
        return switch (extension) {
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "mov" -> "video/quicktime";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "webp" -> "image/webp";
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }
}
