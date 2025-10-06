package org.LeetCode.checklists.application.service;

import org.LeetCode.checklists.domain.dto.PublishedChecklistResponse;
import org.LeetCode.checklists.domain.dto.PublishedChecklistResponse.CatalogItemDTO;
import org.LeetCode.checklists.domain.dto.PublishedChecklistResponse.ItemDTO;
import org.LeetCode.checklists.domain.dto.PublishedChecklistResponse.SectionDTO;
import org.LeetCode.checklists.domain.model.ChecklistVersion;
import org.LeetCode.checklists.domain.model.OptionItem;
import org.LeetCode.checklists.domain.repo.ChecklistItemRepository;
import org.LeetCode.checklists.domain.repo.ChecklistSectionRepository;
import org.LeetCode.checklists.domain.repo.ChecklistVersionRepository;
import org.LeetCode.checklists.domain.repo.OptionItemRepository;
import org.LeetCode.common.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Expone el diseño de la "versión publicada" para que el frontend renderice el checklist en modo data-driven.
 * Usa proyecciones declaradas en los repos (SectionView / ItemView / OptionView) para no depender de nombres de getters en entidades.
 */
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
        // Intenta método derivado; si no aplica a tu modelo, usa el @Query alternativo.
        ChecklistVersion version = versionRepo
                .findTopByTemplate_CodeAndStatusOrderByPublishedAtDesc(templateCode, "Published")
                .orElseGet(() -> versionRepo.findLatestPublishedByTemplateCode(templateCode)
                        .orElseThrow(() -> new NotFoundException("No hay versión publicada para la plantilla: " + templateCode)));

        Long versionId = version.getId();

        // Proyecciones desde los repos (NO entidades)
        List<ChecklistSectionRepository.SectionView> sections =
                sectionRepo.findViewsByVersionId(versionId);

        List<ChecklistItemRepository.ItemView> items =
                itemRepo.findViewsByVersionId(versionId);

        // Catálogo base de estado (OK/OBS/NOOP/NA)
        List<String> stateOptions = optionItemRepo
                .findActiveByGroupCode("EstadoGeneral")
                .stream()
                .map(OptionItem::getCode)
                .toList();

        // Catálogos de detalle realmente usados por esta versión (evita llamar getOptionGroup() sobre la entidad)
        LinkedHashSet<String> detailGroupCodes = items.stream()
                .map(ChecklistItemRepository.ItemView::getDetailCatalogCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, List<CatalogItemDTO>> detailCatalogs = new LinkedHashMap<>();
        if (!detailGroupCodes.isEmpty()) {
            // Nueva proyección con groupCode → no dependemos del nombre del campo en OptionItem
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
                // Campo de entidad es allowNa -> en el DTO se llama allowNA
                idto.allowNA = Boolean.TRUE.equals(iv.getAllowNa());
                idto.severity = iv.getSeverity();
                idto.hasDetails = (iv.getDetailCatalogCode() != null);
                idto.detailCatalog = iv.getDetailCatalogCode();
                idto.order = iv.getOrderIndex() != null ? iv.getOrderIndex() : 0;
                idto.helpText = iv.getHelpText(); // si no existe en tu entidad, quedará null desde el query
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
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            sb.append(dto.templateCode).append('|')
                    .append(dto.versionId).append('|')
                    .append(dto.versionLabel).append('|');

            for (SectionDTO s : dto.sections) {
                sb.append(s.code).append(':').append(s.order).append('|');
                for (ItemDTO i : s.items) {
                    sb.append(i.code).append(',')
                            .append(i.order).append(',')
                            .append(i.required).append(',')
                            .append(i.allowNA).append(',')
                            .append(i.severity).append(',')
                            .append(i.hasDetails).append(',')
                            .append(Objects.toString(i.detailCatalog, "null"))
                            .append('|');
                }
            }
            dto.detailCatalogs.forEach((g, list) -> {
                sb.append('#').append(g).append(':');
                for (CatalogItemDTO c : list) sb.append(c.code).append(',');
            });

            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return "nohash";
        }
    }
}
