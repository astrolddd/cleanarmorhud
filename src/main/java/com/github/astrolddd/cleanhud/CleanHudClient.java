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
	private static int warningCooldownTicks;

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

		CleanHud.LOGGER.info("CleanHud client initialized");
	}

	public static void handleClientTick(Minecraft client) {
		if (OPEN_CONFIG_KEY != null) {
			while (OPEN_CONFIG_KEY.consumeClick()) {
				if (client.player != null) {
					client.setScreenAndShow(new ArmorHudConfigScreen());
				}
			}
		}
		checkDurabilityWarning(client);
	}

	private static void checkDurabilityWarning(Minecraft client) {
		ArmorHudConfig config = ConfigManager.get();
		if (!config.enabled || !config.durabilityWarning || client.player == null) {
			warningCooldownTicks = Math.max(0, warningCooldownTicks - 1);
			return;
		}
		if (warningCooldownTicks > 0) {
			warningCooldownTicks--;
			return;
		}
		List<ItemStack> stacks = ArmorHudRenderer.collectArmorStacks(client.player);
		for (int i = 0; i < stacks.size(); i++) {
			if (i == 0 && !config.showOffhandItem || i == 1 && !config.showMainHandItem) {
				continue;
			}
			ItemStack stack = stacks.get(i);
			if (stack.isEmpty() || !stack.isDamageableItem()) {
				continue;
			}
			int maxDamage = stack.getMaxDamage();
			if (maxDamage <= 0) {
				continue;
			}
			int remaining = Math.max(0, maxDamage - stack.getDamageValue());
			int percent = (int) Math.ceil(remaining * 100.0 / maxDamage);
			if (percent <= config.durabilityWarningThreshold) {
				client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_BREAK, 1.0F));
				warningCooldownTicks = 80;
				return;
			}
		}
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
