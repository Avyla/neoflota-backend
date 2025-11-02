package org.avyla.checklists.api.dto.response;

import org.avyla.checklists.domain.enums.ResponseState;
import org.avyla.checklists.domain.enums.SeverityOptions;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class PublishedChecklistResponse {
    public String templateCode;
    public Long versionId;
    public String versionLabel;
    public OffsetDateTime publishedAt;
    public String versionHash;

    // Enum para estados
    public List<ResponseState> stateOptions; // ["OK","OBS","NOOP","NA"]

    // "ExternalLights":[{code,label,order}]
    public Map<String, List<CatalogItemDTO>> detailCatalogs;

    public List<SectionDTO> sections;

    public static class SectionDTO {
        public Long id;
        public String code;
        public String title;
        public int order;
        public List<ItemDTO> items;
    }

    public static class CatalogItemDTO {
        public String code;
        public String label;
        public Integer order;
    }

    public static class ItemDTO {
        public String code;
        public String label;
        public boolean required;
        public boolean allowNA;          // mapeado desde allowNa (entidad)
        public SeverityOptions severity; // LOW|MEDIUM|HIGH|CRITICAL
        public boolean hasDetails;
        public String detailCatalog;     // null si no aplica
        public int order;
        public String helpText;          // opcional (puede venir null)
    }
}
