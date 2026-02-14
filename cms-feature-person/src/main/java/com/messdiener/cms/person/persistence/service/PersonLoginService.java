package com.messdiener.cms.person.persistence.service;

import com.messdiener.cms.person.domain.entity.Person;
import com.messdiener.cms.shared.enums.PersonAttributes;
import com.messdiener.cms.utils.other.CharacterConverter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PersonLoginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonLoginService.class);

    @PostConstruct
    public void init() {
        LOGGER.info("PersonLoginService initialized.");
    }

    public void updateUsers(List<Person> persons) throws SQLException {
        for (Person person : persons) {
            createLogin(person);

            if (person.getType() == null) {
                person.setType(PersonAttributes.Type.NULL);
            }
        }
    }

    public void createLogin(Person person) throws SQLException {
        if (!person.isCanLogin()) return;

        if (isBlank(person.getUsername())) {
            String username = generateUsername(person.getFirstName(), person.getLastName());
            person.setUsername(username);
            LOGGER.info("Generated username '{}' for '{} {}'", username, person.getFirstName(), person.getLastName());
        }

        if (isBlank(person.getPassword())) {
            if (person.getBirthdate().isPresent()) {
                String birthdatePassword = person.getBirthdate().get().getGermanDate();
                person.setPassword(birthdatePassword);
                LOGGER.info("Password set from birthdate for '{} {}'", person.getFirstName(), person.getLastName());
            } else {
                String randomPassword = generatePassword();
                person.setPassword(randomPassword);
                LOGGER.info("Generated random password for '{} {}'", person.getFirstName(), person.getLastName());
            }
        }
    }

    public void matchPersonToUser() throws SQLException {
        // Vorbereitung für spätere Implementierung
        // z. B. Matching zwischen Person und User-Tabelle (via UserService)
    }

    public String generatePassword() {
        int length = 8;
        String[] charCategories = {
                "abcdefghijklmnopqrstuvwxyz",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                "0123456789",
                "-_@&/!"
        };

        StringBuilder password = new StringBuilder(length);
        Random random = new Random(System.nanoTime());

        for (int i = 0; i < length; i++) {
            String chars = charCategories[random.nextInt(charCategories.length)];
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    public String generateUsername(String firstname, String lastname) {
        return CharacterConverter.convert(firstname) + "." + CharacterConverter.convert(lastname);
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}
