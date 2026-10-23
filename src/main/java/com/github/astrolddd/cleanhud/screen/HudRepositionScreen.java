package com.github.astrolddd.cleanhud.screen;

import com.astrolddd.commongui.theme.UiTheme;
import com.astrolddd.commongui.widget.RoundedButton;
import com.github.astrolddd.cleanhud.config.ArmorHudConfig;
import com.github.astrolddd.cleanhud.config.ConfigManager;
import com.github.astrolddd.cleanhud.hud.ArmorHudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class HudRepositionScreen extends Screen {
	private static final int TEXT_COLOR = 0xFFF2F2F5;
	private static final int TEXT_DIM_COLOR = 0xAAF2F2F5;
	private static final int ACCENT_COLOR = 0xFFEEEEEE;

	private final Screen parent;
	private final ArmorHudConfig config;
	private final Runnable onApply;
	private boolean dragging;
	private int dragOffsetX;
	private int dragOffsetY;
	private RoundedButton doneButton;

	public HudRepositionScreen(Screen parent, ArmorHudConfig config) {
		this(parent, config, ConfigManager::save);
	}

	public HudRepositionScreen(Screen parent, ArmorHudConfig config, Runnable onApply) {
		super(Component.translatable("screen.cleanhud.reposition"));
		this.parent = parent;
		this.config = config;
		this.onApply = onApply;
	}

	@Override
	protected void init() {
		int btnW = 100;
		int btnH = 24;
		doneButton = new RoundedButton(width / 2 - btnW / 2, height - 38, btnW, btnH,
			Component.translatable("gui.cleanhud.done"),
			button -> {
				applyAndReturn();
				minecraft.setScreenAndShow(parent);
			}
		);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, width, height, 0x88000000);

		String title = Component.translatable("screen.cleanhud.reposition.hint").getString();
		graphics.centeredText(font, UiTheme.text(title), width / 2, 16, TEXT_COLOR);
		graphics.centeredText(font, UiTheme.text(Component.translatable("screen.cleanhud.reposition.subhint").getString()), width / 2, 30, TEXT_DIM_COLOR);

		List<net.minecraft.world.item.ItemStack> preview = ArmorHudRenderer.previewStacks();
		ArmorHudRenderer.renderEditor(graphics, config, preview, width, height);

		ArmorHudRenderer.HudBounds hudBounds = ArmorHudRenderer.visualBounds(config, preview, width, height);
		int hudX = hudBounds.x();
		int hudY = hudBounds.y();
		int hudWidth = hudBounds.width();
		int hudHeight = hudBounds.height();

		if (dragging || isHoveringHud(mouseX, mouseY, hudX, hudY, hudWidth, hudHeight)) {
			int left = hudX - 2;
			int top = hudY - 2;
			int right = hudX + hudWidth + 2;
			int bottom = hudY + hudHeight + 2;
			graphics.fill(left, top, right, top + 1, ACCENT_COLOR);
			graphics.fill(left, bottom - 1, right, bottom, ACCENT_COLOR);
			graphics.fill(left, top, left + 1, bottom, ACCENT_COLOR);
			graphics.fill(right - 1, top, right, bottom, ACCENT_COLOR);
		}

		if (doneButton != null) {
			doneButton.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
		}

		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	private boolean isHoveringHud(int mouseX, int mouseY, int hudX, int hudY, int hudWidth, int hudHeight) {
		return mouseX >= hudX && mouseX <= hudX + hudWidth && mouseY >= hudY && mouseY <= hudY + hudHeight;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (doneButton != null && doneButton.mouseClicked(event, doubled)) {
			return true;
		}

		if (event.button() == 0 || event.button() == 1) {
			List<net.minecraft.world.item.ItemStack> preview = ArmorHudRenderer.previewStacks();
			ArmorHudRenderer.HudBounds hudBounds = ArmorHudRenderer.visualBounds(config, preview, width, height);
			int hudX = hudBounds.x();
			int hudY = hudBounds.y();
			int hudWidth = hudBounds.width();
			int hudHeight = hudBounds.height();

			if (isHoveringHud((int) event.x(), (int) event.y(), hudX, hudY, hudWidth, hudHeight)) {
				dragging = true;
				dragOffsetX = (int) event.x() - hudX;
				dragOffsetY = (int) event.y() - hudY;
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging) {
			List<net.minecraft.world.item.ItemStack> preview = ArmorHudRenderer.previewStacks();
			ArmorHudRenderer.HudBounds hudBounds = ArmorHudRenderer.visualBounds(config, preview, width, height);
			int desiredX = MthClamp((int) event.x() - dragOffsetX, 0, width - hudBounds.width());
			int desiredY = MthClamp((int) event.y() - dragOffsetY, 0, height - hudBounds.height());
			int anchorX = desiredX - hudBounds.offsetX();
			int anchorY = ArmorHudRenderer.storedYFromVisualY(config, preview, desiredY - hudBounds.offsetY());
			config.setRelativePosition(anchorX, anchorY, width, height);
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (doneButton != null) {
			doneButton.mouseReleased(event);
		}
		if (event.button() == 0 || event.button() == 1) {
			dragging = false;
		}
		return super.mouseReleased(event);
	}

	private static int MthClamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void onClose() {
		applyAndReturn();
		if (minecraft != null) {
			minecraft.setScreenAndShow(parent);
		}
	}

	private void applyAndReturn() {
		if (onApply != null) {
			onApply.run();
		}
	}
}
