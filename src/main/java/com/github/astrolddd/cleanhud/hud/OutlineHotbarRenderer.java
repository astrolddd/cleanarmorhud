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
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Rendering path used exclusively by Hotbar Outline Mode.  It deliberately does
 * not share layout or background code with the regular armor HUD. */
final class OutlineHotbarRenderer {
	private static final int SLOT = 22;
	// Uku's widget intentionally overlaps neighbouring 22px frames by two pixels.
	private static final int STEP = 20;
	private static final int ITEM_INSET_X = 2;
	private static final int ITEM_INSET_Y = 2;
	private static final Identifier HOTBAR_SPRITE = Identifier.fromNamespaceAndPath("minecraft", "hud/hotbar");
	private static final Identifier HOTBAR_SLOT_SPRITE = Identifier.fromNamespaceAndPath("minecraft", "hud/hotbar_offhand_left");

	private record Slot(int index, ItemStack stack, int x, int y) { }
	private record Layout(List<Slot> slots, int minX, int minY, int width, int height) { }

	private OutlineHotbarRenderer() { }

	static int measureWidth(ArmorHudConfig config, List<ItemStack> stacks) { return layout(config, stacks).width; }
	static int measureHeight(ArmorHudConfig config, List<ItemStack> stacks) { return layout(config, stacks).height; }
	static int measureVisualWidth(ArmorHudConfig config, List<ItemStack> stacks) { return rotatedWidth(config, measureWidth(config, stacks), measureHeight(config, stacks)); }
	static int measureVisualHeight(ArmorHudConfig config, List<ItemStack> stacks) { return rotatedHeight(config, measureWidth(config, stacks), measureHeight(config, stacks)); }

	static ArmorHudRenderer.HudBounds visualBounds(ArmorHudConfig config, List<ItemStack> stacks, int screenWidth, int screenHeight) {
		return new ArmorHudRenderer.HudBounds(renderX(config, stacks, screenWidth), renderY(config, stacks, screenHeight), measureVisualWidth(config, stacks), measureVisualHeight(config, stacks), 0, 0);
	}

	static int renderX(ArmorHudConfig config, List<ItemStack> stacks, int screenWidth) {
		// Position is the fixed armor block origin.  Adding a hand slot must never move it.
		return clamp(config.resolveX(screenWidth), 0, Math.max(0, screenWidth - fixedArmorVisualWidth(config)));
	}

	static int renderY(ArmorHudConfig config, List<ItemStack> stacks, int screenHeight) {
		return clamp(config.resolveY(screenHeight), 0, Math.max(0, screenHeight - fixedArmorVisualHeight(config)));
	}

