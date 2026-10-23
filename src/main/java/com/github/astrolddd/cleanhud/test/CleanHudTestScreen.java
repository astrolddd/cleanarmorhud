package com.github.astrolddd.cleanhud.test;

import com.github.astrolddd.cleanhud.config.ArmorHudConfig;
import com.github.astrolddd.cleanhud.hud.ArmorHudRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CleanHudTestScreen extends Screen {
	private final String scenarioName;
	private final ArmorHudConfig testConfig;
	private final List<ItemStack> mockStacks;

	public CleanHudTestScreen(String scenarioName, ArmorHudConfig testConfig, List<ItemStack> mockStacks) {
		super(Component.literal("Clean HUD Test"));
		this.scenarioName = scenarioName;
		this.testConfig = testConfig;
		this.mockStacks = mockStacks;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, width, height, 0xFF141720);
		graphics.text(font, "[TEST SCENARIO] " + scenarioName, 12, 12, 0xFF88A0C8);
		ArmorHudRenderer.renderEditor(graphics, testConfig, mockStacks, width, height);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}
}
