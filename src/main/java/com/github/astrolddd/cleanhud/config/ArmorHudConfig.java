package com.github.astrolddd.cleanhud.config;

public class ArmorHudConfig {
	public boolean enabled = true;
	public float posX = 0.02f;
	public float posY = 0.55f;
	public boolean useRelativePosition = true;
	public float scale = 1.0f;
	public float rotation = 0.0f;
	public DurabilityMode durabilityMode = DurabilityMode.PERCENTAGE;
	public DurabilityTextPosition durabilityTextPosition = DurabilityTextPosition.RIGHT;
	public float durabilityTextScale = 1.0f;
	public boolean matchTextColorToBar = false;
	public boolean showDurabilityBar = true;
	public boolean showOffhandItem = true;
	public boolean showMainHandItem = true;
	public boolean mainHandToolsOnly = false;
	public MainHandPosition mainHandPosition = MainHandPosition.ABOVE;
	public boolean showBackground = true;
	public float backgroundOpacity = 0.35f;
	public boolean showOutline = false;
	public int spacing = 1;
	public int padding = 0;
	public HudAlignment alignment = HudAlignment.VERTICAL;
	public boolean hideVanillaArmorBar = false;
	public boolean durabilityWarning = false;
	public int durabilityWarningThreshold = 20;
	public GuiTheme guiTheme = GuiTheme.DEFAULT;
	public boolean darkTheme = true;

	public int savedVerticalSpacing = 1;
	public DurabilityTextPosition savedVerticalTextPosition = DurabilityTextPosition.RIGHT;

	public void setAlignment(HudAlignment newAlignment) {
		if (this.alignment == newAlignment) {
			return;
		}
		if (newAlignment == HudAlignment.HORIZONTAL) {
			this.savedVerticalSpacing = this.spacing;
			this.savedVerticalTextPosition = this.durabilityTextPosition;
			this.alignment = HudAlignment.HORIZONTAL;
			this.spacing = 5;
			this.durabilityTextPosition = DurabilityTextPosition.BELOW;
		} else {
			this.alignment = HudAlignment.VERTICAL;
			this.spacing = this.savedVerticalSpacing;
			this.durabilityTextPosition = this.savedVerticalTextPosition != null ? this.savedVerticalTextPosition : DurabilityTextPosition.RIGHT;
			this.showOutline = false;
		}
	}

	public ArmorHudConfig copy() {
		ArmorHudConfig copy = new ArmorHudConfig();
		copy.enabled = enabled;
		copy.posX = posX;
		copy.posY = posY;
		copy.useRelativePosition = useRelativePosition;
		copy.scale = scale;
		copy.rotation = rotation;
		copy.durabilityMode = durabilityMode;
		copy.durabilityTextPosition = durabilityTextPosition;
		copy.durabilityTextScale = durabilityTextScale;
		copy.matchTextColorToBar = matchTextColorToBar;
		copy.showDurabilityBar = showDurabilityBar;
		copy.showOffhandItem = showOffhandItem;
		copy.showMainHandItem = showMainHandItem;
		copy.mainHandToolsOnly = mainHandToolsOnly;
		copy.mainHandPosition = mainHandPosition;
		copy.showBackground = showBackground;
		copy.backgroundOpacity = backgroundOpacity;
		copy.showOutline = showOutline;
		copy.spacing = spacing;
		copy.padding = padding;
		copy.alignment = alignment;
		copy.hideVanillaArmorBar = hideVanillaArmorBar;
		copy.durabilityWarning = durabilityWarning;
		copy.durabilityWarningThreshold = durabilityWarningThreshold;
		copy.guiTheme = guiTheme;
		copy.darkTheme = darkTheme;
		copy.savedVerticalSpacing = savedVerticalSpacing;
		copy.savedVerticalTextPosition = savedVerticalTextPosition;
		return copy;
	}

