package com.github.astrolddd.cleanhud.config;

public enum DurabilityTextPosition {
	RIGHT("right"),
	LEFT("left"),
	BELOW("below");

	private final String id;

	DurabilityTextPosition(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public DurabilityTextPosition next() {
		return values()[(ordinal() + 1) % values().length];
	}
}

