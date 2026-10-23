package com.github.astrolddd.cleanhud;

import com.github.astrolddd.cleanhud.config.ConfigManager;
import com.github.astrolddd.cleanhud.config.ArmorHudConfig;
import com.github.astrolddd.cleanhud.hud.ArmorHudRenderer;
import com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CleanHudClient implements ClientModInitializer {
	public static final Identifier ARMOR_HUD_ID = Identifier.fromNamespaceAndPath(CleanHud.MOD_ID, "armor_hud");
	public static KeyMapping OPEN_CONFIG_KEY;
	private static int remainingBurstSounds = 0;
	private static long nextBurstTimeMillis = 0L;
	private static long burstIntervalMillis = 1000L;

	@Override
	public void onInitializeClient() {
		ConfigManager.load();

		OPEN_CONFIG_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.cleanhud.open_config",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_H,
			new KeyMapping.Category(Identifier.fromNamespaceAndPath(CleanHud.MOD_ID, "cleanhud"))
		));

		ClientTickEvents.END_CLIENT_TICK.register(CleanHudClient::handleClientTick);

		if ("true".equalsIgnoreCase(System.getProperty("autotest")) || "cleanhud".equalsIgnoreCase(System.getProperty("autotest"))) {
			com.github.astrolddd.cleanhud.test.CleanHudAutoTestRunner.init();
		}

		CleanHud.LOGGER.info("CleanHud client initialized");
	}

	public static void triggerWarningSoundBurst(int count, float intervalSeconds) {
		Minecraft client = Minecraft.getInstance();
		if (client == null) return;
		int clampedCount = Math.clamp(count, 1, 99);
		long intervalMs = Math.max(100L, Math.round(intervalSeconds * 1000.0f));
		client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_BREAK, 1.0F));
		remainingBurstSounds = clampedCount - 1;
		burstIntervalMillis = intervalMs;
		nextBurstTimeMillis = System.currentTimeMillis() + intervalMs;
	}

	private static void tickWarningSoundBurst(Minecraft client) {
		if (remainingBurstSounds <= 0) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now >= nextBurstTimeMillis) {
			client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_BREAK, 1.0F));
			remainingBurstSounds--;
			nextBurstTimeMillis = now + burstIntervalMillis;
		}
	}

	public static void handleClientTick(Minecraft client) {
		if (com.github.astrolddd.cleanhud.test.CleanHudAutoTestRunner.isRunning()) {
			com.github.astrolddd.cleanhud.test.CleanHudAutoTestRunner.tick(client);
		}

		if (OPEN_CONFIG_KEY != null) {
			while (OPEN_CONFIG_KEY.consumeClick()) {
				if (client.player != null) {
					client.setScreenAndShow(new ArmorHudConfigScreen());
				}
			}
		}
		tickWarningSoundBurst(client);
	}

	public static void renderArmorHud(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.gui.hud.isHidden()) {
			return;
		}

		List<ItemStack> armor = ArmorHudRenderer.collectArmorStacks(client.player);
		ArmorHudRenderer.render(graphics, ConfigManager.get(), armor, graphics.guiWidth(), graphics.guiHeight());
	}
}
