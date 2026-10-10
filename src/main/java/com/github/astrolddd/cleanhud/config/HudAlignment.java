package com.github.astrolddd.cleanhud.config;

public enum HudAlignment {
	VERTICAL("vertical"),
	HORIZONTAL("horizontal");

	private final String id;

	HudAlignment(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public HudAlignment next() {
		return values()[(ordinal() + 1) % values().length];
	}

	public static HudAlignment fromId(String id) {
		for (HudAlignment alignment : values()) {
			if (alignment.id.equals(id)) {
				return alignment;
			}
		}
		return VERTICAL;
	}
}



