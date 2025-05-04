package com.messdiener.cms.v3.shared.scheduler;

import com.messdiener.cms.v3.app.entities.audit.ComplianceCheck;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.audit.ComplianceService;
import com.messdiener.cms.v3.app.services.person.PersonLoginService;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.shared.enums.ComplianceType;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GlobalManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalManager.class);

    private final PersonService personService;
    private final PersonLoginService personLoginService;

    private final UUID systemUserId = UUID.fromString("93dacda6-b951-413a-96dc-9a37858abe3e");
    private final ComplianceService complianceService;


    @PostConstruct
    public void logInit() {
        LOGGER.info("GlobalManager initialized.");
    }

    public void startUp() throws SQLException {
        personLoginService.matchPersonToUser(); // <- hier aufrufen

        for(Person person : personService.getPersons()){
            if((person.getRank() == PersonAttributes.Rank.LEITUNGSTEAM || person.getRank() == PersonAttributes.Rank.OBERMESSDIENER)){
                if(!person.isOb4()) complianceService.saveComplianceCheck(ComplianceCheck.of(ComplianceType.SAE, person.getId(), true));
                if(person.isOb4()) complianceService.update(ComplianceCheck.of(ComplianceType.SAE, person.getId(), false));

                if(person.getPreventionDate().toLong() == 0) complianceService.saveComplianceCheck(ComplianceCheck.of(ComplianceType.PREVENTION_TRAINING, person.getId(), true));
                if(person.getPreventionDate().toLong() > 0) complianceService.update(ComplianceCheck.of(ComplianceType.PREVENTION_TRAINING, person.getId(), false));
            }

            if(!person.isOb2()) complianceService.saveComplianceCheck(ComplianceCheck.of(ComplianceType.PRIVACY, person.getId(), true));
            if(person.isOb2()) complianceService.update(ComplianceCheck.of(ComplianceType.PRIVACY, person.getId(), false));

            if(!person.isOb3()) complianceService.saveComplianceCheck(ComplianceCheck.of(ComplianceType.CONTACT, person.getId(), true));
            if(person.isOb3()) complianceService.update(ComplianceCheck.of(ComplianceType.CONTACT, person.getId(), false));

            if(!person.isOb1()) complianceService.saveComplianceCheck(ComplianceCheck.of(ComplianceType.DATA, person.getId(), true));
            if(person.isOb1()) complianceService.update(ComplianceCheck.of(ComplianceType.DATA, person.getId(), false));

        }

    }

}
