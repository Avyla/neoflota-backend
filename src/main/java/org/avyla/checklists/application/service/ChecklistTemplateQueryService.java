package org.avyla.checklists.application.service;

import org.avyla.checklists.api.dto.PublishedChecklistResponse;
import org.avyla.checklists.api.dto.PublishedChecklistResponse.CatalogItemDTO;
import org.avyla.checklists.api.dto.PublishedChecklistResponse.ItemDTO;
import org.avyla.checklists.api.dto.PublishedChecklistResponse.SectionDTO;
import org.avyla.checklists.domain.repo.ChecklistItemRepository;
import org.avyla.checklists.domain.repo.ChecklistSectionRepository;
import org.avyla.checklists.domain.repo.ChecklistVersionRepository;
import org.avyla.checklists.domain.repo.OptionItemRepository;
import org.avyla.checklists.infrastructure.ResponseState;
import org.avyla.checklists.infrastructure.SeverityOptions;
import org.avyla.common.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChecklistTemplateQueryService {

    private final ChecklistVersionRepository versionRepo;
    private final ChecklistSectionRepository sectionRepo;
    private final ChecklistItemRepository itemRepo;
    private final OptionItemRepository optionItemRepo;

    public ChecklistTemplateQueryService(
            ChecklistVersionRepository versionRepo,
            ChecklistSectionRepository sectionRepo,
            ChecklistItemRepository itemRepo,
            OptionItemRepository optionItemRepo
    ) {
        this.versionRepo = versionRepo;
        this.sectionRepo = sectionRepo;
        this.itemRepo = itemRepo;
        this.optionItemRepo = optionItemRepo;
    }

    @Transactional(readOnly = true)
    public PublishedChecklistResponse getPublishedDesign(String templateCode) {
        var version = versionRepo
                .findTopByTemplate_CodeAndStatusOrderByPublishedAtDesc(templateCode, "Published")
                .orElseGet(() -> versionRepo.findLatestPublishedByTemplateCode(templateCode)
                        .orElseThrow(() -> new NotFoundException("No hay versión publicada para la plantilla: " + templateCode)));

        Long versionId = version.getId();

        var sections = sectionRepo.findViewsByVersionId(versionId);
        var items = itemRepo.findViewsByVersionId(versionId);

        // Catálogo base de estado (OK/OBS/NOOP/NA)
        List<ResponseState> stateOptions = optionItemRepo
                .findActiveByGroupCode("EstadoGeneral")
                .stream()
                .map(ov -> ResponseState.from(ov.getCode()))
                .toList();

        // Catálogos de detalle usados por esta versión
        LinkedHashSet<String> detailGroupCodes = items.stream()
                .map(ChecklistItemRepository.ItemView::getDetailCatalogCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, List<CatalogItemDTO>> detailCatalogs = new LinkedHashMap<>();
        if (!detailGroupCodes.isEmpty()) {
            List<OptionItemRepository.OptionView> all =
                    optionItemRepo.findActiveViewsByGroupCodes(new ArrayList<>(detailGroupCodes));

            Map<String, List<OptionItemRepository.OptionView>> byGroup = all.stream()
                    .collect(Collectors.groupingBy(OptionItemRepository.OptionView::getGroupCode,
                            LinkedHashMap::new, Collectors.toList()));

            for (String g : detailGroupCodes) {
                List<OptionItemRepository.OptionView> list = byGroup.getOrDefault(g, Collections.emptyList());
                List<CatalogItemDTO> dtos = list.stream().map(ov -> {
                    CatalogItemDTO c = new CatalogItemDTO();
                    c.code = ov.getCode();
                    c.label = ov.getLabel();
                    c.order = ov.getOrderIndex() != null ? ov.getOrderIndex() : 0;
                    return c;
                }).toList();
                detailCatalogs.put(g, dtos);
            }
        }

        // Agrupar ítems por sección (id)
        Map<Long, List<ChecklistItemRepository.ItemView>> bySection = items.stream()
                .collect(Collectors.groupingBy(ChecklistItemRepository.ItemView::getSectionId,
                        LinkedHashMap::new, Collectors.toList()));

        // Map a DTO
        List<SectionDTO> sectionDTOs = new ArrayList<>();
        for (ChecklistSectionRepository.SectionView s : sections) {
            SectionDTO sd = new SectionDTO();
            sd.id = s.getId();
            sd.code = s.getCode();
            sd.title = s.getTitle();
            sd.order = s.getOrderIndex() != null ? s.getOrderIndex() : 0;

            List<ChecklistItemRepository.ItemView> sectionItems =
                    bySection.getOrDefault(s.getId(), Collections.emptyList());

            List<ItemDTO> itemDTOs = sectionItems.stream().map(iv -> {
                ItemDTO idto = new ItemDTO();
                idto.code = iv.getCode();
                idto.label = iv.getLabel();
                idto.required = Boolean.TRUE.equals(iv.getRequired());
                idto.allowNA = Boolean.TRUE.equals(iv.getAllowNa());
                idto.severity = SeverityOptions.from(iv.getSeverity()); // String -> enum
                idto.hasDetails = (iv.getDetailCatalogCode() != null);
                idto.detailCatalog = iv.getDetailCatalogCode();
                idto.order = iv.getOrderIndex() != null ? iv.getOrderIndex() : 0;
                idto.helpText = iv.getHelpText(); // si no existe, quedará null desde el query
                return idto;
            }).toList();

            sd.items = itemDTOs;
            sectionDTOs.add(sd);
        }

        PublishedChecklistResponse out = new PublishedChecklistResponse();
        out.templateCode = version.getTemplate().getCode();
        out.versionId = versionId;
        out.versionLabel = version.getVersionLabel();
        out.publishedAt = (version.getPublishedAt() != null)
                ? OffsetDateTime.ofInstant(version.getPublishedAt(), ZoneOffset.UTC)
                : OffsetDateTime.now(ZoneOffset.UTC);
        out.stateOptions = stateOptions;
        out.detailCatalogs = detailCatalogs;
        out.sections = sectionDTOs;
        out.versionHash = computeVersionHash(out);

        return out;
    }

    private String computeVersionHash(PublishedChecklistResponse dto) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            var sb = new StringBuilder();
            sb.append(dto.templateCode).append('|')
                    .append(dto.versionId).append('|')
                    .append(dto.versionLabel).append('|')
                    .append(dto.publishedAt != null ? dto.publishedAt.toString() : "null").append('|');

            for (ResponseState st : dto.stateOptions) sb.append(st.name()).append(',');

            if (dto.sections != null) {
                for (SectionDTO s : dto.sections) {
                    sb.append(s.code).append(':').append(s.order).append('|');
                    if (s.items != null) {
                        for (ItemDTO i : s.items) {
                            sb.append(i.code).append(',')
                                    .append(i.order).append(',')
                                    .append(i.required).append(',')
                                    .append(i.allowNA).append(',')
                                    .append(Objects.toString(i.severity, "null")).append(',')
                                    .append(i.hasDetails).append(',')
                                    .append(Objects.toString(i.detailCatalog, "null"))
                                    .append('|');
                        }
                    }
                }
            }

            if (dto.detailCatalogs != null) {
                dto.detailCatalogs.forEach((g, list) -> {
                    sb.append('#').append(g).append(':');
                    for (CatalogItemDTO c : list) sb.append(c.code).append(',');
                });
            }

            byte[] hash = md.digest(sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return "nohash";
        }
    }
}
