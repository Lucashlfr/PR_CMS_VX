package com.messdiener.cms.v3.shared.enums;

import lombok.Getter;

public class PersonAttributes {

	public enum Type {

		MESSDIENER, PARENT, OTHER, NULL, EXTERN;

		public static Type get(String personType) {
			if (personType == null || personType.isEmpty()) return NULL;
			return Type.valueOf(personType);
		}
	}

	@Getter
    public enum Connection {
		FRIEND("Befreundet"), SIBLING("Geschwister");

		private final String name;

		Connection(String name) {
			this.name = name;
		}
	}

	@Getter
    public enum Rank {

		MESSDIENER("Messdiener"), LEITUNGSTEAM("Leitungsteam"), OBERMESSDIENER("Obermessdiener"), NONE("Kein Messdiener"), NULL("FEHLER");

		private final String name;

		Rank(String name) {
			this.name = name;
		}

	}


	@Getter
	public enum Salutation {

		PERSONAL_YOU("persönliches Du"),
		POLITE_YOU("höfliches Du"),
		FORMAL_YOU("höfliches Sie"),
		NULL("Keine Ausgewählt");

		private final String label;

		Salutation(String label) {
			this.label = label;
		}
	}

	@Getter
	public enum Gender {

		MALE("männlich"),
		FEMALE("weiblich"),
		DIVERSE("divers"),
		NOT_SPECIFIED("Nicht angegeben");

		private final String label;

		Gender(String label) {
			this.label = label;
		}
	}
}
