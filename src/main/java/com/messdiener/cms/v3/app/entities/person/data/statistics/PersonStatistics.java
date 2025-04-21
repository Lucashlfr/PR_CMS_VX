package com.messdiener.cms.v3.app.entities.person.data.statistics;

import com.messdiener.cms.v3.app.entities.person.Person;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonStatistics {

	private Person person;
	private int max;
	private int available;
	private int duty;
	private int absent;

	private double availabilityPercentage;
	private double dutyPercentage;
	private double absentPercentage;
}
