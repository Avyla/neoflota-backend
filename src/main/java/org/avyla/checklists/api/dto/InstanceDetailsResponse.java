package org.avyla.checklists.api.dto;

import lombok.Builder;
import lombok.Data;
import org.avyla.checklists.infrastructure.InstanceStatus;

import java.time.Instant;
import java.util.List;

@Data @Builder
public class InstanceDetailsResponse {
    private Long instanceId;
    private InstanceStatus status;
    private Long driverId;
    private Long vehicleId;
    private Integer odometer;
    private String templateCode;
    private Instant startedAt;
    private Instant completedAt;

    private List<ItemDetailDto> responses;
    private Summary summary;

    @Data @Builder
    public static class ItemDetailDto {
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

    @Data @Builder
    public static class Summary {
        private long total;
        private long okCount;
        private long oobCount;
        private long noopCount;
        private long criticalNoopCount;
        private String overall; // "APTO" | "OOB" | "NOOP"
    }
}
