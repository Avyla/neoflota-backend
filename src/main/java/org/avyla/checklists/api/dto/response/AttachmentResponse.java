package org.avyla.checklists.api.dto.response;

import lombok.*;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AttachmentResponse {
    private String id;       // UUID como String
    private String filename;
    private String type;     // MIME
    private Long size;       // bytes
    private String url;      // /api/attachments/{id}
}