	public void apply(ArmorHudConfig other) {
		if (other == null) return;
		this.enabled = other.enabled;
		this.posX = other.posX;
		this.posY = other.posY;
		this.useRelativePosition = other.useRelativePosition;
		this.scale = other.scale;
		this.rotation = other.rotation;
		this.durabilityMode = other.durabilityMode;
		this.durabilityTextPosition = other.durabilityTextPosition;
		this.durabilityTextScale = other.durabilityTextScale;
		this.matchTextColorToBar = other.matchTextColorToBar;
		this.showDurabilityBar = other.showDurabilityBar;
		this.showOffhandItem = other.showOffhandItem;
		this.showMainHandItem = other.showMainHandItem;
		this.mainHandToolsOnly = other.mainHandToolsOnly;
		this.mainHandPosition = other.mainHandPosition;
		this.showBackground = other.showBackground;
		this.backgroundOpacity = other.backgroundOpacity;
		this.showOutline = other.showOutline;
		this.spacing = other.spacing;
		this.padding = other.padding;
		this.alignment = other.alignment;
		this.hideVanillaArmorBar = other.hideVanillaArmorBar;
		this.durabilityWarning = other.durabilityWarning;
		this.durabilityWarningThreshold = other.durabilityWarningThreshold;
		this.guiTheme = other.guiTheme;
		this.darkTheme = other.darkTheme;
		this.savedVerticalSpacing = other.savedVerticalSpacing;
		this.savedVerticalTextPosition = other.savedVerticalTextPosition;
	}

	public int resolveX(int screenWidth) {
		if (useRelativePosition) {
			return Math.round(posX * screenWidth);
		}
		return Math.round(posX);
	}

	public int resolveY(int screenHeight) {
		if (useRelativePosition) {
			return Math.round(posY * screenHeight);
		}
		return Math.round(posY);
	}

	public void setRelativePosition(int x, int y, int screenWidth, int screenHeight) {
		useRelativePosition = true;
		posX = screenWidth > 0 ? (float) x / screenWidth : 0;
		posY = screenHeight > 0 ? (float) y / screenHeight : 0;
	}

	public void validate() {
		scale = clamp(scale, 0.5f, 2.5f);
		rotation = rotation % 360.0f;
		if (rotation < 0.0f) {
			rotation += 360.0f;
		}
		spacing = clamp(spacing, 0, 12);
		padding = clamp(padding, 0, 8);
		backgroundOpacity = clamp(backgroundOpacity, 0.0f, 1.0f);
		durabilityWarningThreshold = clamp(durabilityWarningThreshold, 1, 100);
		if (durabilityMode == null) {
			durabilityMode = DurabilityMode.PERCENTAGE;
		}
		if (durabilityTextPosition == null) {
			durabilityTextPosition = DurabilityTextPosition.RIGHT;
		}
		durabilityTextScale = clamp(durabilityTextScale, 0.5f, 1.5f);
		if (alignment == null) {
			alignment = HudAlignment.VERTICAL;
		}
		savedVerticalSpacing = clamp(savedVerticalSpacing, 0, 12);
		if (savedVerticalTextPosition == null) {
			savedVerticalTextPosition = DurabilityTextPosition.RIGHT;
		}
		if (mainHandPosition == null) {
			mainHandPosition = MainHandPosition.ABOVE;
		}
		if (guiTheme == null) {
			guiTheme = GuiTheme.DEFAULT;
		}
		if (useRelativePosition) {
			posX = clamp(posX, 0.0f, 1.0f);
			posY = clamp(posY, 0.0f, 1.0f);
		}
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		ArmorHudConfig other = (ArmorHudConfig) obj;
		return enabled == other.enabled &&
			Float.compare(other.posX, posX) == 0 &&
			Float.compare(other.posY, posY) == 0 &&
			useRelativePosition == other.useRelativePosition &&
			Float.compare(other.scale, scale) == 0 &&
			Float.compare(other.rotation, rotation) == 0 &&
			durabilityMode == other.durabilityMode &&
			durabilityTextPosition == other.durabilityTextPosition &&
			Float.compare(other.durabilityTextScale, durabilityTextScale) == 0 &&
			matchTextColorToBar == other.matchTextColorToBar &&
			showDurabilityBar == other.showDurabilityBar &&
			showOffhandItem == other.showOffhandItem &&
			showMainHandItem == other.showMainHandItem &&
			mainHandToolsOnly == other.mainHandToolsOnly &&
			mainHandPosition == other.mainHandPosition &&
			showBackground == other.showBackground &&
			Float.compare(other.backgroundOpacity, backgroundOpacity) == 0 &&
			showOutline == other.showOutline &&
			spacing == other.spacing &&
			padding == other.padding &&
			alignment == other.alignment &&
			hideVanillaArmorBar == other.hideVanillaArmorBar &&
			durabilityWarning == other.durabilityWarning &&
			durabilityWarningThreshold == other.durabilityWarningThreshold &&
			guiTheme == other.guiTheme &&
			darkTheme == other.darkTheme;
	}
}


