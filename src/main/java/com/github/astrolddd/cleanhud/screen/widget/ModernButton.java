package com.github.astrolddd.cleanhud.screen.widget;

import com.github.astrolddd.cleanhud.config.GuiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ModernButton extends AbstractWidget {
	private final Consumer<ModernButton> onPress;
	private final Style style;

	public enum Style {
		DEFAULT,
		MUTED,
		PRIMARY
	}

	public ModernButton(int x, int y, int width, int height, Component message, Consumer<ModernButton> onPress) {
		this(x, y, width, height, message, onPress, Style.DEFAULT);
	}

	public ModernButton(int x, int y, int width, int height, Component message, Consumer<ModernButton> onPress, int accentColor) {
		this(x, y, width, height, message, onPress,
			accentColor == ModernUiColors.BUTTON_MUTED ? Style.MUTED : Style.DEFAULT);
	}

	public ModernButton(int x, int y, int width, int height, Component message, Consumer<ModernButton> onPress, Style style) {
		super(x, y, width, height, message);
		this.onPress = onPress;
		this.style = style;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		GuiTheme theme = ModernUiColors.currentTheme();
		int idle;
		int hover;
		int textColor;

		switch (style) {
			case PRIMARY -> {
				idle = ModernUiColors.getButtonPrimary(theme);
				hover = ModernUiColors.getButtonPrimaryHover(theme);
				textColor = ModernUiColors.TEXT_ON_LIGHT;
			}
			case MUTED -> {
				idle = ModernUiColors.getButtonIdle(theme);
				hover = ModernUiColors.getButtonHover(theme);
				textColor = isHovered() ? ModernUiColors.getAccentSoft(theme) : ModernUiColors.TEXT_DIM;
			}
			default -> {
				idle = ModernUiColors.getButtonIdle(theme);
				hover = ModernUiColors.getButtonHover(theme);
				textColor = ModernUiColors.TEXT;
			}
		}

		int background = isHovered() ? hover : idle;
		int border = isHovered() ? ModernUiColors.getAccentHover(theme) : ModernUiColors.getPanelBorder(theme);

		int cornerRadius = 6;
		if (isHovered()) {
			com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen.drawRoundedRect(graphics, getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, cornerRadius + 1, ModernUiColors.getAccentGlow(theme));
		}

		com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen.drawRoundedPanel(graphics, getX(), getY(), getWidth(), getHeight(), cornerRadius, background, border);

		Font font = Minecraft.getInstance().font;
		int textX = getX() + (getWidth() - font.width(getMessage())) / 2;
		int textY = getY() + (getHeight() - 8) / 2;
		graphics.text(font, getMessage(), textX, textY, textColor);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubled) {
		if (onPress != null) {
			onPress.accept(this);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narration) {
		defaultButtonNarrationText(narration);
	}
}



