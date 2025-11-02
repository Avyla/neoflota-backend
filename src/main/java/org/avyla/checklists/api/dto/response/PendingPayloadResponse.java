package org.avyla.checklists.api.dto.response;

import lombok.Builder;
import lombok.Data;
import org.avyla.checklists.domain.enums.InstanceStatus;

import java.time.Instant;
import java.util.List;

@Data @Builder
public class PendingPayloadResponse {
    private Long instanceId;
    private InstanceStatus status;
    private Instant startedAt;
    private Instant dueAt;
    private long timeRemainingSec;

    private String templateCode;
    private List<ItemSnapshotDto> partialResponses;

    @Data @Builder
    public static class ItemSnapshotDto {
        private String itemCode;
        private String state;
        private List<String> details;
        private String comment;
        private List<AttachmentLite> attachments;
    }

    @Data @Builder
    public static class AttachmentLite {
        private String id;
        private String filename;
        private String url;
        private String type;
        private Long size;
    }
}
