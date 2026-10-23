package com.github.astrolddd.cleanhud.hud;

import com.github.astrolddd.cleanhud.config.ArmorHudConfig;
import com.github.astrolddd.cleanhud.config.DurabilityMode;
import com.github.astrolddd.cleanhud.config.DurabilityTextPosition;
import com.github.astrolddd.cleanhud.config.HudAlignment;
import com.github.astrolddd.cleanhud.config.MainHandPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class ArmorHudRenderer {
	public record HudBounds(int x, int y, int width, int height, int offsetX, int offsetY) {
	}
	private static final int BAR_OFFSET_Y = 15;
	private static final int BAR_HEIGHT_SPACE = 2;

	private static final EquipmentSlot[] ARMOR_SLOTS = {
		EquipmentSlot.HEAD,
		EquipmentSlot.CHEST,
		EquipmentSlot.LEGS,
		EquipmentSlot.FEET
	};

	private static final Set<EquipmentSlot> WARNED_ARMOR = EnumSet.noneOf(EquipmentSlot.class);
	private static boolean warnedOffhand = false;
	private static boolean warnedMainhand = false;

	private ArmorHudRenderer() {
	}

	private record BarRender(int x, int y, float durability) {
	}

	private record WarningRender(int x, int y) {
	}

	public static List<ItemStack> collectArmorStacks(Player player) {
		List<ItemStack> stacks = new ArrayList<>(6);
		stacks.add(player.getOffhandItem());
		stacks.add(player.getMainHandItem());
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			stacks.add(player.getItemBySlot(slot));
		}
		return stacks;
	}

	public static int countEquipped(List<ItemStack> armorStacks) {
		int count = 0;
		for (ItemStack stack : armorStacks) {
			if (!stack.isEmpty()) {
				count++;
			}
		}
		return count;
	}

	public static int measureWidth(ArmorHudConfig config, List<ItemStack> armorStacks) {
		if (config.isVanillaStyle()) {
			return VanillaHotbarRenderer.measureWidth(config, armorStacks);
		}
		if (!hasVisibleStack(config, armorStacks)) {
			return 0;
		}
		int slotWidth = slotWidth(config, armorStacks);
		if (config.alignment == HudAlignment.HORIZONTAL) {
			int slots = horizontalLayoutSlots(config, armorStacks);
			return slotWidth * slots + config.spacing * (slots - 1);
		}
		return slotWidth;
	}

	public static int measureHeight(ArmorHudConfig config, List<ItemStack> armorStacks) {
		if (config.isVanillaStyle()) {
			return VanillaHotbarRenderer.measureHeight(config, armorStacks);
		}
		if (!hasVisibleStack(config, armorStacks)) {
			return 0;
		}
		int slotHeight = slotHeight(config);
		if (config.alignment == HudAlignment.VERTICAL) {
			int slots = verticalLayoutSlots(config, armorStacks);
			return slotHeight * slots + config.spacing * (slots - 1);
		}
		return slotHeight;
	}

	public static int measureVisualWidth(ArmorHudConfig config, List<ItemStack> armorStacks) {
		if (config.isVanillaStyle()) {
			return VanillaHotbarRenderer.measureVisualWidth(config, armorStacks);
		}
		int width = measureWidth(config, armorStacks);
		int height = measureHeight(config, armorStacks);
		double radians = Math.toRadians(config.rotation);
		double scaledWidth = width * config.scale;
		double scaledHeight = height * config.scale;
		return Math.max(1, (int) Math.ceil(Math.abs(scaledWidth * Math.cos(radians)) + Math.abs(scaledHeight * Math.sin(radians))));
	}

	public static int measureVisualHeight(ArmorHudConfig config, List<ItemStack> armorStacks) {
		if (config.isVanillaStyle()) {
			return VanillaHotbarRenderer.measureVisualHeight(config, armorStacks);
		}
		int width = measureWidth(config, armorStacks);
		int height = measureHeight(config, armorStacks);
		double radians = Math.toRadians(config.rotation);
		double scaledWidth = width * config.scale;
		double scaledHeight = height * config.scale;
		return Math.max(1, (int) Math.ceil(Math.abs(scaledWidth * Math.sin(radians)) + Math.abs(scaledHeight * Math.cos(radians))));
	}

	public static HudBounds visualBounds(ArmorHudConfig config, List<ItemStack> armorStacks, int screenWidth, int screenHeight) {
		if (config.isVanillaStyle()) {
			return VanillaHotbarRenderer.visualBounds(config, armorStacks, screenWidth, screenHeight);
		}
		int fallbackX = visualX(config, armorStacks, screenWidth);
		int fallbackY = visualY(config, armorStacks, screenHeight);
		int fallbackWidth = measureVisualWidth(config, armorStacks);
		int fallbackHeight = measureVisualHeight(config, armorStacks);

		if (config.rotation != 0.0f || armorStacks.isEmpty() || countVisible(config, armorStacks) == 0) {
			return new HudBounds(fallbackX, fallbackY, fallbackWidth, fallbackHeight, 0, 0);
		}

		Font font = Minecraft.getInstance().font;
		int strideX = slotWidth(config, armorStacks) + config.spacing;
		int strideY = slotHeight(config) + config.spacing;
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int visualIndex = 0;
		int armorIndex = 0;
		for (int i = 0; i < armorStacks.size(); i++) {
			ItemStack stack = armorStacks.get(i);
			if (!shouldRenderStack(config, armorStacks, i, stack)) {
				if (config.alignment == HudAlignment.HORIZONTAL && isReservedHandSlot(config, i)) {
					visualIndex++;
				}
				continue;
			}
			int slotX = config.alignment == HudAlignment.HORIZONTAL ? horizontalSlotX(config, armorStacks, i, strideX) : 0;
			int slotY = config.alignment == HudAlignment.VERTICAL ? verticalSlotY(config, armorStacks, i, armorIndex, strideY) : 0;
			int padding = config.padding;
			String text = config.durabilityMode != DurabilityMode.NONE && stack.isDamageableItem()
				? formatDurability(stack, config.durabilityMode)
				: "";
			int maxTextWidth = maxDurabilityTextWidth(config, armorStacks);
			int itemX = slotX + padding;
			if (config.durabilityMode != DurabilityMode.NONE && config.durabilityTextPosition == DurabilityTextPosition.LEFT) {
				itemX += maxTextWidth + 4;
			}
			int itemY = slotY + padding;

			minX = Math.min(minX, itemX);
			minY = Math.min(minY, itemY);
			maxX = Math.max(maxX, itemX + 16);
			maxY = Math.max(maxY, itemY + itemBackgroundHeight(config, stack));

			if (config.showDurabilityBar && stack.isDamageableItem()) {
				int barX = barX(itemX);
				int barY = itemY + barOffsetY(config, i);
				minX = Math.min(minX, barX);
				minY = Math.min(minY, barY);
				maxX = Math.max(maxX, barX + barWidth());
				maxY = Math.max(maxY, barY + 2);
			}

			if (!text.isEmpty()) {
				float textScale = textRenderScale(config);
				int textWidth = scaledTextWidth(font, text, textScale);
				int textX = switch (config.durabilityTextPosition) {
					case LEFT -> slotX + padding + maxTextWidth - textWidth;
					case RIGHT -> itemX + 16 + 4;
					case BELOW -> itemX + (16 - textWidth) / 2;
				};
				int textY = config.durabilityTextPosition == DurabilityTextPosition.BELOW
					? itemY + 16 + padding + 4
					: itemY + (16 - textHeight(config)) / 2;
				minX = Math.min(minX, textX);
				minY = Math.min(minY, textY);
				maxX = Math.max(maxX, textX + textWidth);
				maxY = Math.max(maxY, textY + textHeight(config));
			}
			if (config.alignment == HudAlignment.HORIZONTAL) {
				visualIndex++;
			} else if (i >= 2) {
				armorIndex++;
			}
		}

		if (minX == Integer.MAX_VALUE) {
			return new HudBounds(fallbackX, fallbackY, fallbackWidth, fallbackHeight, 0, 0);
		}

		int offsetX = (int) Math.floor(minX * config.scale);
		int offsetY = (int) Math.floor(minY * config.scale);
		int x = fallbackX + offsetX;
		int y = fallbackY + offsetY;
		int width = Math.max(1, (int) Math.ceil((maxX - minX) * config.scale));
		int height = Math.max(1, (int) Math.ceil((maxY - minY) * config.scale));
		return new HudBounds(x, y, width, height, offsetX, offsetY);
	}

	public static int visualX(ArmorHudConfig config, List<ItemStack> armorStacks, int screenWidth) {
		if (config.isVanillaStyle()) {
			return VanillaHotbarRenderer.renderX(config, armorStacks, screenWidth);
		}
		return renderX(config, armorStacks, screenWidth);
	}

	public static int visualY(ArmorHudConfig config, List<ItemStack> armorStacks, int screenHeight) {
		if (config.isVanillaStyle()) {
			return VanillaHotbarRenderer.renderY(config, armorStacks, screenHeight);
		}
		return renderY(config, armorStacks, screenHeight);
	}

	private static int slotHeight(ArmorHudConfig config) {
		int height = 16 + config.padding * 2;
		if (config.showDurabilityBar) {
			height += BAR_HEIGHT_SPACE;
		}
		if (config.durabilityMode != DurabilityMode.NONE && config.durabilityTextPosition == DurabilityTextPosition.BELOW) {
			height += textHeight(config) + 4;
		}
		return height;
	}

	private static int slotWidth(ArmorHudConfig config, List<ItemStack> armorStacks) {
		int width = 16 + config.padding * 2;
		if (config.durabilityMode != DurabilityMode.NONE && config.durabilityTextPosition != DurabilityTextPosition.BELOW) {
			width += 4 + maxDurabilityTextWidth(config, armorStacks);
		}
		return width;
	}

	private static int maxDurabilityTextWidth(ArmorHudConfig config, List<ItemStack> armorStacks) {
		if (config.durabilityMode == DurabilityMode.NONE) {
			return 0;
		}

		Font font = Minecraft.getInstance().font;
		float scale = textRenderScale(config);
		int maxWidth = 0;
		if (config.durabilityMode == DurabilityMode.PERCENTAGE) {
			maxWidth = scaledTextWidth(font, "100%", scale);
		} else if (config.durabilityMode == DurabilityMode.NUMERIC) {
			maxWidth = scaledTextWidth(font, "9999", scale);
		}
		for (ItemStack stack : armorStacks) {
			if (!stack.isEmpty() && stack.isDamageableItem()) {
				maxWidth = Math.max(maxWidth, scaledTextWidth(font, formatDurability(stack, config.durabilityMode), scale));
			}
		}
		return maxWidth;
	}

	private static float textRenderScale(ArmorHudConfig config) {
		return Math.max(0.25f, config.durabilityTextScale);
	}

	private static int scaledTextWidth(Font font, String text, float scale) {
		return Math.max(1, (int) Math.ceil(font.width(text) * scale));
	}

	private static int textHeight(ArmorHudConfig config) {
		return Math.max(1, (int) Math.ceil(8 * textRenderScale(config)));
	}

	public static void render(GuiGraphicsExtractor graphics, ArmorHudConfig config, List<ItemStack> armorStacks, int screenWidth, int screenHeight) {
		if (!config.enabled || armorStacks == null || armorStacks.isEmpty()) {
			return;
		}
		checkAndPlayWarningSounds(config, armorStacks);

		if (config.isVanillaStyle()) {
			VanillaHotbarRenderer.render(graphics, config, armorStacks, screenWidth, screenHeight, false);
			return;
		}
		if (countVisible(config, armorStacks) == 0) {
			return;
		}

		int hudWidth = measureWidth(config, armorStacks);
		int hudHeight = measureHeight(config, armorStacks);
		int x = renderX(config, armorStacks, screenWidth);
		int y = renderY(config, armorStacks, screenHeight);
		renderTransformed(graphics, config, armorStacks, x, y, hudWidth, hudHeight);
	}

	public static void renderEditor(GuiGraphicsExtractor graphics, ArmorHudConfig config, List<ItemStack> armorStacks, int screenWidth, int screenHeight) {
		if (config.isVanillaStyle()) {
			VanillaHotbarRenderer.render(graphics, config, armorStacks, screenWidth, screenHeight, true);
			return;
		}
		if (armorStacks == null || armorStacks.isEmpty() || countVisible(config, armorStacks) == 0) {
			return;
		}

		int hudWidth = measureWidth(config, armorStacks);
		int hudHeight = measureHeight(config, armorStacks);
		int x = renderX(config, armorStacks, screenWidth);
		int y = renderY(config, armorStacks, screenHeight);
		renderTransformed(graphics, config, armorStacks, x, y, hudWidth, hudHeight);
	}

	private static void checkAndPlayWarningSounds(ArmorHudConfig config, List<ItemStack> armorStacks) {
		if (!config.durabilityWarning || !config.playBreakSound || armorStacks.size() < 6) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.player == null || client.isPaused()) return;

		for (int i = 0; i < armorStacks.size(); i++) {
			ItemStack stack = armorStacks.get(i);
			boolean isWarning = isWarningStack(config, stack);

			if (i == 0) {
				if (isWarning && !warnedOffhand) {
					warnedOffhand = true;
					playWarningSound(client);
				} else if (!isWarning) {
					warnedOffhand = false;
				}
			} else if (i == 1) {
				if (isWarning && !warnedMainhand) {
					warnedMainhand = true;
					playWarningSound(client);
				} else if (!isWarning) {
					warnedMainhand = false;
				}
			} else {
				EquipmentSlot slot = ARMOR_SLOTS[i - 2];
				if (isWarning && !WARNED_ARMOR.contains(slot)) {
					WARNED_ARMOR.add(slot);
					playWarningSound(client);
				} else if (!isWarning) {
					WARNED_ARMOR.remove(slot);
				}
			}
		}
	}

	public static void triggerWarningSoundBurst(int count, float intervalSeconds) {
		com.github.astrolddd.cleanhud.CleanHudClient.triggerWarningSoundBurst(count, intervalSeconds);
	}

	private static void playWarningSound(Minecraft client) {
		ArmorHudConfig config = com.github.astrolddd.cleanhud.config.ConfigManager.get();
		com.github.astrolddd.cleanhud.CleanHudClient.triggerWarningSoundBurst(config.breakSoundCount, config.breakSoundInterval);
	}

	private static int measureAnchorHeight(ArmorHudConfig config, List<ItemStack> armorStacks) {
		int slotHeight = slotHeight(config);
		if (config.alignment == HudAlignment.VERTICAL) {
			int armorRows = visibleArmorRows(config, armorStacks);
			if (armorRows == 0) {
				armorRows = Math.max(1, visibleHandRows(config, armorStacks));
			}
			return slotHeight * armorRows + config.spacing * Math.max(0, armorRows - 1);
		}
		return slotHeight;
	}

	private static int measureAnchorWidth(ArmorHudConfig config, List<ItemStack> armorStacks) {
		int slotWidth = slotWidth(config, armorStacks);
		if (config.alignment == HudAlignment.HORIZONTAL) {
			int armorCols = visibleArmorRows(config, armorStacks);
			if (armorCols == 0) {
				armorCols = Math.max(1, visibleHandRows(config, armorStacks));
			}
			return slotWidth * armorCols + config.spacing * Math.max(0, armorCols - 1);
		}
		return slotWidth;
	}

	private static int renderX(ArmorHudConfig config, List<ItemStack> armorStacks, int screenWidth) {
		int x = config.resolveX(screenWidth);
		int maxX = Math.max(0, screenWidth - measureAnchorWidth(config, armorStacks));
		return clamp(x, 0, maxX);
	}

	private static int renderY(ArmorHudConfig config, List<ItemStack> armorStacks, int screenHeight) {
		int y = config.resolveY(screenHeight);
		int maxY = Math.max(0, screenHeight - measureAnchorHeight(config, armorStacks));
		return clamp(y, 0, maxY);
	}

	public static int storedYFromVisualY(ArmorHudConfig config, List<ItemStack> armorStacks, int visualY) {
		return visualY;
	}

	private static void renderTransformed(GuiGraphicsExtractor graphics, ArmorHudConfig config, List<ItemStack> armorStacks, int x, int y, int hudWidth, int hudHeight) {
		List<BarRender> snappedBars = new ArrayList<>();
		List<WarningRender> snappedWarnings = new ArrayList<>();
		var pose = graphics.pose();
		pose.pushMatrix();
		pose.translate((float)Math.round(x), (float)Math.round(y));
		pose.scale(config.scale);
		if (config.rotation != 0.0f) {
			pose.translate(hudWidth / 2.0f, hudHeight / 2.0f);
			pose.rotate((float)Math.toRadians(config.rotation));
			pose.translate(-hudWidth / 2.0f, -hudHeight / 2.0f);
		}

		int strideX = slotWidth(config, armorStacks) + config.spacing;
		int strideY = slotHeight(config) + config.spacing;
		int visualIndex = 0;
		int armorIndex = 0;
		for (int i = 0; i < armorStacks.size(); i++) {
			ItemStack stack = armorStacks.get(i);
			if (!shouldRenderStack(config, armorStacks, i, stack)) {
				if (config.alignment == HudAlignment.HORIZONTAL && isReservedHandSlot(config, i)) {
					visualIndex++;
				}
				continue;
			}
			int slotX = config.alignment == HudAlignment.HORIZONTAL ? horizontalSlotX(config, armorStacks, i, strideX) : 0;
			int slotY = config.alignment == HudAlignment.VERTICAL ? verticalSlotY(config, armorStacks, i, armorIndex, strideY) : 0;
			renderSlot(graphics, config, armorStacks, i, stack, slotX, slotY, snappedBars, snappedWarnings);
			if (config.alignment == HudAlignment.HORIZONTAL) {
				visualIndex++;
			} else if (i >= 2) {
				armorIndex++;
			}
		}

		pose.popMatrix();

		if (config.rotation == 0.0f) {
			for (WarningRender warning : snappedWarnings) {
				int screenX = Math.round(x + warning.x() * config.scale);
				int screenY = Math.round(y + warning.y() * config.scale);
				drawWarningMarker(graphics, screenX, screenY);
			}
			for (BarRender bar : snappedBars) {
				int screenX = Math.round(x + bar.x() * config.scale);
				int screenY = Math.round(y + bar.y() * config.scale);
				int screenWidth = Math.max(1, Math.round(barWidth() * config.scale));
				drawSnappedDurabilityBar(graphics, bar.durability(), screenX, screenY, screenWidth);
			}
		}
	}

	private static void renderSlot(GuiGraphicsExtractor graphics, ArmorHudConfig config, List<ItemStack> armorStacks, int index, ItemStack stack, int x, int y, List<BarRender> snappedBars, List<WarningRender> snappedWarnings) {
		if (stack.isEmpty()) {
			return;
		}

		int padding = config.padding;

		Font font = Minecraft.getInstance().font;
		String text = config.durabilityMode != DurabilityMode.NONE && stack.isDamageableItem()
			? formatDurability(stack, config.durabilityMode)
			: "";
		int maxTextWidth = maxDurabilityTextWidth(config, armorStacks);
		int itemX = x + padding;
		if (config.durabilityMode != DurabilityMode.NONE && config.durabilityTextPosition == DurabilityTextPosition.LEFT) {
			itemX += maxTextWidth + 4;
		}
		int itemY = y + padding;
		if (config.showBackground) {
			int backgroundHeight = itemBackgroundHeight(config, stack);
			drawBackground(graphics, config, itemX, itemY, 16, backgroundHeight);
		}
		graphics.item(stack, itemX, itemY);

		if (config.showDurabilityBar && !stack.isEmpty() && stack.isDamageableItem()) {
			if (config.rotation == 0.0f) {
				snappedBars.add(new BarRender(barX(itemX), itemY + barOffsetY(config, index), getDurabilityFraction(stack)));
			} else {
				drawDurabilityBar(graphics, config, stack, itemX, itemY, index);
			}
		}

		if (isWarningStack(config, stack)) {
			int warningX;
			int warningY;
			if (config.durabilityMode != DurabilityMode.NONE && !text.isEmpty()) {
				if (config.durabilityTextPosition == DurabilityTextPosition.RIGHT) {
					warningX = x - 12;
					warningY = itemY + 4;
				} else if (config.durabilityTextPosition == DurabilityTextPosition.LEFT) {
					warningX = itemX + 16 + padding + 4;
					warningY = itemY + 4;
				} else {
					warningX = itemX + 4;
					warningY = y - 10;
				}
			} else {
				if (config.alignment == HudAlignment.HORIZONTAL) {
					warningX = itemX + 4;
					warningY = y - 10;
				} else {
					warningX = itemX + 18;
					warningY = itemY + 4;
				}
			}
			if (config.rotation == 0.0f) {
				snappedWarnings.add(new WarningRender(warningX, warningY));
			} else {
				drawWarningMarker(graphics, warningX, warningY);
			}
		}

		if (!text.isEmpty()) {
			int textColor = durabilityTextColor(config, stack);
			float textScale = textRenderScale(config);
			int textWidth = scaledTextWidth(font, text, textScale);
			int textX = switch (config.durabilityTextPosition) {
				case LEFT -> x + padding + maxTextWidth - textWidth;
				case RIGHT -> itemX + 16 + 4;
				case BELOW -> itemX + (16 - textWidth) / 2;
			};
			int textY = config.durabilityTextPosition == DurabilityTextPosition.BELOW
				? itemY + 16 + padding + 4
				: itemY + (16 - textHeight(config)) / 2;
			drawScaledString(graphics, font, text, textX, textY, textScale, textColor);
		}
	}

	private static boolean isWarningStack(ArmorHudConfig config, ItemStack stack) {
		if (!config.durabilityWarning || stack.isEmpty() || !stack.isDamageableItem()) {
			return false;
		}
		int maxDamage = stack.getMaxDamage();
		if (maxDamage <= 0) {
			return false;
		}
		int remaining = Math.max(0, maxDamage - stack.getDamageValue());
		int percent = (int) Math.ceil(remaining * 100.0 / maxDamage);
		return percent <= config.durabilityWarningThreshold;
	}

	private static void drawWarningMarker(GuiGraphicsExtractor graphics, int x, int y) {
		graphics.fill(x + 3, y, x + 6, y + 1, 0xFFE51F2F);
		graphics.fill(x + 1, y + 1, x + 8, y + 2, 0xFFE51F2F);
		graphics.fill(x, y + 2, x + 9, y + 7, 0xFFE51F2F);
		graphics.fill(x + 1, y + 7, x + 8, y + 8, 0xFFE51F2F);
		graphics.fill(x + 3, y + 8, x + 6, y + 9, 0xFFE51F2F);
		graphics.fill(x + 4, y + 2, x + 5, y + 6, 0xFFFFFFFF);
		graphics.fill(x + 4, y + 7, x + 5, y + 8, 0xFFFFFFFF);
	}

	private static void drawScaledString(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, float scale, int color) {
		var pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		pose.scale(scale, scale);
		graphics.text(font, text, 1, 1, 0x99000000, false);
		graphics.text(font, text, 0, 0, color, false);
		pose.popMatrix();
	}

	private static int itemBackgroundHeight(ArmorHudConfig config, ItemStack stack) {
		if (config.showDurabilityBar && !stack.isEmpty() && stack.isDamageableItem()) {
			return BAR_OFFSET_Y + 2;
		}
		return 16;
	}

	private static void drawBackground(GuiGraphicsExtractor graphics, ArmorHudConfig config, int x, int y, int width, int height) {
		int alpha = Math.round(config.backgroundOpacity * 255.0f) & 0xFF;
		if (alpha > 0) {
			int color = (alpha << 24) | 0x05070D;
			graphics.fill(x, y, x + width, y + height, color);
		}
	}

	public static boolean isToolOrWeapon(ItemStack stack) {
		if (stack.isEmpty() || !stack.isDamageableItem()) {
			return false;
		}
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			EquipmentSlot slot = player.getEquipmentSlotForItem(stack);
			if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET) {
				return false;
			}
		}
		return true;
	}

	private static int verticalLayoutSlots(ArmorHudConfig config, List<ItemStack> armorStacks) {
		int slots = visibleHandRows(config, armorStacks);
		for (int i = 2; i < armorStacks.size(); i++) {
			if (shouldRenderStack(config, armorStacks, i, armorStacks.get(i))) {
				slots++;
			}
		}
		return Math.max(1, slots);
	}

	private static int visibleHandRows(ArmorHudConfig config, List<ItemStack> armorStacks) {
		int rows = 0;
		if (armorStacks.size() > 1 && shouldRenderStack(config, armorStacks, 1, armorStacks.get(1))) {
			rows++;
		}
		if (armorStacks.size() > 0 && shouldRenderStack(config, armorStacks, 0, armorStacks.get(0))) {
			rows++;
		}
		return rows;
	}

	private static int horizontalLayoutSlots(ArmorHudConfig config, List<ItemStack> armorStacks) {
		int slots = 0;
		for (int i = 0; i < armorStacks.size(); i++) {
			ItemStack stack = armorStacks.get(i);
			if (shouldRenderStack(config, armorStacks, i, stack) || i == 1 && shouldRenderStack(config, armorStacks, 1, stack)) {
				slots++;
			}
		}
		return Math.max(1, slots);
	}

	private static int visibleArmorRows(ArmorHudConfig config, List<ItemStack> armorStacks) {
		int rows = 0;
		for (int i = 2; i < armorStacks.size(); i++) {
			if (shouldRenderStack(config, armorStacks, i, armorStacks.get(i))) {
				rows++;
			}
		}
		return rows;
	}

	private static int verticalSlotY(ArmorHudConfig config, List<ItemStack> armorStacks, int index, int armorIndex, int strideY) {
		if (index >= 2) {
			return armorIndex * strideY;
		}

		boolean offhandVisible = shouldRenderStack(config, armorStacks, 0, armorStacks.get(0));

		if (index == 0) {
			return -1 * strideY;
		}

		if (index == 1) {
			if (config.mainHandPosition == MainHandPosition.BELOW) {
				int armorRows = visibleArmorRows(config, armorStacks);
				return armorRows * strideY;
			} else {
				return (offhandVisible ? -2 : -1) * strideY;
			}
		}

		return 0;
	}

	private static int horizontalSlotX(ArmorHudConfig config, List<ItemStack> armorStacks, int index, int strideX) {
		if (index >= 2) {
			return armorBeforeCount(config, armorStacks, index) * strideX;
		}

		boolean offhandVisible = shouldRenderStack(config, armorStacks, 0, armorStacks.get(0));

		if (index == 0) {
			return -1 * strideX;
		}

		if (index == 1) {
			if (config.mainHandPosition == MainHandPosition.BELOW) {
				int armorCount = visibleArmorRows(config, armorStacks);
				return armorCount * strideX;
			} else {
				return (offhandVisible ? -2 : -1) * strideX;
			}
		}

		return 0;
	}

	private static int armorBeforeCount(ArmorHudConfig config, List<ItemStack> armorStacks, int index) {
		int count = 0;
		for (int k = 2; k < index; k++) {
			if (shouldRenderStack(config, armorStacks, k, armorStacks.get(k))) {
				count++;
			}
		}
		return count;
	}

	private static int countVisible(ArmorHudConfig config, List<ItemStack> armorStacks) {
		int count = 0;
		for (int i = 0; i < armorStacks.size(); i++) {
			if (shouldRenderStack(config, armorStacks, i, armorStacks.get(i))) {
				count++;
			}
		}
		return count;
	}

	private static boolean shouldRenderStack(ArmorHudConfig config, List<ItemStack> armorStacks, int index, ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		if (index == 0) {
			return config.showOffhandItem;
		}
		if (index == 1) {
			if (!config.showMainHandItem) {
				return false;
			}
			if (config.mainHandToolsOnly && !isToolOrWeapon(stack)) {
				return false;
			}
			return true;
		}
		return true;
	}

	private static boolean hasVisibleStack(ArmorHudConfig config, List<ItemStack> armorStacks) {
		for (int i = 0; i < armorStacks.size(); i++) {
			if (shouldRenderStack(config, armorStacks, i, armorStacks.get(i))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isReservedHandSlot(ArmorHudConfig config, int index) {
		return index == 1 && config.showMainHandItem || index == 0 && config.showOffhandItem;
	}

	private static int barX(int itemX) {
		return itemX + 2;
	}

	private static int barWidth() {
		return 12;
	}

	private static int barOffsetY(ArmorHudConfig config, int index) {
		if (config.alignment == HudAlignment.VERTICAL && (index == 2 || index == 5)) {
			return BAR_OFFSET_Y - 2;
		}
		return BAR_OFFSET_Y;
	}

	private static void drawDurabilityBar(GuiGraphicsExtractor graphics, ArmorHudConfig config, ItemStack stack, int itemX, int itemY, int index) {
		float durability = getDurabilityFraction(stack);
		int x = barX(itemX);
		int y = itemY + barOffsetY(config, index);
		int width = barWidth();
		drawSnappedDurabilityBar(graphics, durability, x, y, width);
	}

	private static void drawSnappedDurabilityBar(GuiGraphicsExtractor graphics, float durability, int x, int y, int width) {
		int background = 0xFF000000;
		int fill = durabilityBarColor(durability);

		var pose = graphics.pose();
		pose.pushMatrix();
		
		graphics.fill(x, y, x + width, y + 2, background);
		int fillWidth = Math.max(1, Math.round(width * durability));
		graphics.fill(x, y, x + fillWidth, y + 1, fill);
		pose.popMatrix();
	}

	private static float getDurabilityFraction(ItemStack stack) {
		int maxDamage = stack.getMaxDamage();
		if (maxDamage <= 0) {
			return 1.0f;
		}
		return 1.0f - (float) stack.getDamageValue() / maxDamage;
	}

	private static int durabilityBarColor(float durability) {
		if (durability >= 0.8f) {
			return 0xFF35FF5A;
		}
		if (durability >= 0.6f) {
			return 0xFFB7FF3A;
		}
		if (durability >= 0.4f) {
			return 0xFFFFE24A;
		}
		if (durability >= 0.2f) {
			return 0xFFFFA033;
		}
		return 0xFFFF3B3B;
	}

	private static int durabilityTextColor(ArmorHudConfig config, ItemStack stack) {
		return config.matchTextColorToBar ? durabilityBarColor(getDurabilityFraction(stack)) : 0xFFFFFFFF;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static String formatDurability(ItemStack stack, DurabilityMode mode) {
		int remaining = stack.getMaxDamage() - stack.getDamageValue();
		return switch (mode) {
			case PERCENTAGE -> Math.round(getDurabilityFraction(stack) * 100) + "%";
			case NUMERIC -> String.valueOf(remaining);
			case NONE -> "";
		};
	}

	public static List<ItemStack> previewStacks() {
		List<ItemStack> preview = new ArrayList<>(6);
		preview.add(safePreviewItem(net.minecraft.world.item.Items.SHIELD, 336));
		preview.add(safePreviewItem(net.minecraft.world.item.Items.DIAMOND_SWORD, 1561));
		preview.add(safePreviewItem(net.minecraft.world.item.Items.DIAMOND_HELMET, 363));
		preview.add(safePreviewItem(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE, 528));
		preview.add(safePreviewItem(net.minecraft.world.item.Items.DIAMOND_LEGGINGS, 495));
		preview.add(safePreviewItem(net.minecraft.world.item.Items.DIAMOND_BOOTS, 429));

		Player player = Minecraft.getInstance().player;
		if (player != null) {
			List<ItemStack> armor = collectArmorStacks(player);
			for (int i = 0; i < Math.min(armor.size(), preview.size()); i++) {
				ItemStack stack = armor.get(i);
				if (!stack.isEmpty() && (i != 1 || isToolOrWeapon(stack))) {
					preview.set(i, stack);
				}
			}
		}
		return preview;
	}

	private static ItemStack safePreviewItem(net.minecraft.world.item.Item item, int maxDamage) {
		try {
			ItemStack stack = item.getDefaultInstance();
			if (stack != null && !stack.isEmpty()) {
				stack.setDamageValue(stack.getMaxDamage() / 3);
				return stack;
			}
		} catch (Throwable ignored) {
		}
		net.minecraft.core.Holder<net.minecraft.world.item.Item> holder = net.minecraft.core.Holder.direct(item, net.minecraft.core.component.DataComponentMap.EMPTY);
		ItemStack stack = new ItemStack(holder);
		stack.set(net.minecraft.core.component.DataComponents.MAX_DAMAGE, maxDamage);
		stack.set(net.minecraft.core.component.DataComponents.DAMAGE, maxDamage / 3);
		stack.set(net.minecraft.core.component.DataComponents.ITEM_MODEL, net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item));
		return stack;
	}
}
