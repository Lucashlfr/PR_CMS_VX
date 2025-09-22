// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\app\service\EventApplicationService.java
package com.messdiener.cms.events.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messdiener.cms.events.persistence.entity.EventResultEntity;
import com.messdiener.cms.events.persistence.repo.EventComponentRepository;
import com.messdiener.cms.events.persistence.repo.EventResultRepository;
import com.messdiener.cms.shared.ui.Component;
import com.messdiener.cms.events.persistence.map.EventComponentMapper;
import com.messdiener.cms.utils.time.CMSDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventApplicationService {

    private final EventComponentRepository componentRepo;
    private final EventResultRepository resultRepo;

    public void deleteComponent(UUID eventId, int number) {
        componentRepo.deleteByEventIdAndNumber(eventId, number);
    }

    public void saveComponent(UUID eventId, Component component) {
        deleteComponent(eventId, component.getNumber());
        componentRepo.save(EventComponentMapper.toEntity(eventId, component));
    }

    public List<Component> getComponents(UUID eventId){
        return componentRepo.findByEventIdOrderByNumberAsc(eventId)
                .stream().map(EventComponentMapper::toDomain).collect(Collectors.toList());
    }

    public void saveResult(UUID resultId, UUID eventId, String json, String userId) {
        EventResultEntity entity = EventResultEntity.builder()
                .resultId(resultId)
                .eventId(eventId)
                .userId(parseUuid(userId))
                .date(System.currentTimeMillis())
                .json(json)
                .build();
        resultRepo.save(entity);
    }

    public void updateUserIdForResult(UUID resultId, String userId){
        resultRepo.findById(resultId).ifPresent(r -> {
            r.setUserId(parseUuid(userId));
            r.setDate(System.currentTimeMillis());
            resultRepo.save(r);
        });
    }

    /**
     * Export wie bisher: Header ("ResultId", "Datum", "Verknüpfte Person", <Labels...>)
     * und danach je ResultId eine Zeile mit den Werte-Mappings aus dem JSON.
     */
    public List<String[]> exportEventResults(UUID eventId) {
        List<String[]> rows = new ArrayList<>();

        // 1) Komponenten lesen -> Header bauen (Name/Label-Reihenfolge = number ASC)
        List<Component> components = getComponents(eventId); // SELECT ... ORDER BY number ASC :contentReference[oaicite:3]{index=3}
        List<String> header = new ArrayList<>();
        header.add("ResultId");
        header.add("Datum");
        header.add("Verknüpfte Person");
        List<String> componentNames = new ArrayList<>();
        for (Component c : components) {
            componentNames.add(c.getName());
            header.add(c.getLabel());
        }
        rows.add(header.toArray(new String[0]));

        // 2) Ergebnisse lesen (ORDER BY date DESC ist für Export oft ok; Altcode ohne ORDER) :contentReference[oaicite:4]{index=4}
        List<EventResultEntity> results = resultRepo.findByEventIdOrderByDateDesc(eventId);

        ObjectMapper om = new ObjectMapper();
        for (EventResultEntity r : results) {
            Map<String,Object> dataMap;
            try {
                dataMap = om.readValue(Optional.ofNullable(r.getJson()).orElse("{}"), new TypeReference<Map<String,Object>>() {});
            } catch (Exception ex) {
                dataMap = Collections.emptyMap();
            }

            String[] row = new String[3 + componentNames.size()];
            row[0] = r.getResultId().toString();
            row[1] = CMSDate.of(Optional.ofNullable(r.getDate()).orElse(0L)).getGermanTime();
            row[2] = r.getUserId() != null ? r.getUserId().toString() : "#";

            for (int i = 0; i < componentNames.size(); i++) {
                Object v = dataMap.get(componentNames.get(i));
                row[3 + i] = v == null ? "" : String.valueOf(v);
            }
            rows.add(row);
        }

        return rows;
    }

    private static UUID parseUuid(String s){
        try { return UUID.fromString(s); } catch (Exception e){ return null; }
    }
}
