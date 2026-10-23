package com.github.astrolddd.cleanhud.hud;

import com.github.astrolddd.cleanhud.config.ArmorHudConfig;
import com.github.astrolddd.cleanhud.config.DurabilityMode;
import com.github.astrolddd.cleanhud.config.DurabilityTextPosition;
import com.github.astrolddd.cleanhud.config.HudAlignment;
import com.github.astrolddd.cleanhud.config.MainHandPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class VanillaHotbarRenderer {
	private static final int SLOT_SIZE = 22;
	private static final int SLOT_STEP = 20;
	private static final int ITEM_SIZE = 16;
	private static final int ITEM_INSET = 3;

	private static final Identifier HOTBAR_SPRITE = Identifier.fromNamespaceAndPath("minecraft", "hud/hotbar");

	private static final Identifier SILHOUETTE_HELMET = Identifier.fromNamespaceAndPath("minecraft", "container/slot/helmet");
	private static final Identifier SILHOUETTE_CHEST = Identifier.fromNamespaceAndPath("minecraft", "container/slot/chestplate");
	private static final Identifier SILHOUETTE_LEGS = Identifier.fromNamespaceAndPath("minecraft", "container/slot/leggings");
	private static final Identifier SILHOUETTE_BOOTS = Identifier.fromNamespaceAndPath("minecraft", "container/slot/boots");
	private static final Identifier SILHOUETTE_OFFHAND = Identifier.fromNamespaceAndPath("minecraft", "container/slot/shield");
	private static final Identifier SILHOUETTE_MAINHAND = Identifier.fromNamespaceAndPath("minecraft", "container/slot/sword");

	private record VanillaSlot(int slotIndex, EquipmentSlot slotType, ItemStack stack, Identifier emptySilhouette, int offsetIndex) { }

	private VanillaHotbarRenderer() { }

	static int slotsAbove(ArmorHudConfig config, List<ItemStack> stacks) {
		if (stacks == null || stacks.size() < 6) return 0;
		int count = 0;
		if (config.showOffhandItem && !stacks.get(0).isEmpty()) {
			count++;
		}
		if (config.showMainHandItem && config.mainHandPosition == MainHandPosition.ABOVE
			&& (!config.mainHandToolsOnly || ArmorHudRenderer.isToolOrWeapon(stacks.get(1)))
			&& !stacks.get(1).isEmpty()) {
			count++;
		}
		return count;
	}

	static List<VanillaSlot> collectSlots(ArmorHudConfig config, List<ItemStack> stacks) {
		if (stacks == null || stacks.size() < 6) return List.of();

		boolean offhandVisible = config.showOffhandItem && !stacks.get(0).isEmpty();
		boolean mainhandVisible = config.showMainHandItem
			&& (!config.mainHandToolsOnly || ArmorHudRenderer.isToolOrWeapon(stacks.get(1)))
			&& !stacks.get(1).isEmpty();

		List<VanillaSlot> result = new ArrayList<>(6);

		int slotsAbove = 0;
		if (offhandVisible) {
			slotsAbove++;
		}
		if (mainhandVisible && config.mainHandPosition == MainHandPosition.ABOVE) {
			slotsAbove++;
		}

		int aboveOffset = -slotsAbove;
		if (mainhandVisible && config.mainHandPosition == MainHandPosition.ABOVE) {
			result.add(new VanillaSlot(1, EquipmentSlot.MAINHAND, stacks.get(1), SILHOUETTE_MAINHAND, aboveOffset++));
		}
		if (offhandVisible) {
			result.add(new VanillaSlot(0, EquipmentSlot.OFFHAND, stacks.get(0), SILHOUETTE_OFFHAND, aboveOffset++));
		}

		result.add(new VanillaSlot(2, EquipmentSlot.HEAD, stacks.get(2), SILHOUETTE_HELMET, 0));
		result.add(new VanillaSlot(3, EquipmentSlot.CHEST, stacks.get(3), SILHOUETTE_CHEST, 1));
		result.add(new VanillaSlot(4, EquipmentSlot.LEGS, stacks.get(4), SILHOUETTE_LEGS, 2));
		result.add(new VanillaSlot(5, EquipmentSlot.FEET, stacks.get(5), SILHOUETTE_BOOTS, 3));

		if (mainhandVisible && config.mainHandPosition == MainHandPosition.BELOW) {
			result.add(new VanillaSlot(1, EquipmentSlot.MAINHAND, stacks.get(1), SILHOUETTE_MAINHAND, 4));
		}

		return result;
	}

	static int measureLength(int slotCount) {
		return Math.max(1, slotCount) * SLOT_STEP + 2;
	}

	static int measureWidth(ArmorHudConfig config, List<ItemStack> stacks) {
		int count = collectSlots(config, stacks).size();
		return config.alignment == HudAlignment.VERTICAL ? SLOT_SIZE : measureLength(count);
	}

	static int measureHeight(ArmorHudConfig config, List<ItemStack> stacks) {
		int count = collectSlots(config, stacks).size();
		return config.alignment == HudAlignment.VERTICAL ? measureLength(count) : SLOT_SIZE;
	}

	static int measureVisualWidth(ArmorHudConfig config, List<ItemStack> stacks) {
		return rotatedWidth(config, measureWidth(config, stacks), measureHeight(config, stacks));
	}

	static int measureVisualHeight(ArmorHudConfig config, List<ItemStack> stacks) {
		return rotatedHeight(config, measureWidth(config, stacks), measureHeight(config, stacks));
	}

	static ArmorHudRenderer.HudBounds visualBounds(ArmorHudConfig config, List<ItemStack> stacks, int screenWidth, int screenHeight) {
		int slotsAbove = slotsAbove(config, stacks);
		int originX = renderX(config, stacks, screenWidth);
		int originY = renderY(config, stacks, screenHeight);
		int visualW = measureVisualWidth(config, stacks);
		int visualH = measureVisualHeight(config, stacks);
		int offsetX = config.alignment == HudAlignment.HORIZONTAL ? (int) Math.floor(-slotsAbove * SLOT_STEP * config.scale) : 0;
		int offsetY = config.alignment == HudAlignment.VERTICAL ? (int) Math.floor(-slotsAbove * SLOT_STEP * config.scale) : 0;
		return new ArmorHudRenderer.HudBounds(
			originX + offsetX,
			originY + offsetY,
			visualW,
			visualH,
			offsetX,
			offsetY
		);
	}

	static int renderX(ArmorHudConfig config, List<ItemStack> stacks, int screenWidth) {
		int slotsAbove = slotsAbove(config, stacks);
		int slotCount = collectSlots(config, stacks).size();
		int slotsBelow = Math.max(0, slotCount - slotsAbove);
		int minX = config.alignment == HudAlignment.HORIZONTAL ? (int) Math.ceil(slotsAbove * SLOT_STEP * config.scale) : 0;
		int maxX = config.alignment == HudAlignment.HORIZONTAL
			? (int) Math.floor(screenWidth - (slotsBelow * SLOT_STEP + 2) * config.scale)
			: Math.max(0, screenWidth - measureVisualWidth(config, stacks));
		return clamp(config.resolveX(screenWidth), minX, Math.max(minX, maxX));
	}

	static int renderY(ArmorHudConfig config, List<ItemStack> stacks, int screenHeight) {
		int slotsAbove = slotsAbove(config, stacks);
		int slotCount = collectSlots(config, stacks).size();
		int slotsBelow = Math.max(0, slotCount - slotsAbove);
		int minY = config.alignment == HudAlignment.VERTICAL ? (int) Math.ceil(slotsAbove * SLOT_STEP * config.scale) : 0;
		int maxY = config.alignment == HudAlignment.VERTICAL
			? (int) Math.floor(screenHeight - (slotsBelow * SLOT_STEP + 2) * config.scale)
			: Math.max(0, screenHeight - measureVisualHeight(config, stacks));
		return clamp(config.resolveY(screenHeight), minY, Math.max(minY, maxY));
	}

	static void render(GuiGraphicsExtractor graphics, ArmorHudConfig config, List<ItemStack> stacks, int screenWidth, int screenHeight, boolean editor) {
		if ((!editor && !config.enabled) || stacks == null || stacks.size() < 6) return;

		List<VanillaSlot> slots = collectSlots(config, stacks);
		if (slots.isEmpty()) return;

		int slotCount = slots.size();
		int totalLength = measureLength(slotCount);
		int baseWidth = config.alignment == HudAlignment.VERTICAL ? SLOT_SIZE : totalLength;
		int baseHeight = config.alignment == HudAlignment.VERTICAL ? totalLength : SLOT_SIZE;

		int originX = renderX(config, stacks, screenWidth);
		int originY = renderY(config, stacks, screenHeight);

		int slotsAbove = slotsAbove(config, stacks);
		int topOffset = -slotsAbove * SLOT_STEP;

		var pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(originX, originY);
		pose.scale(config.scale, config.scale);

		if (config.rotation != 0.0f) {
			pose.translate(baseWidth / 2.0f, baseHeight / 2.0f);
			pose.rotate((float) Math.toRadians(config.rotation));
			pose.translate(-baseWidth / 2.0f, -baseHeight / 2.0f);
		}

		if (config.alignment == HudAlignment.HORIZONTAL) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 0, 0, topOffset, 0, totalLength - 3, 22);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 179, 0, topOffset + totalLength - 3, 0, 3, 22);
		} else {
			pose.pushMatrix();
			pose.translate(SLOT_SIZE, topOffset);
			pose.rotate((float) Math.toRadians(90));
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 0, 0, 0, 0, totalLength - 3, 22);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 179, 0, totalLength - 3, 0, 3, 22);
			pose.popMatrix();
		}

		int bob = (int) Math.round(Math.sin(System.currentTimeMillis() / 180.0) * 1.5);

		for (int i = 0; i < slotCount; i++) {
			VanillaSlot slot = slots.get(i);
			int itemX = config.alignment == HudAlignment.HORIZONTAL ? slot.offsetIndex() * SLOT_STEP + ITEM_INSET : ITEM_INSET;
			int itemY = config.alignment == HudAlignment.HORIZONTAL ? ITEM_INSET : slot.offsetIndex() * SLOT_STEP + ITEM_INSET;

			if (!slot.stack.isEmpty()) {
				graphics.item(slot.stack, itemX, itemY);
				drawDurabilityBar(graphics, config, slot.stack, itemX, itemY);
				drawWarningAndText(graphics, config, slot.stack, itemX, itemY, i, totalLength, bob);
			} else if (slot.emptySilhouette != null) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, slot.emptySilhouette, itemX, itemY, ITEM_SIZE, ITEM_SIZE);
			}
		}

		pose.popMatrix();
	}

	private static void drawDurabilityBar(GuiGraphicsExtractor g, ArmorHudConfig config, ItemStack stack, int itemX, int itemY) {
		if (!config.showDurabilityBar || stack.isEmpty() || !stack.isDamageableItem()) return;
		float fraction = durability(stack);
		int barX = itemX + 2;
		int barY = itemY + 13;
		int fillWidth = Math.max(1, Math.round(12 * fraction));

		g.fill(barX, barY, barX + 12, barY + 2, 0xFF000000);
		g.fill(barX, barY, barX + fillWidth, barY + 1, barColor(fraction));
	}

	private static void drawWarningAndText(GuiGraphicsExtractor g, ArmorHudConfig config, ItemStack stack, int itemX, int itemY, int slotIndex, int totalLength, int bob) {
		Font font = Minecraft.getInstance().font;

		if (warning(config, stack)) {
			int warnX;
			int warnY;
			if (config.alignment == HudAlignment.HORIZONTAL) {
				warnX = itemX + (ITEM_SIZE - 9) / 2;
				warnY = -12 + bob;
			} else {
				warnX = -12 + bob;
				warnY = itemY + (ITEM_SIZE - 9) / 2;
			}
			drawWarningBadge(g, warnX, warnY);
		}

		if (config.durabilityMode != DurabilityMode.NONE && stack.isDamageableItem()) {
			String text = durabilityText(stack, config.durabilityMode);
			if (!text.isEmpty()) {
				int color = config.matchTextColorToBar ? barColor(durability(stack)) : 0xFFFFFFFF;
				float textScale = config.alignment == HudAlignment.HORIZONTAL
					? Math.min(config.durabilityTextScale, 0.75f)
					: config.durabilityTextScale;
				int textWidth = Math.max(1, (int) Math.ceil(font.width(text) * textScale));

				int textX;
				int textY;
				if (config.alignment == HudAlignment.HORIZONTAL) {
					textX = itemX + (ITEM_SIZE - textWidth) / 2;
					textY = config.durabilityTextPosition == DurabilityTextPosition.LEFT ? -11 : 23;
				} else {
					textX = config.durabilityTextPosition == DurabilityTextPosition.LEFT ? -textWidth - 4 : SLOT_SIZE + 4;
					textY = itemY + Math.round((ITEM_SIZE - 8 * textScale) / 2f);
				}

				drawScaledString(g, font, text, textX, textY, textScale, color);
			}
		}
	}

	private static void drawWarningBadge(GuiGraphicsExtractor g, int x, int y) {
		g.fill(x + 2, y, x + 7, y + 1, 0xFFE51F2F);
		g.fill(x + 1, y + 1, x + 8, y + 2, 0xFFE51F2F);
		g.fill(x, y + 2, x + 9, y + 7, 0xFFE51F2F);
		g.fill(x + 1, y + 7, x + 8, y + 8, 0xFFE51F2F);
		g.fill(x + 2, y + 8, x + 7, y + 9, 0xFFE51F2F);
		g.fill(x + 4, y + 2, x + 5, y + 6, 0xFFFFFFFF);
		g.fill(x + 4, y + 7, x + 5, y + 8, 0xFFFFFFFF);
	}

	private static void drawScaledString(GuiGraphicsExtractor g, Font font, String text, int x, int y, float scale, int color) {
		var pose = g.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		pose.scale(scale, scale);
		g.text(font, text, 0, 0, color, true);
		pose.popMatrix();
	}

	private static boolean warning(ArmorHudConfig c, ItemStack s) {
		return c.durabilityWarning && s.isDamageableItem() && Math.ceil(durability(s) * 100) <= c.durabilityWarningThreshold;
	}

	private static String durabilityText(ItemStack s, DurabilityMode m) {
		return m == DurabilityMode.PERCENTAGE ? Math.round(durability(s) * 100) + "%" : String.valueOf(s.getMaxDamage() - s.getDamageValue());
	}

	private static float durability(ItemStack s) {
		return s.getMaxDamage() <= 0 ? 1f : 1f - (float) s.getDamageValue() / s.getMaxDamage();
	}

	private static int barColor(float d) {
		return d >= .8f ? 0xFF35FF5A : d >= .6f ? 0xFFB7FF3A : d >= .4f ? 0xFFFFE24A : d >= .2f ? 0xFFFFA033 : 0xFFFF3B3B;
	}

	private static int rotatedWidth(ArmorHudConfig c, int w, int h) {
		double r = Math.toRadians(c.rotation);
		return Math.max(1, (int) Math.ceil(Math.abs(w * c.scale * Math.cos(r)) + Math.abs(h * c.scale * Math.sin(r))));
	}

	private static int rotatedHeight(ArmorHudConfig c, int w, int h) {
		double r = Math.toRadians(c.rotation);
		return Math.max(1, (int) Math.ceil(Math.abs(w * c.scale * Math.sin(r)) + Math.abs(h * c.scale * Math.cos(r))));
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
