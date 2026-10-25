package com.github.astrolddd.cleanhud.screen;

import com.astrolddd.commongui.anim.Anim;
import com.astrolddd.commongui.draw.Draw;
import com.astrolddd.commongui.draw.Icons;
import com.astrolddd.commongui.theme.UiTheme;
import com.astrolddd.commongui.widget.Widget;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

import java.util.function.IntConsumer;

public class NumberStepperWidget extends Widget {
	private final int min;
	private final int max;
	private int value;
	private String text;
	private final IntConsumer onChange;
	private float focusProgress = 0.0f;
	private float boxHoverProgress = 0.0f;
	private float upHoverProgress = 0.0f;
	private float downHoverProgress = 0.0f;
	private float blinkTimer = 0.0f;
	private long lastNanos = 0L;

	public NumberStepperWidget(int x, int y, int width, int height, int min, int max, int initialValue, IntConsumer onChange) {
		super(x, y, width, height);
		this.min = min;
		this.max = max;
		this.value = Math.clamp(initialValue, min, max);
		this.text = String.valueOf(this.value);
		this.onChange = onChange;
	}

	private int arrowColWidth() {
		return 13;
	}

	private int boxWidth() {
		return width - arrowColWidth() - 2;
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		if (!visible) return;
		long now = System.nanoTime();
		float dt = lastNanos == 0L ? 0.016f : Math.clamp((now - lastNanos) / 1_000_000_000.0f, 0.001f, 0.1f);
		lastNanos = now;

		updateHover(mouseX, mouseY);
		int boxW = boxWidth();
		int arrowX = x + boxW + 2;
		int arrowW = arrowColWidth();
		int halfH = height / 2;

		boolean overBox = enabled && mouseX >= x && mouseX < x + boxW && mouseY >= y && mouseY < y + height;
		boolean overUp = enabled && mouseX >= arrowX && mouseX < arrowX + arrowW && mouseY >= y && mouseY < y + halfH;
		boolean overDown = enabled && mouseX >= arrowX && mouseX < arrowX + arrowW && mouseY >= y + halfH && mouseY < y + height;

		boxHoverProgress = Anim.damp(boxHoverProgress, overBox ? 1.0f : 0.0f, 16.0f, dt);
		upHoverProgress = Anim.damp(upHoverProgress, overUp ? 1.0f : 0.0f, 16.0f, dt);
		downHoverProgress = Anim.damp(downHoverProgress, overDown ? 1.0f : 0.0f, 16.0f, dt);
		focusProgress = Anim.damp(focusProgress, focused ? 1.0f : 0.0f, 18.0f, dt);
		blinkTimer += dt;

		int boxBg = Anim.lerpColor(UiTheme.CONTROL_BG, UiTheme.CONTROL_HOVER_BG, Math.max(boxHoverProgress * 0.5f, focusProgress));
		int boxBorder = Anim.lerpColor(UiTheme.BTN_BORDER, UiTheme.ACCENT, focusProgress);
		int radius = UiTheme.RADIUS_INPUT;

		Draw.roundedRect(g, x, y, boxW, height, radius, boxBg);
		Draw.roundedOutline(g, x, y, boxW, height, radius, 1, boxBorder);

		Font font = Minecraft.getInstance().font;
		String displayStr = focused ? text : String.valueOf(value);
		var comp = UiTheme.text(displayStr);
		int textW = font.width(comp);
		int textX = x + (boxW - textW) / 2;
		int textY = y + (height - 8) / 2;

		if (!displayStr.isEmpty()) {
			g.text(font, comp, textX, textY, UiTheme.TEXT_PRIMARY, false);
		}

		if (focused && ((int) (blinkTimer * 2.2f) % 2 == 0)) {
			int cx = displayStr.isEmpty() ? (x + boxW / 2) : (textX + textW + 1);
			g.fill(cx, textY - 1, cx + 1, textY + 8, UiTheme.ACCENT);
		}

		int upBg = Anim.lerpColor(UiTheme.BTN_IDLE, UiTheme.BTN_HOVER, upHoverProgress);
		int downBg = Anim.lerpColor(UiTheme.BTN_IDLE, UiTheme.BTN_HOVER, downHoverProgress);
		int btnH = (height - 1) / 2;

		Draw.roundedRect(g, arrowX, y, arrowW, btnH, 2, upBg);
		Draw.roundedOutline(g, arrowX, y, arrowW, btnH, 2, 1, UiTheme.BTN_BORDER);

		int downY = y + height - btnH;
		Draw.roundedRect(g, arrowX, downY, arrowW, btnH, 2, downBg);
		Draw.roundedOutline(g, arrowX, downY, arrowW, btnH, 2, 1, UiTheme.BTN_BORDER);

		int upIconColor = value < max ? Anim.lerpColor(UiTheme.TEXT_SECONDARY, UiTheme.TEXT_PRIMARY, upHoverProgress) : UiTheme.TEXT_MUTED;
		int downIconColor = value > min ? Anim.lerpColor(UiTheme.TEXT_SECONDARY, UiTheme.TEXT_PRIMARY, downHoverProgress) : UiTheme.TEXT_MUTED;

		Icons.draw(g, Icons.Icon.CHEVRON_UP, arrowX + (arrowW - 7) / 2, y + (btnH - 7) / 2, 7, upIconColor);
		Icons.draw(g, Icons.Icon.CHEVRON_DOWN, arrowX + (arrowW - 7) / 2, downY + (btnH - 7) / 2, 7, downIconColor);
	}

