package com.github.astrolddd.cleanhud;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanHud implements ModInitializer {
	public static final String MOD_ID = "cleanhud";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("CleanHud loaded");
	}
}








