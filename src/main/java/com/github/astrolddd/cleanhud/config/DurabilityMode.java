package com.github.astrolddd.cleanhud.config;

public enum DurabilityMode {
	PERCENTAGE("percentage"),
	NUMERIC("numeric"),
	NONE("none");

	private final String id;

	DurabilityMode(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public DurabilityMode next() {
		return values()[(ordinal() + 1) % values().length];
	}

	public static DurabilityMode fromId(String id) {
		for (DurabilityMode mode : values()) {
			if (mode.id.equals(id)) {
				return mode;
			}
		}
		return PERCENTAGE;
	}
}



