package com.github.astrolddd.cleanhud;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanHud implements ModInitializer {
	public static final String MOD_ID = "cleanhud";

	// This logger is used to write text to the console and the log filer mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("CleanHud loaded");
	}
}








