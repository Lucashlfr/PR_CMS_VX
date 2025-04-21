package com.messdiener.cms.v3.app.services.organisation;

import com.messdiener.cms.v3.app.entities.event.OrganisationEvent;
import com.messdiener.cms.v3.app.entities.tenant.Tenant;
import com.messdiener.cms.v3.app.services.sql.DatabaseService;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
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
public class OrganisationAnalyticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationAnalyticsService.class);
    private final DatabaseService databaseService;
    private final OrganisationEventService organisationEventService;

    @PostConstruct
    public void init() {
        LOGGER.info("OrganisationAnalyticsService initialized.");
    }

    public List<String> getMonthNames(UUID tenantId) throws SQLException {
        Set<String> monthYearSet = new HashSet<>();
        String sql = "SELECT * FROM module_organisation_events WHERE tenantId = ? ORDER BY startDate DESC";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    monthYearSet.add(CMSDate.of(resultSet.getLong("startDate")).convertTo(DateUtils.DateType.MONTH_NAMES));
                }
            }
        }

        List<String> sortedMonthNames = monthYearSet.stream()
                .sorted((m1, m2) -> {
                    int year1 = Integer.parseInt(m1.split(" ")[1]);
                    int year2 = Integer.parseInt(m2.split(" ")[1]);
                    String month1 = m1.split(" ")[0];
                    String month2 = m2.split(" ")[0];
                    List<String> monthOrder = List.of("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember");
                    return year1 != year2 ? Integer.compare(year1, year2) : Integer.compare(monthOrder.indexOf(month1), monthOrder.indexOf(month2));
                })
                .toList();

        return sortedMonthNames;
    }

    public List<CMSDate> getMonthDates(UUID tenantId) throws SQLException {
        Set<String> monthYearSet = new HashSet<>();
        List<CMSDate> list = new ArrayList<>();
        String sql = "SELECT * FROM module_organisation_events WHERE tenantId = ? ORDER BY startDate DESC";

        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenantId.toString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(CMSDate.of(resultSet.getLong("startDate")));
                }
            }
        }

        for (CMSDate cmsDate : list) {
            String[] dateParts = cmsDate.convertTo(DateUtils.DateType.GERMAN).split("\\.");
            monthYearSet.add(dateParts[1] + "." + dateParts[2]);
        }

        list.clear();
        for (String monthYear : monthYearSet) {
            list.add(CMSDate.convert("01." + monthYear, DateUtils.DateType.GERMAN));
        }

        return list.stream()
                .sorted(Comparator.comparingLong(CMSDate::getDate))
                .toList();
    }

    public int getResponses(UUID tenantId, int response, int schedule) throws SQLException {
        String sql = "SELECT e.tenantId, e.startDate, COUNT(m.response) AS response_count FROM module_organisation_events e JOIN module_organisation_map m ON e.id = m.eventId WHERE m.response = ? AND m.schedule = ? AND e.tenantId = ? AND e.startDate >= ? GROUP BY e.tenantId";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, response);
            preparedStatement.setInt(2, schedule);
            preparedStatement.setString(3, tenantId.toString());
            preparedStatement.setLong(4, System.currentTimeMillis());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("response_count") : 0;
            }
        }
    }

    public Optional<OrganisationEvent> getEvent(long date, Tenant tenant) throws SQLException {
        String sql = "SELECT * FROM module_organisation_events WHERE tenantId = ? AND startDate = ?";
        try (Connection connection = databaseService.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tenant.getId().toString());
            preparedStatement.setLong(2, date);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(organisationEventService.getByResultSet(resultSet));
                } else {
                    return Optional.empty();
                }
            }
        }
    }
}
