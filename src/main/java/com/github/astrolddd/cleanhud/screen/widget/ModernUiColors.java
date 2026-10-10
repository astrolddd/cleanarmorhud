package com.github.astrolddd.cleanhud.screen.widget;

import com.github.astrolddd.cleanhud.config.ConfigManager;
import com.github.astrolddd.cleanhud.config.GuiTheme;

public final class ModernUiColors {
	// Base Constants
	public static final int SPECULAR = 0x44FFFFFF;
	public static final int PANEL_DIVIDER = 0x18FFFFFF;

	// Text Defaults
	public static final int TEXT = 0xFFF2F2F5;
	public static final int TEXT_DIM = 0xAAF2F2F5;
	public static final int TEXT_FAINT = 0x66F2F2F5;
	public static final int TEXT_ON_LIGHT = 0xFFFFFFFF;

	// Warning Colors
	public static final int WARNING = 0xFFE5484D;
	public static final int WARNING_GLOW = 0x40E5484D;

	// Default static fallback constants for legacy calls
	public static int BACKGROUND = 0x66000000;
	public static int PANEL = 0x7705050A;
	public static int PANEL_BORDER = 0x25FFFFFF;
	public static int ACCENT_SOFT = 0xFFFB923C;
	public static int BUTTON_IDLE = 0x1AFFFFFF;
	public static int BUTTON_HOVER = 0x33F97316;
	public static int BUTTON_MUTED = 0x12FFFFFF;
	public static int BUTTON_MUTED_HOVER = 0x25EA580C;
	public static int BUTTON_PRIMARY = 0xFFEA580C;
	public static int BUTTON_PRIMARY_HOVER = 0xFFF97316;
	public static int ORANGE_GLOW = 0x40F97316;
	public static int ORANGE_HOVER = 0xFFF97316;
	public static int ORANGE_LIGHT = 0xFFFB923C;
	public static int TOGGLE_ON = 0xFFEA580C;
	public static int TOGGLE_ON_GLOW = 0x40F97316;
	public static int TOGGLE_OFF = 0x20FFFFFF;
	public static int TOGGLE_KNOB_OFF = 0xCCAAAAB0;
	public static int SLIDER_TRACK = 0x20FFFFFF;
	public static int SLIDER_FILL = 0xFFF97316;
	public static int SLIDER_FILL_GLOW = 0x40EA580C;
	public static int SLIDER_KNOB = 0xFFFFFFFF;
	public static int CYCLE_PILL_IDLE = 0x1AFFFFFF;
	public static int CYCLE_PILL_HOVER = 0x33F97316;
	public static int ACCENT = 0xFFEEEEEE;

	private ModernUiColors() {
	}

	public static GuiTheme currentTheme() {
		return ConfigManager.get().guiTheme;
	}

	public static int getBackground(GuiTheme theme) {
		return switch (theme) {
			case DEFAULT -> 0xE6101018;
			case ORANGE_GLASS, BLUE_GLASS -> 0x66000000;
			case VANILLA -> 0x70000000;
		};
	}

	public static int getPanel(GuiTheme theme) {
		return switch (theme) {
			case DEFAULT -> 0xF0181824;
			case ORANGE_GLASS, BLUE_GLASS -> 0x7705050A;
			case VANILLA -> 0xEE2A2A2A;
		};
	}

	public static int getPanelBorder(GuiTheme theme) {
		return switch (theme) {
			case DEFAULT -> 0x336C63FF;
			case ORANGE_GLASS, BLUE_GLASS -> 0x25FFFFFF;
			case VANILLA -> 0xFF555555;
		};
	}

	public static int getAccentSoft(GuiTheme theme) {
		return switch (theme) {
			case DEFAULT -> 0xFFA59EFF;
			case ORANGE_GLASS -> 0xFFFB923C;
			case BLUE_GLASS -> 0xFF60A5FA;
			case VANILLA -> 0xFFE0E0E0;
		};
	}

	public static int getAccentGlow(GuiTheme theme) {
		return switch (theme) {
			case DEFAULT -> 0x406C63FF;
			case ORANGE_GLASS -> 0x40F97316;
			case BLUE_GLASS -> 0x403B82F6;
			case VANILLA -> 0x30FFFFFF;
		};
	}

	public static int getAccentHover(GuiTheme theme) {
		return switch (theme) {
			case DEFAULT -> 0xFF8F88FF;
			case ORANGE_GLASS -> 0xFFF97316;
			case BLUE_GLASS -> 0xFF3B82F6;
			case VANILLA -> 0xFFFFFFFF;
		};
	}

	public static int getButtonIdle(GuiTheme theme) {
		return switch (theme) {
			case DEFAULT -> 0x226C63FF;
			case ORANGE_GLASS, BLUE_GLASS -> 0x1AFFFFFF;
			case VANILLA -> 0xFF3C3C3C;
		};
	}

	public static int getButtonHover(GuiTheme theme) {
		return switch (theme) {
			case DEFAULT -> 0x446C63FF;
			case ORANGE_GLASS -> 0x33F97316;
			case BLUE_GLASS -> 0x333B82F6;
			case VANILLA -> 0xFF505050;
		};
	}

	public static int getButtonPrimary(GuiTheme theme) {
		return switch (theme) {
			case DEFAULT -> 0xFF6C63FF;
			case ORANGE_GLASS -> 0xFFEA580C;
			case BLUE_GLASS -> 0xFF2563EB;
			case VANILLA -> 0xFF4A4A4A;
		};
	}

	public static int getButtonPrimaryHover(GuiTheme theme) {
		return switch (theme) {
			case DEFAULT -> 0xFF8F88FF;
			case ORANGE_GLASS -> 0xFFF97316;
			case BLUE_GLASS -> 0xFF3B82F6;
			case VANILLA -> 0xFF5E5E5E;
		};
	}

	public static int getToggleOn(GuiTheme theme) {
		return switch (theme) {
			case DEFAULT -> 0xFF6C63FF;
			case ORANGE_GLASS -> 0xFFEA580C;
			case BLUE_GLASS -> 0xFF2563EB;
			case VANILLA -> 0xFF55FF55;
		};
	}

	public static int getSliderFill(GuiTheme theme) {
		return switch (theme) {
			case DEFAULT -> 0xFF8F88FF;
			case ORANGE_GLASS -> 0xFFF97316;
			case BLUE_GLASS -> 0xFF3B82F6;
			case VANILLA -> 0xFFAAAAAA;
		};
	}
}








