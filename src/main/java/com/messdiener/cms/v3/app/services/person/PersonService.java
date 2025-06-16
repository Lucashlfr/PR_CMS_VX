package com.messdiener.cms.v3.app.services.person;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.PersonOverviewDTO;
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
    private final PersonFileService personFileService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_person (person_id VARCHAR(255), tenant_id VARCHAR(255), type VARCHAR(255), person_rank VARCHAR(255), principal VARCHAR(255), fRank int, salutation VARCHAR(255), firstname VARCHAR(255), lastname VARCHAR(255), gender VARCHAR(255), birthdate LONG, street VARCHAR(255), houseNumber VARCHAR(255), postalCode VARCHAR(255), city VARCHAR(255), email VARCHAR(255), phone VARCHAR(255), mobile VARCHAR(255), accessionDate LONG, exitDate LONG, activityNote TEXT, notes TEXT, active BOOLEAN, canLogin BOOLEAN, username VARCHAR(255), password VARCHAR(255), iban VARCHAR(255), bic VARCHAR(255), bank VARCHAR(255), accountHolder VARCHAR(255), privacy_policy TEXT, signature LONGBLOB, ob1 BOOLEAN, ob2 BOOLEAN, ob3 BOOLEAN, ob4 BOOLEAN, preventionDate long, customPassword boolean)";
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

        UUID principal = UUID.fromString(resultSet.getString("principal"));
        int fRank = resultSet.getInt("fRank");

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

        boolean customPassword = resultSet.getBoolean("customPassword");
        CMSDate lastUpdate = CMSDate.of(resultSet.getLong("lastUpdate"));

        return new Person(
                id, tenantId, type, rank, principal, fRank, salutation, firstname, lastname, gender, birthdate,
                street, houseNumber, postalCode, city, email, phone, mobile, accessionDate,
                exitDate, activityNote, notes, active, canLogin, username, password,
                iban, bic, bank, accountHolder, customPassword, lastUpdate
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

        String sql = "INSERT INTO module_person (person_id, tenant_id, type, person_rank, principal, fRank, salutation, firstname, lastname, gender, birthdate, street, houseNumber, postalCode, city, email, phone, mobile, accessionDate, exitDate, activityNote, notes, active, canLogin, username, password, iban, bic, bank, accountHolder,customPassword,lastUpdate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, person.getId().toString());
            preparedStatement.setString(2, person.getTenantId().toString());
            preparedStatement.setString(3, person.getType().toString());
            preparedStatement.setString(4, person.getRank().toString());
            preparedStatement.setString(5, person.getPrincipal().toString());
            preparedStatement.setInt(6, person.getFRank());
            preparedStatement.setString(7, person.getSalutation().toString());
            preparedStatement.setString(8, person.getFirstname());
            preparedStatement.setString(9, person.getLastname());
            preparedStatement.setString(10, person.getGender().toString());
            preparedStatement.setLong(11, person.getBirthdate().map(CMSDate::toLong).orElse(0L));
            preparedStatement.setString(12, person.getStreet());
            preparedStatement.setString(13, person.getHouseNumber());
            preparedStatement.setString(14, person.getPostalCode());
            preparedStatement.setString(15, person.getCity());
            preparedStatement.setString(16, person.getEmail());
            preparedStatement.setString(17, person.getPhone());
            preparedStatement.setString(18, person.getMobile());
            preparedStatement.setLong(19, person.getAccessionDate().map(CMSDate::toLong).orElse(0L));
            preparedStatement.setLong(20, person.getExitDate().map(CMSDate::toLong).orElse(0L));
            preparedStatement.setString(21, person.getActivityNote());
            preparedStatement.setString(22, person.getNotes());
            preparedStatement.setBoolean(23, person.isActive());
            preparedStatement.setBoolean(24, person.isCanLogin());
            preparedStatement.setString(25, person.getUsername());
            preparedStatement.setString(26, person.getPassword());
            preparedStatement.setString(27, person.getIban());
            preparedStatement.setString(28, person.getBic());
            preparedStatement.setString(29, person.getBank());
            preparedStatement.setString(30, person.getAccountHolder());
            preparedStatement.setBoolean(31, person.isCustomPassword());
            preparedStatement.setLong(32, System.currentTimeMillis());
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

    public List<Person> getActivePersonsByPermission(int fRank, UUID tenantId) throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql;

        if (fRank >= 3) {
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


    public String getPersonName(UUID id) throws SQLException {
        String sql = "SELECT firstname, lastname FROM module_person WHERE person_id = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, id.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("firstname") + " " + resultSet.getString("lastname") : "null";
            }
        }
    }

    public List<Person> getManagers() throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM module_person WHERE active and fRank >= 2";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    persons.add(getPersonByResultSet(resultSet));
                }
            }
        }
        return persons;
    }

    public int countActivePersonsByTenant(UUID tenantId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM module_person WHERE active AND tenant_id = ?";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        return 0;
    }

    private PersonOverviewDTO mapToOverviewDTO(ResultSet rs) throws SQLException {
        UUID id = UUID.fromString(rs.getString("p.person_id"));
        UUID tenantId = UUID.fromString(rs.getString("p.tenant_id"));
        String firstName = rs.getString("p.firstname");
        String lastName = rs.getString("p.lastname");
        String tenantName = rs.getString("t.name");
        String rank = PersonAttributes.Rank.valueOf(rs.getString("p.person_rank")).getName();
        String birthdate = rs.getObject("birthdate") != null
                ? CMSDate.of(rs.getLong("birthdate")).getGermanDate() + " (" + CMSDate.of(rs.getLong("birthdate")).getAge() + ")"
                : "";

        if(birthdate.startsWith("01.01.1970")) {
            birthdate = "";
        }

        // Aktivitäts-Prozentsätze berechnen (Duty, Absent, Availability)
        double[] activity = new double[3];

        // Bild-URL aus Fileservice
        String imgUrl = ("/dist/assets/img/demo/user-placeholder.svg");

        return new PersonOverviewDTO(
                id,
                firstName,
                lastName,
                tenantName,
                rank,
                birthdate,
                activity,
                imgUrl
        );
    }

    public List<PersonOverviewDTO> getActivePersonsByPermissionDTO(int fRank, UUID tenantId) throws SQLException {
        List<PersonOverviewDTO> persons = new ArrayList<>();
        String sql;

        if (fRank == 3) {
            sql = "SELECT p.person_id, p.firstname, p.lastname, p.person_rank, p.birthdate, p.tenant_id, t.name " +
                    "FROM module_person p JOIN module_tenant t ON p.tenant_id = t.id " +
                    "WHERE p.type = 'MESSDIENER' AND p.active ORDER BY p.lastname";
        } else {
            sql = "SELECT p.person_id, p.firstname, p.lastname, p.person_rank, p.birthdate, p.tenant_id, t.name " +
                    "FROM module_person p JOIN module_tenant t ON p.tenant_id = t.id " +
                    "WHERE p.type = 'MESSDIENER' AND p.active AND p.tenant_id = ? ORDER BY p.lastname";
        }

        try (Connection conn = databaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (fRank != 3) {
                ps.setString(1, tenantId.toString());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    persons.add(mapToOverviewDTO(rs));
                }
            }
        }
        return persons;
    }

    public List<PersonOverviewDTO> getActiveMessdienerByTenantDTO(UUID tenantId) throws SQLException {
        List<PersonOverviewDTO> persons = new ArrayList<>();
        String sql =
                "SELECT p.person_id, p.firstname, p.lastname, p.person_rank, p.birthdate, p.tenant_id, t.name " +
                        "FROM module_person p JOIN module_tenant t ON p.tenant_id = t.id " +
                        "WHERE p.type = 'MESSDIENER' AND p.active AND p.tenant_id = ? ORDER BY p.lastname";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenantId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    persons.add(mapToOverviewDTO(rs));
                }
            }
        }
        return persons;
    }

    public List<PersonOverviewDTO> findAllByIds(List<UUID> personIds) throws SQLException {
        // Falls keine IDs übergeben wurden, direkt leere Liste zurückliefern
        if (personIds == null || personIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<PersonOverviewDTO> persons = new ArrayList<>();

        // SQL zusammenbauen: IN-Klausel mit so vielen Platzhaltern, wie UUIDs im List-Parameter sind
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.person_id, p.firstname, p.lastname, p.person_rank, p.birthdate, p.tenant_id, t.name ")
                .append("FROM module_person p JOIN module_tenant t ON p.tenant_id = t.id ")
                .append("WHERE p.person_id IN (");
        for (int i = 0; i < personIds.size(); i++) {
            sql.append("?");
            if (i < personIds.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(") ORDER BY p.lastname");

        try (Connection conn = databaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // Die UUID-Strings in die PreparedStatement-Parameter einfügen
            for (int i = 0; i < personIds.size(); i++) {
                ps.setString(i + 1, personIds.get(i).toString());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    persons.add(mapToOverviewDTO(rs));
                }
            }
        }

        return persons;
    }

    public Optional<PersonOverviewDTO> getPersonDTO(Person targetPerson) throws SQLException {
        String sql =
                "SELECT p.person_id, p.firstname, p.lastname, p.person_rank, p.birthdate, p.tenant_id, t.name " +
                        "FROM module_person p JOIN module_tenant t ON p.tenant_id = t.id " +
                        "WHERE p.type = 'MESSDIENER' AND p.active AND p.person_id = ? ORDER BY p.lastname";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, targetPerson.getId().toString());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapToOverviewDTO(rs)) : Optional.empty();
            }
        }
    }

    public Optional<PersonOverviewDTO> getPersonDTO(UUID personId) throws SQLException {
        String sql =
                "SELECT p.person_id, p.firstname, p.lastname, p.person_rank, p.birthdate, p.tenant_id, t.name " +
                        "FROM module_person p JOIN module_tenant t ON p.tenant_id = t.id " +
                        "WHERE p.type = 'MESSDIENER' AND p.active AND p.person_id = ? ORDER BY p.lastname";

        try (Connection conn = databaseService.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, personId.toString());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapToOverviewDTO(rs)) : Optional.empty();
            }
        }
    }
}
