package com.messdiener.cms.v3.app.services;


import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.person.data.connection.PersonConnection;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.security.SecurityHelper;
import com.messdiener.cms.v3.shared.enums.PersonAttributes;
import com.messdiener.cms.v3.utils.other.Pair;
import com.messdiener.cms.v3.utils.time.CMSDate;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PersonService {

    private final DatabaseService databaseService;

    public PersonService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        try {
            databaseService.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS module_person (person_id VARCHAR(255), tenant_id VARCHAR(255), type VARCHAR(255), person_rank VARCHAR(255), salutation VARCHAR(255), firstname VARCHAR(255), lastname VARCHAR(255), gender VARCHAR(255), birthdate long, street VARCHAR(255), houseNumber VARCHAR(255), postalCode VARCHAR(255), city VARCHAR(255), email VARCHAR(255), phone VARCHAR(255), mobile VARCHAR(255), accessionDate long, exitDate long, activityNote text, notes text, active boolean, canLogin boolean, username VARCHAR(255), password VARCHAR(255), iban VARCHAR(255), bic VARCHAR(255), bank VARCHAR(255), accountHolder VARCHAR(255), privacy_policy TEXT, signature LONGBLOB)").executeUpdate();
            databaseService.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS module_person_connection (connection_id VARCHAR(255), host VARCHAR(255), sub VARCHAR(255), type VARCHAR(255))").executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Person createPerson(UUID tenantId) throws SQLException {
        Person person = Person.empty(tenantId);
        updatePerson(person);

        return getPersonById(person.getId()).orElseThrow();
    }

    public void updatePerson(Person person) throws SQLException {

        databaseService.delete("module_person", "person_id", person.getId().toString());

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement(
                "INSERT INTO module_person (person_id, tenant_id, type, person_rank, salutation, firstname, lastname, gender, birthdate, street, houseNumber, postalCode, city, email, phone, mobile, accessionDate, exitDate, activityNote, notes, active, canLogin, username, password, iban, bic, bank, accountHolder, privacy_policy, signature) VALUES (?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?);");

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
        preparedStatement.setString(29, person.getPrivacy_policy());
        preparedStatement.setString(30, person.getSignature());
        preparedStatement.executeUpdate();

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

        Optional<CMSDate>  accessionDate = resultSet.getObject("accessionDate") != null ? CMSDate.generateOptional(resultSet.getLong("accessionDate")) : Optional.empty();
        Optional<CMSDate>  exitDate = resultSet.getObject("exitDate") != null ? CMSDate.generateOptional(resultSet.getLong("exitDate")) : Optional.empty();
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

        String privacy_policy = resultSet.getString("privacy_policy");
        String signature = resultSet.getString("signature");

        return new Person(
                id, tenantId, type, rank, salutation, firstname, lastname, gender, birthdate,
                street, houseNumber, postalCode, city, email, phone, mobile, accessionDate,
                exitDate, activityNote, notes, active, canLogin, username, password, iban, bic, bank, accountHolder,
                privacy_policy, signature
        );
    }


    public List<Person> getPersons() throws SQLException {
        List<Person> persons = new ArrayList<>();
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_person");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            persons.add(getPersonByResultSet(resultSet));
        }
        return persons;
    }

    public List<Person> getActiveMessdienerByTenant(UUID id) throws SQLException {
        List<Person> persons = new ArrayList<>();
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_person WHERE tenant_id = ? and type = 'MESSDIENER' and active ORDER BY lastname");
        preparedStatement.setString(1, id.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            persons.add(getPersonByResultSet(resultSet));
        }
        return persons;
    }

    public List<Person> getInactiveMessdienerByTenant(UUID id) throws SQLException {
        List<Person> persons = new ArrayList<>();
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_person WHERE tenant_id = ? and type = 'MESSDIENER' and active = 0 ORDER BY lastname");
        preparedStatement.setString(1, id.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            persons.add(getPersonByResultSet(resultSet));
        }
        return persons;
    }


    public List<Person> getPersonsByTenant(UUID tenantId) throws SQLException {
        List<Person> persons = new ArrayList<>();
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_person WHERE tenant_id = ? ORDER BY lastname");
        preparedStatement.setString(1, tenantId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            persons.add(getPersonByResultSet(resultSet));
        }
        return persons;
    }


    public List<Person> getPersonsByTenantAndType(UUID tenantId, PersonAttributes.Type type) throws SQLException {
        List<Person> persons = new ArrayList<>();
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_person WHERE tenant_id = ? and type = ?");
        preparedStatement.setString(1, tenantId.toString());
        preparedStatement.setString(2, type.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            persons.add(getPersonByResultSet(resultSet));
        }
        return persons;
    }


    public List<Person> getPersonsByLogin() throws SQLException {
        List<Person> persons = new ArrayList<>();
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_person WHERE canLogin = 1 and active = 1");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            persons.add(getPersonByResultSet(resultSet));
        }
        return persons;
    }


    public Optional<Person> getPersonById(UUID personId) throws SQLException {
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_person WHERE person_id = ?");
        preparedStatement.setString(1, personId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next() ? Optional.of(getPersonByResultSet(resultSet)) : Optional.empty();
    }

    public Optional<Person> getPersonByUsername(String username) throws SQLException {
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_person WHERE username = ?");
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next() ? Optional.of(getPersonByResultSet(resultSet)) : Optional.empty();
    }

    public Set<Pair<String, File>> listFilesUsingJavaIO(UUID id) {

        if (!new File("." + File.separator + "cms_v2" + File.separator + "messdienerDaten" + File.separator + id).exists())
            return new HashSet<>();

        return Stream.of((new File("." + File.separator + "cms_v2" + File.separator + "messdienerDaten" + File.separator + id).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(f -> new Pair<>(f.getName(), f))
                .collect(Collectors.toSet());
    }

    public Optional<File> getFile(UUID id, String filename) {
        if (!new File("." + File.separator + "cms_v2" + File.separator + "messdienerDaten" + File.separator + id).exists())
            return Optional.empty();

        return Stream.of((new File("." + File.separator + "cms_v2" + File.separator + "messdienerDaten" + File.separator + id).listFiles()))
                .filter(file -> file.getName().equals(filename))
                .findFirst();
    }

    public Set<Pair<String, File>> listFilesUsingJavaIO(UUID id, int i) {
        return listFilesUsingJavaIO(id).stream().limit(i).collect(Collectors.toSet());
    }


    public void matchPersonToUser() throws SQLException {

        for (Person person : getPersons()) {
            //User user = Cache.getUserService().getUserById(person.getId()).orElse(new User(person.getId(), messdiener.getFirstname() + "." + messdiener.getLastname(), messdiener.getGermanDate(), new ArrayList<>(), UserConfigurations.empty()));
            //user.save();
        }
    }

    public Optional<Person> getPersonByName(String teamerName) {
        return Optional.empty();
    }

    public void createConnection(PersonConnection connection) throws SQLException {
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("INSERT INTO module_person_connection (connection_id, host, sub, type) VALUES (?,?,?,?)");
        preparedStatement.setString(1, connection.getId().toString());
        preparedStatement.setString(2, connection.getHost().toString());
        preparedStatement.setString(3, connection.getSub().toString());
        preparedStatement.setString(4, connection.getConnectionType().toString());
        preparedStatement.executeUpdate();
    }

    public List<PersonConnection> getConnectionsByHost(UUID host) throws SQLException {
        List<PersonConnection> connections = new ArrayList<>();
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_person_connection where  host = ?");
        preparedStatement.setString(1, host.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            UUID id = UUID.fromString(resultSet.getString("connection_id"));
            UUID sub = UUID.fromString(resultSet.getString("sub"));
            PersonAttributes.Connection connectionType = PersonAttributes.Connection.valueOf(resultSet.getString("type"));
            connections.add(new PersonConnection(id, host, sub, connectionType));
        }
        return connections;
    }

    public void updateUsers() throws SQLException {
        for (Person person : getPersonsByLogin()) {
            createLogin(person);

            if (person.getType() == null) {
                person.setType(PersonAttributes.Type.NULL);
            }

            updatePerson(person);
        }
    }

    public void createLogin(Person person) {
        if (!person.isCanLogin()) return;
        if (person.getUsername() == null || person.getUsername().isEmpty())
            person.setUsername(SecurityHelper.generateUsername(person.getFirstname(), person.getLastname()));
        if (person.getPassword() == null || person.getPassword().isEmpty()) {
            if (person.getBirthdate().isPresent()) {
                person.setPassword(person.getBirthdate().get().getGermanDate());
            } else {
                person.setPassword(SecurityHelper.generatePassword());
            }
        }
    }

}
