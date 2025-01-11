package com.messdiener.cms.v3.app.services;


import com.messdiener.cms.v3.app.entities.event.OrganisationEvent;
import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.entities.tenant.Tenant;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class OrganisationService {

    private final DatabaseService databaseService;

    public OrganisationService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        try {
            databaseService.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS module_organisation_events (id VARCHAR(255), tenantId VARCHAR(255), type VARCHAR(255), startDate long, endDate long, openEnd bool, description varchar(255), info text, metaData text)").executeUpdate();
            databaseService.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS module_organisation_map (eventId VARCHAR(255), userId VARCHAR(255), response int, schedule int, activity int)").executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveEvent(OrganisationEvent event) throws SQLException {
        databaseService.delete("module_organisation_events", "id", event.getId().toString());

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("INSERT INTO module_organisation_events (id, tenantId, type, startDate, endDate, openEnd, description, info, metaData) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        preparedStatement.setString(1, event.getId().toString());
        preparedStatement.setString(2, event.getTenantId().toString());
        preparedStatement.setString(3, event.getOrganisationType().toString());
        preparedStatement.setLong(4, event.getStartDate().toLong());
        preparedStatement.setLong(5, event.getEndDate().toLong());
        preparedStatement.setBoolean(6, event.isOpenEnd());
        preparedStatement.setString(7, event.getDescription());
        preparedStatement.setString(8, event.getInfo());
        preparedStatement.setString(9, event.getMetaData());
        preparedStatement.executeUpdate();
    }

    private OrganisationEvent getByResultSet(ResultSet resultSet) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));
        UUID tenantId = UUID.fromString(resultSet.getString("tenantId"));
        OrganisationType organisationType = OrganisationType.valueOf(resultSet.getString("type"));

        CMSDate startDate = CMSDate.of(resultSet.getLong("startDate"));
        CMSDate endDate =  CMSDate.of(resultSet.getLong("endDate"));
        boolean openEnd = resultSet.getBoolean("openEnd");

        String description = resultSet.getString("description");
        String info = resultSet.getString("info");
        String metaData = resultSet.getString("metaData");

        return new OrganisationEvent(id, tenantId, organisationType, startDate, endDate, openEnd, description, info, metaData);
    }

    public List<OrganisationEvent> getEvents(UUID tenantId, OrganisationType type) throws SQLException {
        List<OrganisationEvent> list = new ArrayList<>();

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_organisation_events WHERE tenantId = ? and type = ?");
        preparedStatement.setString(1, tenantId.toString());
        preparedStatement.setString(2, type.toString());

        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            OrganisationEvent event = getByResultSet(resultSet);
            list.add(event);
        }
        return list;

    }

    public void setMapState(UUID eventId, UUID userId, int response, int schedule, int activity) throws SQLException {
        String deleteQuery = "DELETE FROM module_organisation_map WHERE eventId = ? AND userId = ?";
        String insertQuery = "INSERT INTO module_organisation_map (eventId, userId, response, schedule, activity) VALUES (?, ?, ?, ?, ?)";

        PreparedStatement deleteStmt = databaseService.getConnection().prepareStatement(deleteQuery);
        PreparedStatement insertStmt  = databaseService.getConnection().prepareStatement(insertQuery );

        // Delete existing entry
        deleteStmt.setString(1, eventId.toString());
        deleteStmt.setString(2, userId.toString());
        deleteStmt.executeUpdate();

        // Insert new entry
        insertStmt.setString(1, eventId.toString());
        insertStmt.setString(2, userId.toString());
        insertStmt.setInt(3, response);
        insertStmt.setInt(4, schedule);
        insertStmt.setInt(5, activity);
        insertStmt.executeUpdate();

    }

    public Optional<OrganisationEvent> getEventById(UUID id) throws SQLException {
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_organisation_events WHERE id = ?");
        preparedStatement.setString(1, id.toString());
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet.next() ? Optional.of(getByResultSet(resultSet)) : Optional.empty();
    }

    public void deleteEvent(UUID id) {
        databaseService.delete("module_organisation_events", "id", id.toString());
        databaseService.delete("module_organisation_map", "eventId", id.toString());
    }

    public List<Person> getScheduledPersons(UUID eventId) throws SQLException {
        List<Person> personList = new ArrayList<>();

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_organisation_map map, module_person person WHERE map.userId = person_id and map.eventId = ? and response = 1 and schedule = 1");
        preparedStatement.setString(1, eventId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            personList.add(Cache.getPersonService().getPersonByResultSet(resultSet));
        }

        return personList;
    }

    public List<Person> getRegisteredPersons(UUID eventId) throws SQLException {
        List<Person> personList = new ArrayList<>();

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_organisation_map map, module_person person WHERE map.userId = person_id and map.eventId = ? and response = 1 and schedule = 0");
        preparedStatement.setString(1, eventId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            personList.add(Cache.getPersonService().getPersonByResultSet(resultSet));
        }

        return personList;
    }


    public List<Person> getPresentPersons(){
        return new ArrayList<>();
    }

    public List<Person> getExcusedPersons(){
        return new ArrayList<>();
    }

    public List<OrganisationEvent> getEvents(UUID tenantId, OrganisationType organisationType, long t) throws SQLException {
        List<OrganisationEvent> list = new ArrayList<>();
        long[] result = DateUtils.getFirstAndLastDayOfMonth(t);

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_organisation_events WHERE tenantId = ? and type = ? AND startDate >= ? AND endDate <= ?");
        preparedStatement.setString(1, tenantId.toString());
        preparedStatement.setString(2, organisationType.toString());
        preparedStatement.setLong(3, result[0]);
        preparedStatement.setLong(4, result[1]);

        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            OrganisationEvent event = getByResultSet(resultSet);
            list.add(event);
        }
        return list;

    }

    public boolean isRegistered(UUID personId, UUID eventId) throws SQLException {
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_organisation_map WHERE userId = ? AND eventId = ? and response = 1 and schedule = 0");
        preparedStatement.setString(1, personId.toString());
        preparedStatement.setString(2, eventId.toString());
        return preparedStatement.executeQuery().next();
    }

    public boolean isScheduled(UUID personId, UUID eventId) throws SQLException {
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_organisation_map WHERE userId = ? AND eventId = ? and response = 1 and schedule = 1");
        preparedStatement.setString(1, personId.toString());
        preparedStatement.setString(2, eventId.toString());
        return preparedStatement.executeQuery().next();
    }


    public List<OrganisationEvent> getNextEvents(UUID tenantId, OrganisationType organisationType) throws SQLException {
        List<OrganisationEvent> list = new ArrayList<>();

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_organisation_events WHERE tenantId = ? and type = ? AND startDate > ?");
        preparedStatement.setString(1, tenantId.toString());
        preparedStatement.setString(2, organisationType.toString());
        preparedStatement.setLong(3, System.currentTimeMillis());

        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            OrganisationEvent event = getByResultSet(resultSet);
            list.add(event);
        }
        return list;
    }

    public List<OrganisationEvent> getNextEvents(UUID tenantId, OrganisationType organisationType, long aLong) throws SQLException {
        List<OrganisationEvent> list = new ArrayList<>();

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_organisation_events WHERE tenantId = ? and type = ? AND startDate > ?");
        preparedStatement.setString(1, tenantId.toString());
        preparedStatement.setString(2, organisationType.toString());
        preparedStatement.setLong(3, aLong);

        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            OrganisationEvent event = getByResultSet(resultSet);
            list.add(event);
        }
        return list;
    }

    public List<OrganisationEvent> getEventsByPerson(UUID personId, OrganisationType organisationType, int response, int schedule) throws SQLException {
        List<OrganisationEvent> list = new ArrayList<>();
        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement("SELECT * FROM module_organisation_events, module_organisation_map WHERE module_organisation_events.id = module_organisation_map.eventId AND userId = ? AND type = ? and response = ? and schedule = ? ");
        preparedStatement.setString(1, personId.toString());
        preparedStatement.setString(2, organisationType.toString());
        preparedStatement.setInt(3, response);
        preparedStatement.setInt(4, schedule);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            OrganisationEvent event = getByResultSet(resultSet);
            list.add(event);
        }
        return list;
    }

    public List<String> getMonthNames(UUID tenantId) throws SQLException {
        Set<String> monthYearSet = new HashSet<>();

        PreparedStatement preparedStatement = databaseService.getConnection().prepareStatement(
                "SELECT * FROM module_organisation_events WHERE tenantId = ? ORDER BY startDate DESC");
        preparedStatement.setString(1, tenantId.toString());
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            monthYearSet.add(CMSDate.of(resultSet.getLong("startDate")).convertTo(DateUtils.DateType.MONTH_NAMES));
        }

        // Sortiere die Monatsnamen in chronologischer Reihenfolge
        List<String> sortedMonthNames = monthYearSet.stream()
                .sorted((m1, m2) -> {
                    // Extrahiere Jahr und Monat für die Sortierung
                    int year1 = Integer.parseInt(m1.split(" ")[1]);
                    int year2 = Integer.parseInt(m2.split(" ")[1]);

                    String month1 = m1.split(" ")[0];
                    String month2 = m2.split(" ")[0];

                    // Vergleiche zuerst die Jahre
                    if (year1 != year2) {
                        return Integer.compare(year1, year2);
                    }

                    // Vergleiche Monate basierend auf ihrer Reihenfolge im Kalender
                    List<String> monthOrder = List.of(
                            "Januar", "Februar", "März", "April", "Mai", "Juni",
                            "Juli", "August", "September", "Oktober", "November", "Dezember"
                    );

                    return Integer.compare(monthOrder.indexOf(month1), monthOrder.indexOf(month2));
                })
                .toList();

        return sortedMonthNames;
    }

    public List<String> getMonthsAfter(UUID tenantId, String startMonthYear) throws SQLException {
        // Extrahiere Jahr und Monat aus dem übergebenen String
        int startYear = Integer.parseInt(startMonthYear.split(" ")[1]);
        String startMonth = startMonthYear.split(" ")[0];

        // Definiere die Reihenfolge der Monate
        List<String> monthOrder = List.of(
                "Januar", "Februar", "März", "April", "Mai", "Juni",
                "Juli", "August", "September", "Oktober", "November", "Dezember"
        );

        // Filtere die Monate basierend auf der Reihenfolge
        return getMonthNames(tenantId).stream()
                .filter(monthYear -> {
                    int year = Integer.parseInt(monthYear.split(" ")[1]);
                    String month = monthYear.split(" ")[0];

                    // Vergleiche das Jahr
                    if (year > startYear) {
                        return true;
                    } else if (year == startYear) {
                        // Vergleiche die Monate basierend auf ihrer Reihenfolge
                        return monthOrder.indexOf(month) > monthOrder.indexOf(startMonth);
                    }
                    return false;
                })
                .toList();
    }



}
