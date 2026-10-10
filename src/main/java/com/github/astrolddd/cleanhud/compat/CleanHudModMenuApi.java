package com.github.astrolddd.cleanhud.compat;

import com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class CleanHudModMenuApi implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> new ArmorHudConfigScreen();
	}
}








