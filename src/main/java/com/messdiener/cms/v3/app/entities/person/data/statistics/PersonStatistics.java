package com.messdiener.cms.v3.app.entities.person.data.statistics;

import com.messdiener.cms.v3.app.entities.person.Person;
import com.messdiener.cms.v3.shared.cache.Cache;
import com.messdiener.cms.v3.utils.time.DateUtils;
import lombok.Data;

import java.sql.SQLException;

@Data
public class PersonStatistics {

	private Person person;
	private int max;
	private int available;
	private int duty;
	private int absent;

	private double aDouble;
	private double dDouble;
	private double tDouble;

	public PersonStatistics(Person person) throws SQLException {
		this.person = person;

		DateUtils.MonthNumberName start = Cache.getConfigurationService().isPresent("workSchedulerStartMonth") ? DateUtils.MonthNumberName.valueOf(Cache.getConfigurationService().get("workSchedulerStartMonth")) : DateUtils.MonthNumberName.JANUAR;
		long date = start.getFirstDay();

		//this.max = Cache.getWorshipService().getNextWorships(person.getTenantId(), date).size();
		//this.available = Cache.getWorshipService().getWorshipsByPerson(person.getId(), date,false, true).size();
		//this.duty = Cache.getWorshipService().getWorshipsByPerson(person.getId(), date,true, true).size();
		this.absent = max - available - duty;

		this.aDouble = ((double) available / max * 100);
		this.dDouble = (double) duty / max * 100;
		this.tDouble = (double) absent / max * 100;
	}

}
