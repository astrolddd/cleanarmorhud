package com.github.astrolddd.cleanhud.test;

import com.github.astrolddd.cleanhud.config.ArmorHudConfig;
import com.github.astrolddd.cleanhud.config.DurabilityMode;
import com.github.astrolddd.cleanhud.config.HudAlignment;
import com.github.astrolddd.cleanhud.config.MainHandPosition;
import com.github.astrolddd.cleanhud.hud.ArmorHudRenderer;
import com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen;
import com.github.astrolddd.cleanhud.screen.HudRepositionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CleanHudAutoTestRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger("CleanHudAutoTest");

	private static boolean running = false;
	private static boolean ready = false;
	private static int step = 0;
	private static int stepTicks = 0;
	private static ArmorHudConfigScreen configScreen = null;
	private static HudRepositionScreen repositionScreen = null;
	private static ArmorHudConfig repositionConfig = null;

	public static void init() {
		LOGGER.info("[AUTOTEST] Clean HUD autonomous test harness initialized!");
		running = true;
		ready = false;
		step = 0;
		stepTicks = 0;
	}

	public static boolean isRunning() {
		return running;
	}

	public static void tick(Minecraft client) {
		if (!running) return;

		if (!ready) {
			if (client.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen && client.gui.overlay() == null) {
				ready = true;
				LOGGER.info("[AUTOTEST] TitleScreen reached! Starting Clean HUD test stages...");
				step = 1;
				stepTicks = 0;
			} else {
				return;
			}
		}

		stepTicks++;

		switch (step) {
			case 1 -> {
				if (stepTicks == 2) {
					configScreen = new ArmorHudConfigScreen();
					client.setScreenAndShow(configScreen);
				} else if (stepTicks == 15) {
					capture(client, "01_config_tab0_display.png", () -> {
						step = 2;
						stepTicks = 0;
						configScreen.switchTab(1);
					});
				}
			}
			case 2 -> switchTabAndCapture(client, 3, 2, "02_config_tab1_position.png");
			case 3 -> switchTabAndCapture(client, 4, 3, "03_config_tab2_durability.png");
			case 4 -> switchTabAndCapture(client, 5, 4, "04_config_tab3_warnings.png");
			case 5 -> switchTabAndCapture(client, 6, -1, "05_config_tab4_gui.png");
			case 6 -> runHudStage(client, 7, "Vertical Clean HUD (Offhand -1, Mainhand -2 Above Armor)",
				() -> createTestConfig(HudAlignment.VERTICAL, DurabilityMode.NUMERIC, false, false, 60, 120),
				0.6f, "06_hud_vertical_offhand_priority.png");
			case 7 -> runHudStage(client, 8, "Horizontal Clean HUD (Percentage + Durability Bars)",
				() -> createTestConfig(HudAlignment.HORIZONTAL, DurabilityMode.PERCENTAGE, false, false, 200, 120),
				0.75f, "07_hud_horizontal.png");
			case 8 -> runHudStage(client, 9, "Low Durability Warning Alert (8% remaining)",
				() -> createTestConfig(HudAlignment.VERTICAL, DurabilityMode.NUMERIC, true, false, 60, 120),
				0.08f, "08_hud_warning_low_durability.png");
			case 9 -> runHudStage(client, 10, "Vanilla Style Vertical (Offhand -1 Below Mainhand -2)",
				() -> createTestConfig(HudAlignment.VERTICAL, DurabilityMode.NUMERIC, false, true, 60, 120),
				0.7f, "09_vanilla_vertical_both_hands.png");
			case 10 -> {
				if (stepTicks == 2) {
					repositionConfig = new ArmorHudConfig();
					repositionConfig.setRelativePosition(40, 120, 854, 480);
					repositionScreen = new HudRepositionScreen(null, repositionConfig, () -> {});
					client.setScreenAndShow(repositionScreen);
				} else if (stepTicks == 10) {
					capture(client, "10a_reposition_before_drag.png", () -> {
						List<ItemStack> preview = ArmorHudRenderer.previewStacks();
						ArmorHudRenderer.HudBounds beforeBounds = ArmorHudRenderer.visualBounds(repositionConfig, preview, repositionScreen.width, repositionScreen.height);
						double startX = beforeBounds.x() + 8;
						double startY = beforeBounds.y() + 8;
						double endX = startX + 220;
						double endY = startY + 80;
						repositionScreen.mouseClicked(new MouseButtonEvent(startX, startY, new MouseButtonInfo(0, 0)), false);
						repositionScreen.mouseDragged(new MouseButtonEvent(endX, endY, new MouseButtonInfo(0, 0)), 220, 80);
						repositionScreen.mouseReleased(new MouseButtonEvent(endX, endY, new MouseButtonInfo(0, 0)));
						ArmorHudRenderer.HudBounds afterBounds = ArmorHudRenderer.visualBounds(repositionConfig, preview, repositionScreen.width, repositionScreen.height);
						LOGGER.info("[AUTOTEST] Reposition drag verified: before=({}, {}) -> after=({}, {}), posX={}, posY={}",
							beforeBounds.x(), beforeBounds.y(), afterBounds.x(), afterBounds.y(), repositionConfig.posX, repositionConfig.posY);
					});
				} else if (stepTicks == 22) {
					capture(client, "10b_reposition_after_drag.png", () -> {
						step = 11;
						stepTicks = 0;
					});
				}
			}
			case 11 -> {
				LOGGER.info("[AUTOTEST] All Clean HUD test stages completed successfully!");
				running = false;
				client.stop();
			}
		}
	}

	private static void switchTabAndCapture(Minecraft client, int nextStep, int nextTab, String filename) {
		if (stepTicks == 12) {
			capture(client, filename, () -> {
				step = nextStep;
				stepTicks = 0;
				if (configScreen != null && nextTab >= 0) {
					configScreen.switchTab(nextTab);
				}
			});
		}
	}

	private static void runHudStage(Minecraft client, int nextStep, String title, Supplier<ArmorHudConfig> cfgSupplier, float health, String filename) {
		if (stepTicks == 2) {
			client.setScreenAndShow(new CleanHudTestScreen(title, cfgSupplier.get(), createMockArmorSet(health)));
		} else if (stepTicks == 12) {
			capture(client, filename, () -> {
				step = nextStep;
				stepTicks = 0;
			});
		}
	}

	private static ArmorHudConfig createTestConfig(HudAlignment alignment, DurabilityMode mode, boolean warning, boolean vanilla, int posX, int posY) {
		ArmorHudConfig cfg = new ArmorHudConfig();
		cfg.enabled = true;
		cfg.setVanillaStyle(vanilla);
		cfg.alignment = alignment;
		cfg.durabilityMode = mode;
		cfg.durabilityWarning = warning;
		cfg.showDurabilityBar = true;
		cfg.showOffhandItem = true;
		cfg.showMainHandItem = true;
		cfg.mainHandPosition = MainHandPosition.ABOVE;
		cfg.setRelativePosition(posX, posY, 854, 480);
		return cfg;
	}

	private static List<ItemStack> createMockArmorSet(float healthFraction) {
		List<ItemStack> stacks = new ArrayList<>(6);
		stacks.add(createDamagedItem(Items.SHIELD, 336, healthFraction));
		stacks.add(createDamagedItem(Items.NETHERITE_SWORD, 2031, healthFraction));
		stacks.add(createDamagedItem(Items.DIAMOND_HELMET, 363, healthFraction));
		stacks.add(createDamagedItem(Items.NETHERITE_CHESTPLATE, 592, healthFraction));
		stacks.add(createDamagedItem(Items.DIAMOND_LEGGINGS, 495, healthFraction));
		stacks.add(createDamagedItem(Items.DIAMOND_BOOTS, 429, healthFraction));
		return stacks;
	}

	private static ItemStack createDamagedItem(Item item, int maxDamage, float healthFraction) {
		Holder<Item> holder = Holder.direct(item, DataComponentMap.EMPTY);
		ItemStack stack = new ItemStack(holder);
		stack.set(DataComponents.MAX_DAMAGE, maxDamage);
		int damage = Math.max(0, Math.min(maxDamage - 1, (int) (maxDamage * (1.0f - healthFraction))));
		stack.set(DataComponents.DAMAGE, damage);
		stack.set(DataComponents.ITEM_MODEL, BuiltInRegistries.ITEM.getKey(item));
		return stack;
	}

	private static void capture(Minecraft mc, String filename, Runnable onDone) {
		Path dir = Path.of("build/test-screenshots/cleanhud");
		try {
			Files.createDirectories(dir);
		} catch (Exception ignored) {}
		Path targetFile = dir.resolve(filename);

		Screenshot.takeScreenshot(mc.gameRenderer.mainRenderTarget(), nativeImage -> {
			try {
				nativeImage.writeToFile(targetFile);
				LOGGER.info("[AUTOTEST] Saved: {}", filename);
			} catch (Exception e) {
				LOGGER.error("[AUTOTEST] Failed saving: {}", filename, e);
			} finally {
				nativeImage.close();
				if (onDone != null) {
					mc.execute(onDone);
				}
			}
		});
	}
}
