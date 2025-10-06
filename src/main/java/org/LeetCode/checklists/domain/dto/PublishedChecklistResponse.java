package org.LeetCode.checklists.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class PublishedChecklistResponse {
    public String templateCode;
    public Long versionId;
    public String versionLabel;
    public OffsetDateTime publishedAt;
    public String versionHash;

    public List<String> stateOptions; // ["OK","OBS","NOOP","NA"]

    public Map<String, List<CatalogItemDTO>> detailCatalogs; // "ExternalLights":[{code,label,order}]

    public List<SectionDTO> sections;

    public static class CatalogItemDTO {
        public String code;
        public String label;
        public int order;
    }

    public static class SectionDTO {
        public Long id;
        public String code;
        public String title;
        public int order;
        public List<ItemDTO> items;
    }

    public static class ItemDTO {
        public String code;
        public String label;
        public boolean required;
        public boolean allowNA;     // mapeado desde allowNa (entidad)
        public String severity;     // Low|Medium|High|Critical
        public boolean hasDetails;
        public String detailCatalog; // null si no aplica
        public int order;
        public String helpText;     // opcional (puede venir null)
    }
}
