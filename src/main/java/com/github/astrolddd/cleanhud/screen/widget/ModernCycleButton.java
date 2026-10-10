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
import java.util.function.Supplier;

public class ModernCycleButton extends AbstractWidget {
	private final Supplier<String> valueSupplier;
	private final Runnable onCycle;
	private final Consumer<String> onChange;

	public ModernCycleButton(int x, int y, int width, int height, Component label, Supplier<String> valueSupplier, Runnable onCycle, Consumer<String> onChange) {
		super(x, y, width, height, label);
		this.valueSupplier = valueSupplier;
		this.onCycle = onCycle;
		this.onChange = onChange;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		GuiTheme theme = ModernUiColors.currentTheme();
		Font font = Minecraft.getInstance().font;
		String value = valueSupplier.get();
		int labelColor = isHovered() ? ModernUiColors.getAccentSoft(theme) : ModernUiColors.TEXT_DIM;
		graphics.text(font, getMessage(), getX(), getY() + 4, labelColor);

		int boxWidth = Math.min(120, Math.max(80, getWidth() / 2));
		int boxX = getX() + getWidth() - boxWidth;
		int boxY = getY();
		int background = isHovered() ? ModernUiColors.getButtonHover(theme) : ModernUiColors.getButtonIdle(theme);
		int border = isHovered() ? ModernUiColors.getAccentHover(theme) : ModernUiColors.getPanelBorder(theme);
		com.github.astrolddd.cleanhud.screen.ArmorHudConfigScreen.drawRoundedPanel(graphics, boxX, boxY, boxWidth, getHeight(), 5, background, border);

		int textColor = isHovered() ? ModernUiColors.TEXT : ModernUiColors.TEXT_DIM;
		graphics.text(font, value, boxX + 8, boxY + (getHeight() - 8) / 2, textColor);

		int arrowX = boxX + boxWidth - 10;
		int arrowY = boxY + getHeight() / 2;
		int arrowColor = isHovered() ? ModernUiColors.getAccentSoft(theme) : ModernUiColors.TEXT_FAINT;
		graphics.fill(arrowX, arrowY - 2, arrowX + 1, arrowY - 1, arrowColor);
		graphics.fill(arrowX + 1, arrowY - 1, arrowX + 2, arrowY, arrowColor);
		graphics.fill(arrowX + 2, arrowY - 2, arrowX + 3, arrowY - 1, arrowColor);
	}

	@Override
	public void onClick(MouseButtonEvent event, boolean doubled) {
		onCycle.run();
		if (onChange != null) {
			onChange.accept(valueSupplier.get());
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narration) {
		defaultButtonNarrationText(narration);
	}
}



