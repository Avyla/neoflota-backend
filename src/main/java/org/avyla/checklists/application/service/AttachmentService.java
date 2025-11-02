package org.avyla.checklists.application.service;

import org.avyla.checklists.api.dto.response.AttachmentResponse;
import org.avyla.checklists.domain.entity.ChecklistAttachment;
import org.avyla.checklists.domain.entity.ChecklistInstance;
import org.avyla.checklists.domain.repo.ChecklistAttachmentRepository;
import org.avyla.checklists.domain.repo.ChecklistInstanceRepository;
import org.avyla.checklists.domain.repo.ChecklistResponseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_MIME = List.of(
            "image/jpeg", "image/png", "application/pdf"
    );

    private final ChecklistAttachmentRepository attachmentRepo;
    private final ChecklistResponseRepository responseRepo;
    private final ChecklistInstanceRepository instanceRepo;

    // ===== Subida por respuesta (1 evidencia por ítem) =====
    @Transactional
    public AttachmentResponse uploadForResponse(Long responseId,
                                                MultipartFile file,
                                                Long currentUserId) {
        var response = responseRepo.findById(responseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Respuesta no encontrada"));

        // Regla: solo 1 evidencia por respuesta
        if (attachmentRepo.existsByResponse_Id(responseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La respuesta ya tiene evidencia");
        }

        var safeName = sanitizeFilename(file.getOriginalFilename());
        var bytes = readWithLimit(file);
        var mime = detectMime(bytes, file.getContentType());

        if (!ALLOWED_MIME.contains(mime)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de archivo no permitido");
        }

        var entity = ChecklistAttachment.builder()
                .filename(safeName)
                .type(mime)
                .size((long) bytes.length)
                .data(bytes)
                .response(response)     // scope RESPUESTA
                .instance(null)
                .createdByUserId(currentUserId)
                .createdAt(Instant.now())
                .build();

        var saved = attachmentRepo.save(entity);
        return toDto(saved);
    }

    // ===== Listado por respuesta =====
    @Transactional
    public List<AttachmentResponse> listForResponse(Long responseId) {
        return attachmentRepo.findAllByResponse_Id(responseId)
                .stream().map(this::toDto).toList();
    }

    // ===== Descargar =====
    @Transactional
    public ChecklistAttachment getAttachmentOr404(UUID id) {
        return attachmentRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Adjunto no encontrado"));
    }

    // ===== Eliminar =====
    @Transactional
    public void delete(UUID id) {
        if (!attachmentRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Adjunto no encontrado");
        }
        attachmentRepo.deleteById(id);
    }

    // ===== Utilidades =====
    private String sanitizeFilename(String name) {
        var cleaned = StringUtils.hasText(name) ? StringUtils.getFilename(name) : "file";
        // evita control chars
        return cleaned.replaceAll("[\\p{Cntrl}]+", "");
    }

    private byte[] readWithLimit(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo vacío");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Archivo excede el tamaño permitido");
        }
        try {
            return file.getBytes(); // suficientemente seguro con límite ya validado
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo");
        }
    }

    // Detección mínima por magic numbers + fallback a contentType del cliente
    private String detectMime(byte[] bytes, String clientType) {
        if (bytes.length >= 4) {
            // JPEG: FF D8 FF
            if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
                return "image/jpeg";
            }
            // PNG: 89 50 4E 47
            if ((bytes[0] & 0xFF) == 0x89 && (bytes[1] & 0xFF) == 0x50 &&
                    (bytes[2] & 0xFF) == 0x4E && (bytes[3] & 0xFF) == 0x47) {
                return "image/png";
            }
            // PDF: 25 50 44 46 (%PDF)
            if ((bytes[0] & 0xFF) == 0x25 && (bytes[1] & 0xFF) == 0x50 &&
                    (bytes[2] & 0xFF) == 0x44 && (bytes[3] & 0xFF) == 0x46) {
                return "application/pdf";
            }
        }
        // Fallback: confía si coincide con whitelist
        if (clientType != null && ALLOWED_MIME.contains(clientType)) {
            return clientType;
        }
        return "application/octet-stream"; // rechazado por whitelist en upload
    }

    private AttachmentResponse toDto(ChecklistAttachment a) {
        var url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/attachments/")
                .path(a.getId().toString())
                .toUriString();

        return AttachmentResponse.builder()
                .id(a.getId().toString())
                .filename(a.getFilename())
                .type(a.getType())
                .size(a.getSize())
                .url(url)
                .build();
    }

    // ===== (opcional) subir a nivel de instancia =====
    @Transactional
    public AttachmentResponse uploadForInstance(Long instanceId,
                                                MultipartFile file,
                                                Long currentUserId) {
        ChecklistInstance instance = instanceRepo.findById(instanceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Instancia no encontrada"));

        var safeName = sanitizeFilename(file.getOriginalFilename());
        var bytes = readWithLimit(file);
        var mime = detectMime(bytes, file.getContentType());
        if (!ALLOWED_MIME.contains(mime)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de archivo no permitido");
        }

        var entity = ChecklistAttachment.builder()
                .filename(safeName)
                .type(mime)
                .size((long) bytes.length)
                .data(bytes)
                .response(null)        // scope INSTANCIA
                .instance(instance)
                .createdByUserId(currentUserId)
                .createdAt(Instant.now())
                .build();

        var saved = attachmentRepo.save(entity);
        return toDto(saved);
    }
}
