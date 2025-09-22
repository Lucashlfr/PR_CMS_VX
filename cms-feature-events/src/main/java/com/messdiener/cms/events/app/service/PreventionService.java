// X:\workspace\PR_CMS\cms-feature-events\src\main\java\com\messdiener\cms\events\app\service\PreventionService.java
package com.messdiener.cms.events.app.service;

import com.messdiener.cms.events.domain.entity.PreventionForm;
import com.messdiener.cms.events.persistence.map.PreventionFormMapper;
import com.messdiener.cms.events.persistence.repo.PreventionFormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreventionService {

    private final PreventionFormRepository repo;

    public PreventionForm getPreventionForm(UUID id){
        return repo.findById(id)
                .map(PreventionFormMapper::toDomain)
                .orElseGet(PreventionForm::new);
    }

    public void savePreventionForm(PreventionForm form){
        if (form.getId() == null) form.setId(UUID.randomUUID());
        repo.save(PreventionFormMapper.toEntity(form));
    }
}