	static void render(GuiGraphicsExtractor graphics, ArmorHudConfig config, List<ItemStack> stacks, int screenWidth, int screenHeight, boolean editor) {
		if ((!editor && !config.enabled) || stacks.size() < 6) return;
		Layout layout = layout(config, stacks);
		int originX = renderX(config, stacks, screenWidth);
		int originY = renderY(config, stacks, screenHeight);
		var pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(originX, originY);
		pose.scale(config.scale, config.scale);
		if (config.rotation != 0) {
			pose.translate(fixedArmorWidth(config) / 2f, fixedArmorHeight(config) / 2f);
			pose.rotate((float)Math.toRadians(config.rotation));
			pose.translate(-fixedArmorWidth(config) / 2f, -fixedArmorHeight(config) / 2f);
		}

		if (config.alignment == HudAlignment.HORIZONTAL) {
			drawHotbarStrip(graphics, layout.minX, 0, layout.width);
		} else {
			List<Slot> sorted = new ArrayList<>(layout.slots);
			sorted.sort((a, b) -> Integer.compare(a.y, b.y));
			for (int i = 0; i < sorted.size(); i++) {
				Slot slot = sorted.get(i);
				drawSquareSlot(graphics, slot.x, slot.y, i == 0);
			}
		}

		for (Slot slot : layout.slots) {
			int x = slot.x;
			int y = slot.y;
			if (!slot.stack.isEmpty()) {
				graphics.item(slot.stack, x + ITEM_INSET_X, y + ITEM_INSET_Y);
			} else if (slot.index >= 2) {
				// Uku's empty armor guides make the four permanent slots recognizable.
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, emptyArmorSprite(slot.index), x + ITEM_INSET_X, y + ITEM_INSET_Y, 16, 16);
			}
			if (!slot.stack.isEmpty()) drawTextAndWarning(graphics, config, slot.stack, x, y);
		}
		// Some armor models (especially chestplates) draw into the normal bar area.
		// Draw the compact outline bar last so it is always complete and readable.
		for (Slot slot : layout.slots) drawDurabilityBar(graphics, slot.stack, slot.x + ITEM_INSET_X, slot.y + ITEM_INSET_Y);
		pose.popMatrix();
	}

	private static Layout layout(ArmorHudConfig config, List<ItemStack> stacks) {
		boolean offhand = handVisible(config, stacks.get(0), 0);
		boolean mainhand = handVisible(config, stacks.get(1), 1);
		int handCount = (offhand ? 1 : 0) + (mainhand ? 1 : 0);
		List<Slot> result = new ArrayList<>(4 + handCount);
		for (int armor = 0; armor < 4; armor++) result.add(new Slot(armor + 2, stacks.get(armor + 2), config.alignment == HudAlignment.HORIZONTAL ? armor * STEP : 0, config.alignment == HudAlignment.VERTICAL ? armor * STEP : 0));
		int armorEnd = 4 * STEP;
		// Offhand is independent: the Main Hand position setting never relocates it.
		if (offhand) result.add(new Slot(0, stacks.get(0), config.alignment == HudAlignment.HORIZONTAL ? -STEP : 0, config.alignment == HudAlignment.VERTICAL ? -STEP : 0));
		if (mainhand) {
			int mainPosition = config.mainHandPosition == MainHandPosition.BELOW
				? armorEnd
				: (offhand ? -2 : -1) * STEP;
			result.add(new Slot(1, stacks.get(1), config.alignment == HudAlignment.HORIZONTAL ? mainPosition : 0, config.alignment == HudAlignment.VERTICAL ? mainPosition : 0));
		}
		int minX = result.stream().mapToInt(Slot::x).min().orElse(0);
		int minY = result.stream().mapToInt(Slot::y).min().orElse(0);
		int maxX = result.stream().mapToInt(s -> s.x + SLOT).max().orElse(SLOT);
		int maxY = result.stream().mapToInt(s -> s.y + SLOT).max().orElse(SLOT);
		return new Layout(result, minX, minY, maxX - minX, maxY - minY);
	}

	private static boolean handVisible(ArmorHudConfig config, ItemStack stack, int index) {
		if (stack.isEmpty()) return false;
		if (index == 0) return config.showOffhandItem;
		return config.showMainHandItem && (!config.mainHandToolsOnly || ArmorHudRenderer.isToolOrWeapon(stack));
	}

	private static void drawSquareSlot(GuiGraphicsExtractor g, int x, int y, boolean isTop) {
		if (isTop) {
			g.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SLOT_SPRITE, x, y, SLOT, SLOT);
		} else {
			g.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SLOT_SPRITE, 22, 22, 0, 2, x, y, 22, 20);
		}
	}

	/**
	 * Draw a variable-length vanilla hotbar without invoking its nine-slice minimum
	 * size.  The body is cropped before the original right cap and the final 3px
	 * cap is placed independently, so the frame precisely follows active slots.
	 */
	private static void drawHotbarStrip(GuiGraphicsExtractor g, int x, int y, int width) {
		int bodyWidth = Math.max(1, width - 3);
		g.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 0, 0, x, y, bodyWidth, SLOT);
		g.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 179, 0, x + bodyWidth, y, 3, SLOT);
	}

	private static Identifier emptyArmorSprite(int index) {
		return switch (index) {
			case 2 -> Identifier.fromNamespaceAndPath("minecraft", "container/slot/helmet");
			case 3 -> Identifier.fromNamespaceAndPath("minecraft", "container/slot/chestplate");
			case 4 -> Identifier.fromNamespaceAndPath("minecraft", "container/slot/leggings");
			default -> Identifier.fromNamespaceAndPath("minecraft", "container/slot/boots");
		};
	}

	private static void drawDurabilityBar(GuiGraphicsExtractor g, ItemStack stack, int itemX, int itemY) {
		if (stack.isEmpty() || !stack.isDamageableItem()) return;
		float fraction = durability(stack);
		var pose = g.pose();
		pose.pushMatrix();
		
		g.fill(itemX + 2, itemY + 14, itemX + 14, itemY + 16, 0xFF000000);
		g.fill(itemX + 2, itemY + 14, itemX + 2 + Math.max(1, Math.round(12 * fraction)), itemY + 15, barColor(fraction));
		pose.popMatrix();
	}

	private static void drawTextAndWarning(GuiGraphicsExtractor g, ArmorHudConfig config, ItemStack stack, int x, int y) {
		Font font = Minecraft.getInstance().font;
		String text = config.durabilityMode == DurabilityMode.NONE || !stack.isDamageableItem() ? "" : durabilityText(stack, config.durabilityMode);
		if (!text.isEmpty()) {
			int color = config.matchTextColorToBar ? barColor(durability(stack)) : 0xFFFFFFFF;
			// The native bar advances by 20px; a full-size "100%" is wider than that.
			// Uku keeps one readable label per slot, so compact horizontal labels must fit
			// inside that 20px cadence instead of painting over their neighbours.
			float textScale = config.alignment == HudAlignment.HORIZONTAL
				? Math.min(config.durabilityTextScale, 0.75f)
				: config.durabilityTextScale;
			int textWidth = Math.max(1, (int) Math.ceil(font.width(text) * textScale));
			int textX = config.durabilityTextPosition == DurabilityTextPosition.LEFT ? x - 4 - textWidth : config.durabilityTextPosition == DurabilityTextPosition.RIGHT ? x + SLOT + 4 : x + (SLOT - textWidth) / 2;
			int textY = config.durabilityTextPosition == DurabilityTextPosition.BELOW ? y + SLOT + 3 : y + 7;
			drawScaledString(g, font, text, textX, textY, textScale, color);
		}
		if (warning(config, stack)) drawWarning(g, config.alignment == HudAlignment.HORIZONTAL ? x + 7 : x + SLOT + 2, config.alignment == HudAlignment.HORIZONTAL ? y - 10 : y + 6);
	}

	private static void drawScaledString(GuiGraphicsExtractor g, Font font, String text, int x, int y, float scale, int color) {
		var pose = g.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		pose.scale(scale, scale);
		g.text(font, text, 0, 0, color, true);
		pose.popMatrix();
	}

	private static boolean warning(ArmorHudConfig c, ItemStack s) { return c.durabilityWarning && s.isDamageableItem() && Math.ceil(durability(s) * 100) <= c.durabilityWarningThreshold; }
	private static void drawWarning(GuiGraphicsExtractor g, int x, int y) { g.fill(x + 3, y, x + 6, y + 1, 0xFFE51F2F); g.fill(x + 1, y + 1, x + 8, y + 2, 0xFFE51F2F); g.fill(x, y + 2, x + 9, y + 7, 0xFFE51F2F); g.fill(x + 1, y + 7, x + 8, y + 8, 0xFFE51F2F); g.fill(x + 3, y + 8, x + 6, y + 9, 0xFFE51F2F); g.fill(x + 4, y + 2, x + 5, y + 6, 0xFFFFFFFF); g.fill(x + 4, y + 7, x + 5, y + 8, 0xFFFFFFFF); }
	private static String durabilityText(ItemStack s, DurabilityMode m) { return m == DurabilityMode.PERCENTAGE ? Math.round(durability(s) * 100) + "%" : String.valueOf(s.getMaxDamage() - s.getDamageValue()); }
	private static float durability(ItemStack s) { return s.getMaxDamage() <= 0 ? 1f : 1f - (float) s.getDamageValue() / s.getMaxDamage(); }
	private static int barColor(float d) { return d >= .8f ? 0xFF35FF5A : d >= .6f ? 0xFFB7FF3A : d >= .4f ? 0xFFFFE24A : d >= .2f ? 0xFFFFA033 : 0xFFFF3B3B; }
	private static int fixedArmorWidth(ArmorHudConfig c) { return c.alignment == HudAlignment.HORIZONTAL ? SLOT + 3 * STEP : SLOT; }
	private static int fixedArmorHeight(ArmorHudConfig c) { return c.alignment == HudAlignment.VERTICAL ? SLOT + 3 * STEP : SLOT; }
	private static int fixedArmorVisualWidth(ArmorHudConfig c) { return rotatedWidth(c, fixedArmorWidth(c), fixedArmorHeight(c)); }
	private static int fixedArmorVisualHeight(ArmorHudConfig c) { return rotatedHeight(c, fixedArmorWidth(c), fixedArmorHeight(c)); }
	private static int rotatedWidth(ArmorHudConfig c, int w, int h) { double r = Math.toRadians(c.rotation); return Math.max(1, (int) Math.ceil(Math.abs(w*c.scale*Math.cos(r)) + Math.abs(h*c.scale*Math.sin(r)))); }
	private static int rotatedHeight(ArmorHudConfig c, int w, int h) { double r = Math.toRadians(c.rotation); return Math.max(1, (int) Math.ceil(Math.abs(w*c.scale*Math.sin(r)) + Math.abs(h*c.scale*Math.cos(r)))); }
	private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}


