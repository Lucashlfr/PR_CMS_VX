package com.messdiener.cms.v3.app.services.person;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import com.messdiener.cms.v3.utils.time.CMSDate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);
    private final DatabaseService databaseService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_person (person_id VARCHAR(255), tenant_id VARCHAR(255), type VARCHAR(255), person_rank VARCHAR(255), salutation VARCHAR(255), firstname VARCHAR(255), lastname VARCHAR(255), gender VARCHAR(255), birthdate LONG, street VARCHAR(255), houseNumber VARCHAR(255), postalCode VARCHAR(255), city VARCHAR(255), email VARCHAR(255), phone VARCHAR(255), mobile VARCHAR(255), accessionDate LONG, exitDate LONG, activityNote TEXT, notes TEXT, active BOOLEAN, canLogin BOOLEAN, username VARCHAR(255), password VARCHAR(255), iban VARCHAR(255), bic VARCHAR(255), bank VARCHAR(255), accountHolder VARCHAR(255), privacy_policy TEXT, signature LONGBLOB, ob1 BOOLEAN, ob2 BOOLEAN, ob3 BOOLEAN, ob4 BOOLEAN)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("PersonService initialized and person table ensured.");

            Person system = Person.empty(Cache.SYSTEM_TENANT);
            system.setId(Cache.SYSTEM_USER);
            system.setActive(true);
            system.setFirstname("System");
            updatePerson(system);
        } catch (SQLException e) {
            LOGGER.error("Error initializing PersonService", e);
        }
    }

    public Person getPersonByResultSet(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("person_id"));
        UUID tenantId = UUID.fromString(resultSet.getString("tenant_id"));

        PersonAttributes.Type type = resultSet.getString("type") != null ? PersonAttributes.Type.valueOf(resultSet.getString("type")) : PersonAttributes.Type.NULL;
        PersonAttributes.Rank rank = resultSet.getString("person_rank") != null ? PersonAttributes.Rank.valueOf(resultSet.getString("person_rank")) : PersonAttributes.Rank.NULL;

        PersonAttributes.Salutation salutation = resultSet.getString("salutation") != null ? PersonAttributes.Salutation.valueOf(resultSet.getString("salutation")) : PersonAttributes.Salutation.NULL;
        String firstname = resultSet.getString("firstname");
        String lastname = resultSet.getString("lastname");

        PersonAttributes.Gender gender = resultSet.getString("gender") != null ? PersonAttributes.Gender.valueOf(resultSet.getString("gender")) : PersonAttributes.Gender.NOT_SPECIFIED;
        Optional<CMSDate> birthdate = resultSet.getObject("birthdate") != null ? CMSDate.generateOptional(resultSet.getLong("birthdate")) : Optional.empty();

        String street = resultSet.getString("street");
        String houseNumber = resultSet.getString("houseNumber");
        String postalCode = resultSet.getString("postalCode");
        String city = resultSet.getString("city");

        String email = resultSet.getString("email");
        String phone = resultSet.getString("phone");
        String mobile = resultSet.getString("mobile");

        Optional<CMSDate> accessionDate = resultSet.getObject("accessionDate") != null ? CMSDate.generateOptional(resultSet.getLong("accessionDate")) : Optional.empty();
        Optional<CMSDate> exitDate = resultSet.getObject("exitDate") != null ? CMSDate.generateOptional(resultSet.getLong("exitDate")) : Optional.empty();
        String activityNote = resultSet.getString("activityNote");

        String notes = resultSet.getString("notes");

        boolean active = resultSet.getBoolean("active");

        boolean canLogin = resultSet.getBoolean("canLogin");
        String username = resultSet.getString("username");
        String password = resultSet.getString("password");

        String iban = resultSet.getString("iban");
        String bic = resultSet.getString("bic");
        String bank = resultSet.getString("bank");
        String accountHolder = resultSet.getString("accountHolder");

        String privacyPolicy = resultSet.getString("privacy_policy");
        String signature = resultSet.getString("signature");

        boolean ob1 = resultSet.getBoolean("ob1");
        boolean ob2 = resultSet.getBoolean("ob2");
        boolean ob3 = resultSet.getBoolean("ob3");
        boolean ob4 = resultSet.getBoolean("ob4");

        return new Person(
                id, tenantId, type, rank, salutation, firstname, lastname, gender, birthdate,
                street, houseNumber, postalCode, city, email, phone, mobile, accessionDate,
                exitDate, activityNote, notes, active, canLogin, username, password,
                iban, bic, bank, accountHolder, privacyPolicy, signature, ob1, ob2, ob3, ob4
        );
    }

    public Person createPerson(UUID tenantId) throws SQLException {
        Person person = Person.empty(tenantId);
        updatePerson(person);
        return getPersonById(person.getId()).orElseThrow();
    }

    public List<Person> getPersons() throws SQLException {
        List<Person> persons = new ArrayList<>();
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM module_person");
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                persons.add(getPersonByResultSet(resultSet));
            }
        }
        return persons;
    }


    public void updatePerson(Person person) throws SQLException {
        databaseService.delete("module_person", "person_id", person.getId().toString());

        String sql = "INSERT INTO module_person (person_id, tenant_id, type, person_rank, salutation, firstname, lastname, gender, birthdate, street, houseNumber, postalCode, city, email, phone, mobile, accessionDate, exitDate, activityNote, notes, active, canLogin, username, password, iban, bic, bank, accountHolder, privacy_policy, signature, ob1, ob2, ob3, ob4) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, person.getId().toString());
            preparedStatement.setString(2, person.getTenantId().toString());
            preparedStatement.setString(3, person.getType().toString());
            preparedStatement.setString(4, person.getRank().toString());
            preparedStatement.setString(5, person.getSalutation().toString());
            preparedStatement.setString(6, person.getFirstname());
            preparedStatement.setString(7, person.getLastname());
            preparedStatement.setString(8, person.getGender().toString());
            preparedStatement.setLong(9, person.getBirthdate().map(CMSDate::toLong).orElse(0L));
            preparedStatement.setString(10, person.getStreet());
            preparedStatement.setString(11, person.getHouseNumber());
            preparedStatement.setString(12, person.getPostalCode());
            preparedStatement.setString(13, person.getCity());
            preparedStatement.setString(14, person.getEmail());
            preparedStatement.setString(15, person.getPhone());
            preparedStatement.setString(16, person.getMobile());
            preparedStatement.setLong(17, person.getAccessionDate().map(CMSDate::toLong).orElse(0L));
            preparedStatement.setLong(18, person.getExitDate().map(CMSDate::toLong).orElse(0L));
            preparedStatement.setString(19, person.getActivityNote());
            preparedStatement.setString(20, person.getNotes());
            preparedStatement.setBoolean(21, person.isActive());
            preparedStatement.setBoolean(22, person.isCanLogin());
            preparedStatement.setString(23, person.getUsername());
            preparedStatement.setString(24, person.getPassword());
            preparedStatement.setString(25, person.getIban());
            preparedStatement.setString(26, person.getBic());
            preparedStatement.setString(27, person.getBank());
            preparedStatement.setString(28, person.getAccountHolder());
            preparedStatement.setString(29, person.getPrivacyPolicy());
            preparedStatement.setString(30, person.getSignature());
            preparedStatement.setBoolean(31, person.isOb1());
            preparedStatement.setBoolean(32, person.isOb2());
            preparedStatement.setBoolean(33, person.isOb3());
            preparedStatement.setBoolean(34, person.isOb4());
            preparedStatement.executeUpdate();
        }
    }

    public Optional<Person> getPersonById(UUID personId) throws SQLException {
        String sql = "SELECT * FROM module_person WHERE person_id = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, personId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getPersonByResultSet(resultSet)) : Optional.empty();
            }
        }
    }

    public Optional<Person> getPersonByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM module_person WHERE username = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? Optional.of(getPersonByResultSet(resultSet)) : Optional.empty();
            }
        }
    }

    public List<Person> getPersonsByTenant(UUID tenantId) throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM module_person WHERE tenant_id = ? ORDER BY lastname";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    persons.add(getPersonByResultSet(resultSet));
                }
            }
        }
        return persons;
    }

    public List<Person> getPersonsByTenantAndType(UUID tenantId, PersonAttributes.Type type) throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM module_person WHERE tenant_id = ? AND type = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            preparedStatement.setString(2, type.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    persons.add(getPersonByResultSet(resultSet));
                }
            }
        }
        return persons;
    }

    public List<Person> getPersonsByLogin() throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM module_person WHERE canLogin = 1 AND active = 1";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                persons.add(getPersonByResultSet(resultSet));
            }
        }
        return persons;
    }

    public List<Person> getActiveMessdienerByTenant(UUID tenantId) throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM module_person WHERE tenant_id = ? AND type = 'MESSDIENER' AND active ORDER BY lastname";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    persons.add(getPersonByResultSet(resultSet));
                }
            }
        }
        return persons;
    }

    public List<Person> getInactiveMessdienerByTenant(UUID tenantId) throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM module_person WHERE tenant_id = ? AND type = 'MESSDIENER' AND active = 0 ORDER BY lastname";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    persons.add(getPersonByResultSet(resultSet));
                }
            }
        }
        return persons;
    }

    public List<Person> getPersonsByRank() throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM module_person WHERE (person_rank = ? OR person_rank = ?) AND active ORDER BY lastname";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, PersonAttributes.Rank.LEITUNGSTEAM.toString());
            preparedStatement.setString(2, PersonAttributes.Rank.OBERMESSDIENER.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    persons.add(getPersonByResultSet(resultSet));
                }
            }
        }
        return persons;
    }

    public List<Person> getActivePersonsByPermission(UUID personId, String permission, UUID tenantId) throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql;


        //TODO: FIX
        if (permission.equals("ORG")) {
            sql = "SELECT * FROM module_person WHERE type = 'MESSDIENER' AND active ORDER BY lastname";
            try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    persons.add(getPersonByResultSet(resultSet));
                }
            }
        } else {
            sql = "SELECT * FROM module_person WHERE tenant_id = ? AND type = 'MESSDIENER' AND active ORDER BY lastname";
            try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, tenantId.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        persons.add(getPersonByResultSet(resultSet));
                    }
                }
            }
        }

        return persons;
    }



}
