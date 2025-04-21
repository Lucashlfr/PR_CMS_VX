package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.event.OrganisationEvent;
import com.messdiener.cms.v3.app.entities.tenant.Tenant;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class OutputController {

	private static final Logger LOGGER = LoggerFactory.getLogger(OutputController.class);
	private final Cache cache;

	@PostConstruct
	public void init() {
		LOGGER.info("OutputController initialized.");
	}

	@GetMapping("/output")
	public String output(Model model) throws SQLException {
		LocalDate today = LocalDate.now();
		int weekNumber = today.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());

		model.addAttribute("img", "/dist/assets/img/KW/KW" + weekNumber + ".png");
		LOGGER.info("Current week number: {}", weekNumber);

		UUID tenantId = UUID.fromString("89fce045-2ad4-43b3-b088-d1e697999793");
		Tenant tenant = cache.getTenantService().findTenant(tenantId)
				.orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));

		List<String> targetDates = getDays(weekNumber);
		List<OrganisationEvent> events = cache.getOrganisationEventService()
				.getEvents(tenant.getId(), OrganisationType.WORSHIP)
				.stream()
				.filter(e -> targetDates.contains(e.getStartDate().getGermanDate()))
				.toList();

		model.addAttribute("events", events);
		model.addAttribute("allEvents", cache.getOrganisationEventService()
				.getNextEvents(tenant.getId(), OrganisationType.WORSHIP));

		return "output";
	}

	public static List<String> getDays(int weekNumber) {
		List<String> dates = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		Locale locale = Locale.GERMANY;
		WeekFields weekFields = WeekFields.of(locale);

		LocalDate firstDayOfWeek = LocalDate.of(LocalDate.now().getYear(), 1, 1)
				.with(weekFields.weekOfWeekBasedYear(), weekNumber)
				.with(weekFields.dayOfWeek(), 1);

		for (int i = 0; i < 7; i++) {
			dates.add(firstDayOfWeek.plusDays(i).format(formatter));
		}

		return dates;
	}
}
