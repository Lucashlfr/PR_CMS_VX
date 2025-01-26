package com.messdiener.cms.v3.web.controller;

import com.messdiener.cms.v3.app.entities.event.OrganisationEvent;
import com.messdiener.cms.v3.app.entities.tenant.Tenant;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.shared.enums.OrganisationType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
public class OutputController {

	@GetMapping("/output")
	public String output(Model model) throws SQLException {

		LocalDate heute = LocalDate.now();
		WeekFields weekFields = WeekFields.of(Locale.getDefault());
		int number = heute.get(weekFields.weekOfWeekBasedYear());

		model.addAttribute("img", "/dist/assets/img/KW/KW" + number + ".png");
		System.out.println(""+number);

		Tenant tenant = Cache.getTenantService().findTenant(UUID.fromString("89fce045-2ad4-43b3-b088-d1e697999793")).orElseThrow();


		List<OrganisationEvent> events = new ArrayList<>();
		for(OrganisationEvent e : Cache.getOrganisationService().getEvents(tenant.getId(), OrganisationType.WORSHIP)){
			for(String n : getDays(number)){
				if(e.getStartDate().getGermanDate().equals(n)){
					events.add(e);
				}
			}
		}
		model.addAttribute("events", events);

		model.addAttribute("allEvents", Cache.getOrganisationService().getNextEvents(tenant.getId(), OrganisationType.WORSHIP));

		System.out.println(getDays(28));


		return "output";
	}

	public static List<String> getDays(int kw) {
		List<String> tageListe = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

		// Aktuelles Jahr ermitteln
		int aktuellesJahr = LocalDate.now().getYear();

		// Locale auf Deutsch setzen und WeekFields mit Montag als erstem Tag der Woche einstellen
		Locale locale = Locale.GERMANY;
		WeekFields weekFields = WeekFields.of(locale);
		LocalDate ersterTagDerWoche = LocalDate.of(aktuellesJahr, 1, 1)
				.with(weekFields.weekOfWeekBasedYear(), kw)
				.with(weekFields.dayOfWeek(), 1);

		// Daten der Tage der Woche in die Liste einfügen
		for (int i = 0; i < 7; i++) {
			LocalDate tag = ersterTagDerWoche.plusDays(i);
			String datum = tag.format(formatter);
			tageListe.add(datum);
		}

		return tageListe;
	}

}
