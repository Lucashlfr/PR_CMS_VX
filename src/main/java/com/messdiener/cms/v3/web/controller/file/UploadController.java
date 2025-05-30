package com.messdiener.cms.v3.web.controller.file;

import com.messdiener.cms.v3.app.entities.document.StorageFile;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.document.StorageService;
import com.messdiener.cms.v3.exception.StorageException;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.enums.document.FileType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class UploadController {

    private final SecurityHelper securityHelper;
    private final StorageService storageService;

    @PostMapping(path = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadFiles(@RequestParam("target") UUID targetId, @RequestParam("type")String type, @RequestParam("files") MultipartFile[] files) throws IOException {
        Person user = securityHelper.getPerson()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Path uploadDir = Paths.get("./cms_vx/uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        StringBuilder sb = new StringBuilder();
        UUID fileId = UUID.randomUUID();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String filename = fileId + (extension != null && !extension.isEmpty() ? "." + extension : "");

            // Optional: Dateinamen eindeutiger machen
            String ext = FilenameUtils.getExtension(filename);
            String base = FilenameUtils.getBaseName(filename);
            String storedName = base + "_" + System.currentTimeMillis() + "." + ext;

            try {
                Path target = uploadDir.resolve(storedName);
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                sb.append("Hochgeladen: ").append(storedName).append("\n");

                storageService.store(new StorageFile(UUID.randomUUID(), 0, user.getId(), targetId, CMSDate.current(), filename, CMSDate.current(), 10, FileType.valueOf(type), "./cms_vx/uploads/"+storedName));

            } catch (IOException | SQLException e) {
                return ResponseEntity
                        .status(500)
                        .body("Fehler beim Speichern von " + filename + ": " + e.getMessage());
            }
        }
        return ResponseEntity.ok(sb.toString());
    }

    @GetMapping("/img")
    public ResponseEntity<ByteArrayResource> getFile(@RequestParam("id") UUID id,
            @RequestParam(name = "download", defaultValue = "false") boolean downloadFlag,
            @RequestParam(name = "w", required = false) Integer width,
            @RequestParam(name = "h", required = false) Integer height) {
        File file;
        try {
            file = storageService.load(id.toString());
        } catch (StorageException e) {
            return getFallbackImage(width, height);
        }

        // Default-Größe, falls nichts übergeben
        int targetWidth  = (width  != null ? width  : 800);
        int targetHeight = (height != null ? height : 600);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // Bild laden und skalieren
            Thumbnails.of(file)
                    .size(targetWidth, targetHeight)
                    .outputFormat("jpg")      // oder png/webp
                    .outputQuality(0.85f)     // 85% Qualität
                    .toOutputStream(baos);
        } catch (IOException e) {
            return getFallbackImage(width, height);
        }

        byte[] imageBytes = baos.toByteArray();
        ByteArrayResource resource = new ByteArrayResource(imageBytes);

        // Content-Type ermitteln
        MediaType mediaType = MediaType.IMAGE_JPEG;
        try {
            String mime = Files.probeContentType(file.toPath());
            if (mime != null) {
                mediaType = MediaType.parseMediaType(mime);
            }
        } catch (IOException ignored) {}

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentLength(imageBytes.length);
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                (downloadFlag ? "attachment" : "inline") +
                        "; filename=\"" + file.getName() + "\"");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    private ResponseEntity<ByteArrayResource> getFallbackImage(Integer width, Integer height) {
        // Standard-Maße, falls nicht übergeben
        int targetWidth  = (width  != null ? width  : 800);
        int targetHeight = (height != null ? height : 600);

        try {
            // Laden des externen Bildes
            URL url = new URL("https://messdiener.elementor.cloud/wp-content/uploads/2022/03/6-1536x864.png");
            BufferedImage original = ImageIO.read(url);

            // Resizing mit Thumbnailator
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Thumbnails.of(original)
                    .size(targetWidth, targetHeight)
                    .outputFormat("png")     // da der Fallback ein PNG ist
                    .outputQuality(0.85f)    // 85% Qualität
                    .toOutputStream(baos);

            byte[] imageBytes = baos.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(imageBytes);

            // Header aufbauen
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(imageBytes.length);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"fallback.png\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException ioException) {
            // Wenn Fallback nicht geladen/gescaled werden kann, 404
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Datei nicht gefunden und Fallback-Bild konnte nicht geladen werden",
                    ioException
            );
        }
    }

    @GetMapping("/file")
    public ResponseEntity<ByteArrayResource> getFile(@RequestParam("id") UUID id) throws SQLException {
        // 1. Metadaten laden
        StorageFile storageFile = storageService.getFile(id)
                .orElseThrow(() -> new StorageException("Datei nicht gefunden: " + id));

        // 2. Datei laden
        byte[] data;
        try {
            File file = storageService.loadFile(storageFile.getPath());
            data = Files.readAllBytes(file.toPath());
        } catch (IOException | StorageException e) {
            throw new StorageException("Datei nicht gefunden: " + id, e);
        }

        // 3. MIME-Type ermitteln (Fallback auf application/octet-stream)
        String mimeType;
        try {
            mimeType = Files.probeContentType(Paths.get(storageFile.getPath()));
        } catch (IOException e) {
            mimeType = null;
        }
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        // 4. Content-Disposition bestimmen
        // Inline nur bei PDFs und Bildern
        String dispositionType = mimeType.equals("application/pdf")
                || mimeType.startsWith("image/")
                ? "inline"
                : "attachment";
        String filename = storageFile.getTitle();
        String contentDisposition = ContentDisposition.builder(dispositionType)
                .filename(filename, StandardCharsets.UTF_8)
                .build()
                .toString();

        // 5. Response aufbauen
        ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mimeType))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentLength(data.length)
                .body(resource);
    }


}