	private void commitEmptyIfNeeded() {
		if (text.isEmpty()) {
			value = min;
			text = String.valueOf(value);
			if (onChange != null) {
				onChange.accept(value);
			}
		}
	}

	private void stepValue(int delta) {
		int next = (int) Math.clamp((long) value + delta, (long) min, (long) max);
		if (next != value || !text.equals(String.valueOf(next))) {
			value = next;
			text = String.valueOf(value);
			if (onChange != null) {
				onChange.accept(value);
			}
		}
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (!visible || !enabled) return false;
		if (!isLeftClick(event)) return false;
		return handleMouseClick(event.x(), event.y());
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!visible || !enabled || button != 0) return false;
		return handleMouseClick(mouseX, mouseY);
	}

	private boolean handleMouseClick(double mouseX, double mouseY) {
		int boxW = boxWidth();
		int arrowX = x + boxW + 2;
		int arrowW = arrowColWidth();
		int halfH = height / 2;

		if (mouseX >= x && mouseX < x + boxW && mouseY >= y && mouseY < y + height) {
			focused = true;
			blinkTimer = 0.0f;
			return true;
		}

		if (mouseX >= arrowX && mouseX < arrowX + arrowW && mouseY >= y && mouseY < y + halfH) {
			focused = false;
			stepValue(1);
			return true;
		}

		if (mouseX >= arrowX && mouseX < arrowX + arrowW && mouseY >= y + halfH && mouseY < y + height) {
			focused = false;
			stepValue(-1);
			return true;
		}

		if (focused) {
			focused = false;
			commitEmptyIfNeeded();
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!visible || !enabled) return false;
		if (isMouseOver(mouseX, mouseY) && verticalAmount != 0) {
			stepValue(verticalAmount > 0 ? 1 : -1);
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event != null) {
			return keyPressed(event.key(), 0, event.modifiers());
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!visible || !enabled || !focused) return false;

		if (keyCode == InputConstants.KEY_BACKSPACE || keyCode == InputConstants.KEY_DELETE) {
			if (!text.isEmpty()) {
				text = text.substring(0, text.length() - 1);
				if (!text.isEmpty()) {
					int parsed = Math.clamp(Integer.parseInt(text), min, max);
					value = parsed;
					if (onChange != null) {
						onChange.accept(value);
					}
				}
			}
			return true;
		}
		if (keyCode == InputConstants.KEY_UP) {
			stepValue(1);
			return true;
		}
		if (keyCode == InputConstants.KEY_DOWN) {
			stepValue(-1);
			return true;
		}
		if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
			focused = false;
			commitEmptyIfNeeded();
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (event != null) {
			return charTyped((char) event.codepoint(), 0);
		}
		return false;
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (!visible || !enabled || !focused) return false;
		if (codePoint >= '0' && codePoint <= '9' && text.length() < 2) {
			String next = text + codePoint;
			try {
				int parsed = Integer.parseInt(next);
				if (parsed >= min && parsed <= max) {
					text = String.valueOf(parsed);
					value = parsed;
					if (onChange != null) {
						onChange.accept(value);
					}
					return true;
				}
			} catch (NumberFormatException ignored) {
			}
		}
		return false;
	}
}
