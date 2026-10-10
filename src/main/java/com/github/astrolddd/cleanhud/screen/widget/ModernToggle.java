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

public class ModernToggle extends AbstractWidget {
	private boolean value;
	private final Consumer<Boolean> onChange;

	public ModernToggle(int x, int y, int width, int height, Component label, boolean initial, Consumer<Boolean> onChange) {
		super(x, y, width, height, label);
		this.value = initial;
		this.onChange = onChange;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		GuiTheme theme = ModernUiColors.currentTheme();
		Font font = Minecraft.getInstance().font;
		int labelColor = isHovered() ? ModernUiColors.getAccentSoft(theme) : ModernUiColors.TEXT_DIM;
		graphics.text(font, getMessage(), getX(), getY() + 4, labelColor);

		int trackWidth = 32;
		int trackHeight = 14;
		int toggleX = getX() + getWidth() - trackWidth - 2;
		int toggleY = getY() + (getHeight() - trackHeight) / 2;

		if (value) {
			com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen.drawRoundedRect(graphics, toggleX - 2, toggleY - 2, trackWidth + 4, trackHeight + 4, 9, ModernUiColors.getAccentGlow(theme));
		}

		int trackColor = value ? ModernUiColors.getToggleOn(theme) : ModernUiColors.TOGGLE_OFF;
		int border = isHovered() ? ModernUiColors.getAccentHover(theme) : ModernUiColors.getPanelBorder(theme);
		com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen.drawRoundedPanel(graphics, toggleX, toggleY, trackWidth, trackHeight, 7, trackColor, border);

		int knobSize = 10;
		int knobX = value ? toggleX + trackWidth - knobSize - 2 : toggleX + 2;
		int knobY = toggleY + (trackHeight - knobSize) / 2;
		int knobColor = value ? 0xFFFFFFFF : ModernUiColors.TOGGLE_KNOB_OFF;
		com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen.drawRoundedRect(graphics, knobX, knobY, knobSize, knobSize, 5, knobColor);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubled) {
		value = !value;
		if (onChange != null) {
			onChange.accept(value);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narration) {
		defaultButtonNarrationText(narration);
	}
}



