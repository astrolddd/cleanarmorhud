package com.github.astrolddd.cleanhud.config;

public enum MainHandPosition {
	ABOVE("above"),
	BELOW("below");

	private final String id;

	MainHandPosition(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public MainHandPosition next() {
		MainHandPosition[] values = values();
		return values[(ordinal() + 1) % values.length];
	}
}



