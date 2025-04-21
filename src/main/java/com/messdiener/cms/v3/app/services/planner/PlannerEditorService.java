package com.messdiener.cms.v3.app.services.planner;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.app.services.person.PersonService;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PlannerEditorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlannerEditorService.class);
    private final DatabaseService databaseService;
    private final PersonService personService;

    @PostConstruct
    public void init() {
        try {
            executeDDL("CREATE TABLE IF NOT EXISTS module_planer_principal (planerId VARCHAR(255), userId VARCHAR(255))");
            executeDDL("CREATE TABLE IF NOT EXISTS module_planer_map (planerId VARCHAR(255), userId VARCHAR(255), date LONG)");
            executeDDL("CREATE TABLE IF NOT EXISTS module_planer_sub_event (subId VARCHAR(255), planerId VARCHAR(255), lastModified LONG, personId VARCHAR(255), title TEXT, html LONGTEXT)");
            executeDDL("CREATE TABLE IF NOT EXISTS module_planer_tasks (taskId VARCHAR(255), planerId VARCHAR(255), id INT AUTO_INCREMENT PRIMARY KEY, taskName TEXT, taskDescription TEXT, taskState VARCHAR(255), priority VARCHAR(255), created LONG, updated LONG, lable VARCHAR(255))");
            executeDDL("CREATE TABLE IF NOT EXISTS module_planer_editors (taskId VARCHAR(255), planerId VARCHAR(255), personId VARCHAR(255))");
            LOGGER.info("PlannerEditorService initialized and required tables ensured.");
        } catch (SQLException e) {
            LOGGER.error("Failed to initialize PlannerEditorService", e);
        }
    }

    private void executeDDL(String sql) throws SQLException {
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
        }
    }

    public void addEditor(UUID planerId, UUID taskId, UUID personId) throws SQLException {
        String checkSql = "SELECT * FROM module_planer_editors WHERE taskId = ? AND planerId = ? AND personId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(checkSql)){
            preparedStatement.setString(1, taskId.toString());
            preparedStatement.setString(2, planerId.toString());
            preparedStatement.setString(3, personId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    LOGGER.info("Editor already exists for task '{}', planer '{}', person '{}'", taskId, planerId, personId);
                    return;
                }
            }
        }

        String insertSql = "INSERT INTO module_planer_editors (taskId, planerId, personId) VALUES (?, ?, ?)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
            preparedStatement.setString(1, taskId.toString());
            preparedStatement.setString(2, planerId.toString());
            preparedStatement.setString(3, personId.toString());
            preparedStatement.executeUpdate();
            LOGGER.info("Editor added for task '{}', planer '{}', person '{}'", taskId, planerId, personId);
        }
    }

    public List<Person> getEditors(UUID taskId) throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT * FROM module_planer_editors JOIN module_person ON module_planer_editors.personId = module_person.person_id WHERE taskId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, taskId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    persons.add(personService.getPersonByResultSet(resultSet));
                }
            }
        }
        return persons;
    }

    public List<Person> getEditorsByPlaner(UUID planerId) throws SQLException {
        List<Person> persons = new ArrayList<>();
        String sql = "SELECT DISTINCT module_planer_editors.personId, module_person.* FROM module_planer_editors JOIN module_person ON module_planer_editors.personId = module_person.person_id WHERE module_planer_editors.planerId = ?";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, planerId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    persons.add(personService.getPersonByResultSet(resultSet));
                }
            }
        }
        return persons;
    }
}
