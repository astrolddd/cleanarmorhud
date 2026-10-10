package com.github.astrolddd.cleanhud.config;

public enum GuiTheme {
	DEFAULT("default"),
	ORANGE_GLASS("orange_glass"),
	BLUE_GLASS("blue_glass"),
	VANILLA("vanilla");

	private final String id;

	GuiTheme(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public GuiTheme next() {
		return values()[(ordinal() + 1) % values().length];
	}

	public static GuiTheme fromId(String id) {
		for (GuiTheme theme : values()) {
			if (theme.id.equals(id)) {
				return theme;
			}
		}
		return DEFAULT;
	}
}



