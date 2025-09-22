package com.messdiener.cms.privacy.app.adapters;

import com.messdiener.cms.domain.privacy.PrivacyQueryPort;
import com.messdiener.cms.domain.privacy.PrivacyView;
import com.messdiener.cms.privacy.domain.entity.PrivacyPolicy;
import com.messdiener.cms.privacy.persistence.service.PrivacyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PrivacyQueryAdapter implements PrivacyQueryPort {

    private final PrivacyService privacyService;

    @Override
    public Optional<PrivacyView> getById(UUID personId) {
        return privacyService.getById(personId)
                .map(this::toView);
    }

    @Override
    public List<PrivacyView> getAll() {
        return privacyService.getAll()
                .stream()
                .map(this::toView)
                .toList();
    }

    private PrivacyView toView(PrivacyPolicy p) {
        String title = (nullToEmpty(p.getFirstname()) + " " + nullToEmpty(p.getLastname())).trim();
        String state = countChecks(p) + "/7 bestätigt";
        return new PrivacyView(p.getId().toString(), title, state);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static int countChecks(PrivacyPolicy p) {
        int n = 0;
        if (p.isCheck1()) n++;
        if (p.isCheck2()) n++;
        if (p.isCheck3()) n++;
        if (p.isCheck4()) n++;
        if (p.isCheck5()) n++;
        if (p.isCheck6()) n++;
        if (p.isCheck7()) n++;
        return n;
    }
}
