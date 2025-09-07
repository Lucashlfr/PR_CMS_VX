package com.messdiener.cms.v3.app.services.person;

import com.messdiener.cms.v3.app.entities.person.data.flags.PersonFlag;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.enums.person.FlagType;
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
public class PersonFlagService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonFlagService.class);
    private final DatabaseService databaseService;
    private final PersonFileService personFileService;

    @PostConstruct
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS module_person_flag (flagId VARCHAR(36), person_id VARCHAR(36), number INT AUTO_INCREMENT PRIMARY KEY, flagType VARCHAR(255), flagDetails TEXT, additionalInformation TEXT,flagDate long, complained boolean)";
        try (Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            LOGGER.info("module_person_flag initialized and person table ensured.");
        } catch (SQLException e) {
            LOGGER.error("Error initializing PersonService", e);
        }
    }

   private PersonFlag getPersonByResultSet(ResultSet resultSet) throws SQLException {
       UUID id = UUID.fromString(resultSet.getString("flagId"));
       int number =  resultSet.getInt("number");
       FlagType flagType =  FlagType.valueOf(resultSet.getString("flagType"));
       String flagDetails =  resultSet.getString("flagDetails");
       String additionalInformation =   resultSet.getString("additionalInformation");
       CMSDate flagDate = CMSDate.of(resultSet.getLong("flagDate"));
       boolean complained =  resultSet.getBoolean("complained");
       return new PersonFlag(id, number, flagType, flagDetails, additionalInformation, flagDate, complained);
   }

   public List<PersonFlag> getAllFlagsByPerson(UUID personId) throws SQLException {
        List<PersonFlag> personFlags = new ArrayList<>();
        String sql = "SELECT * FROM module_person_flag WHERE person_id = ?";

        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, personId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    personFlags.add(getPersonByResultSet(resultSet));
                }
            }
        }
        return personFlags;
   }

   private boolean flagExists(UUID personId, FlagType type) throws SQLException {
       String sql = "SELECT number FROM module_person_flag WHERE person_id = ? AND flagType = ?";
       try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
           preparedStatement.setString(1, personId.toString());
           preparedStatement.setString(2, type.toString());
           try (ResultSet resultSet = preparedStatement.executeQuery()) {
               return resultSet.next();
           }
       }
   }

   public void createDefaultFlags(UUID personId) throws SQLException {
        for(FlagType flagType : FlagType.values()) {
            if(!flagType.isDefaultCreated())continue;
            if(flagExists(personId, flagType))continue;
            saveFlag(personId, new PersonFlag(UUID.randomUUID(), 0, flagType, "/", "/", CMSDate.current(), false));
        }
   }

   public void saveFlag(UUID personId, PersonFlag flag) throws SQLException {
        databaseService.delete("module_person_flag", "flagId", flag.getId().toString());

        String sql = "INSERT INTO module_person_flag (flagId, person_id, flagType, flagDetails, additionalInformation, flagDate, complained) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, flag.getId().toString());
            preparedStatement.setString(2, personId.toString());
            preparedStatement.setString(3, flag.getFlagType().toString());
            preparedStatement.setString(4, flag.getFlagDetails());
            preparedStatement.setString(5, flag.getAdditionalInformation());
            preparedStatement.setLong(6, flag.getFlagDate().toLong());
            preparedStatement.setBoolean(7, flag.isComplained());
            preparedStatement.executeUpdate();
        }
   }

    public Optional<PersonFlag> getFlag(UUID flagId) throws SQLException {
        String sql = "SELECT * FROM module_person_flag WHERE flagId = ?";

        try(Connection connection = databaseService.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, flagId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return (resultSet.next()) ? Optional.of(getPersonByResultSet(resultSet)) : Optional.empty();
            }
        }
    }

}
