package com.github.astrolddd.cleanhud.screen.widget;

import com.github.astrolddd.cleanhud.config.GuiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class ModernSlider extends AbstractWidget {
	private final double min;
	private final double max;
	private double value;
	private final Consumer<Double> onChange;
	private final String suffix;
	private boolean dragging;

	public ModernSlider(int x, int y, int width, int height, Component label, double min, double max, double initial, String suffix, Consumer<Double> onChange) {
		super(x, y, width, height, label);
		this.min = min;
		this.max = max;
		this.value = Mth.clamp(initial, min, max);
		this.suffix = suffix;
		this.onChange = onChange;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = Mth.clamp(value, min, max);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		GuiTheme theme = ModernUiColors.currentTheme();
		Font font = Minecraft.getInstance().font;
		String valueText = formatValue() + suffix;
		int labelColor = isHovered() || dragging ? ModernUiColors.getAccentSoft(theme) : ModernUiColors.TEXT_DIM;
		graphics.text(font, getMessage(), getX(), getY(), labelColor);
		graphics.text(font, valueText, getX() + getWidth() - font.width(valueText), getY(), ModernUiColors.TEXT_FAINT);

		int trackHeight = 4;
		int trackY = getY() + 14;
		com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen.drawRoundedRect(graphics, getX(), trackY, getWidth(), trackHeight, 2, ModernUiColors.SLIDER_TRACK);

		double ratio = (value - min) / (max - min);
		int fillWidth = Math.max(0, (int) Math.round(getWidth() * ratio));

		if (fillWidth > 0) {
			com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen.drawRoundedRect(graphics, getX(), trackY - 1, fillWidth, trackHeight + 2, 3, ModernUiColors.getAccentGlow(theme));
			com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen.drawRoundedRect(graphics, getX(), trackY, fillWidth, trackHeight, 2, ModernUiColors.getSliderFill(theme));
		}

		int knobSize = dragging || isHovered() ? 8 : 6;
		int knobX = getX() + fillWidth - knobSize / 2;
		int knobY = trackY + trackHeight / 2 - knobSize / 2;
		com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen.drawRoundedRect(graphics, knobX - 1, knobY - 1, knobSize + 2, knobSize + 2, (knobSize + 2) / 2, ModernUiColors.getAccentGlow(theme));
		com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen.drawRoundedRect(graphics, knobX, knobY, knobSize, knobSize, knobSize / 2, ModernUiColors.SLIDER_KNOB);
	}

	private String formatValue() {
		if (Math.abs(max - min) <= 10) {
			return String.format("%.1f", value);
		}
		return String.valueOf(Math.round(value));
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubled) {
		dragging = true;
		updateValue(event.x());
	}

	@Override
	protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
		if (dragging) {
			updateValue(event.x());
		}
	}

	@Override
	public void onRelease(MouseButtonEvent event) {
		dragging = false;
	}

	private void updateValue(double mouseX) {
		double ratio = Mth.clamp((mouseX - getX()) / getWidth(), 0, 1);
		value = min + ratio * (max - min);
		if (onChange != null) {
			onChange.accept(value);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narration) {
		defaultButtonNarrationText(narration);
	}
}



