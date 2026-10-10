package com.github.astrolddd.cleanhud.screen;

import com.astrolddd.commongui.screen.BaseConfigScreen;
import com.astrolddd.commongui.screen.ConfigSection;
import com.astrolddd.commongui.screen.ConfigTab;
import com.astrolddd.commongui.theme.UiTheme;
import com.astrolddd.commongui.widget.DropdownWidget.DropdownItem;
import com.github.astrolddd.cleanhud.config.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * Clean Armor HUD configuration screen.
 * Extends CommonGUI's {@link BaseConfigScreen} to eliminate modal boilerplate.
 */
public class ArmorHudConfigScreen extends BaseConfigScreen {
    private final ArmorHudConfig config;
    private ArmorHudConfig workingCopy;

    public ArmorHudConfigScreen() {
        this(null);
    }

    public ArmorHudConfigScreen(Screen parent) {
        super(Component.translatable("screen.cleanhud.config"), parent, "cleanhud", "Clean Armor HUD");
        this.config = ConfigManager.get();
        this.workingCopy = config.copy();
        UiTheme.setDarkMode(workingCopy.darkTheme);
    }

    @Override
    protected void buildConfig() {
        // Tab 1: Layout
        ConfigTab layoutTab = addTab("Layout");

        ConfigSection displaySec = layoutTab.addSection("HUD Display & Alignment");
        displaySec.addToggle("Enable Armor HUD", workingCopy.enabled, v -> {
            workingCopy.enabled = v;
            onSettingChanged();
        });

        var alignmentItems = List.of(
            new DropdownItem<>(HudAlignment.VERTICAL, "Vertical", null),
            new DropdownItem<>(HudAlignment.HORIZONTAL, "Horizontal", null)
        );
        displaySec.addDropdown("Orientation", alignmentItems, workingCopy.alignment, a -> {
            workingCopy.setAlignment(a);
            onSettingChanged();
            rebuildTabs();
        });

        displaySec.addSlider("HUD Scale", 0.5, 2.5, workingCopy.scale, 1.0,
            val -> String.format(Locale.ROOT, "%.2fx", val),
            val -> {
                workingCopy.scale = val.floatValue();
                onSettingChanged();
            });

        displaySec.addSlider("Rotation", 0, 360, workingCopy.rotation, 0,
            val -> String.format(Locale.ROOT, "%.0f deg", val),
            val -> {
                workingCopy.rotation = val.floatValue();
                onSettingChanged();
            });

        displaySec.addSlider("Slot Spacing", 0, 12, workingCopy.spacing, 1,
            val -> String.format(Locale.ROOT, "%.0f px", val),
            val -> {
                workingCopy.spacing = (int) Math.round(val);
                onSettingChanged();
            });

        displaySec.addSlider("Slot Padding", 0, 8, workingCopy.padding, 0,
            val -> String.format(Locale.ROOT, "%.0f px", val),
            val -> {
                workingCopy.padding = (int) Math.round(val);
                onSettingChanged();
            });

        ConfigSection bgSec = layoutTab.addSection("Background & Outline");
        bgSec.addToggle("Item Background", workingCopy.showBackground, v -> {
            workingCopy.showBackground = v;
            onSettingChanged();
        });

        bgSec.addSlider("Background Opacity", 0.0, 1.0, workingCopy.backgroundOpacity, 0.35,
            val -> String.format(Locale.ROOT, "%.0f%%", val * 100),
            val -> {
                workingCopy.backgroundOpacity = val.floatValue();
                onSettingChanged();
            });

        bgSec.addToggle("Outline Hotbar", workingCopy.showOutline, v -> {
            workingCopy.showOutline = v;
            if (v) {
                workingCopy.setAlignment(HudAlignment.HORIZONTAL);
            }
            onSettingChanged();
            rebuildTabs();
        });

        ConfigSection posSec = layoutTab.addSection("HUD Position");
        posSec.addButton("Drag & Drop Reposition HUD", b -> openMoveHudScreen());

        // Tab 2: Durability
        ConfigTab durTab = addTab("Durability");

        ConfigSection readoutSec = durTab.addSection("Durability Readout");
        var modeItems = List.of(
            new DropdownItem<>(DurabilityMode.PERCENTAGE, "Percentage (%)", null),
            new DropdownItem<>(DurabilityMode.NUMERIC, "Numeric (Current/Max)", null),
            new DropdownItem<>(DurabilityMode.NONE, "Off (Hidden)", null)
        );
        readoutSec.addDropdown("Durability Display", modeItems, workingCopy.durabilityMode, m -> {
            workingCopy.durabilityMode = m;
            onSettingChanged();
        });

        var posItems = List.of(
            new DropdownItem<>(DurabilityTextPosition.RIGHT, "Right", null),
            new DropdownItem<>(DurabilityTextPosition.LEFT, "Left", null),
            new DropdownItem<>(DurabilityTextPosition.BELOW, "Below", null)
        );
        readoutSec.addDropdown("Text Position", posItems, workingCopy.durabilityTextPosition, p -> {
            workingCopy.durabilityTextPosition = p;
            onSettingChanged();
        });

        readoutSec.addSlider("Text Scale", 0.5, 1.5, workingCopy.durabilityTextScale, 1.0,
            val -> String.format(Locale.ROOT, "%.2fx", val),
            val -> {
                workingCopy.durabilityTextScale = Math.round(val.floatValue() * 100.0f) / 100.0f;
                onSettingChanged();
            });

        ConfigSection barSec = durTab.addSection("Durability Bar & Color");
        barSec.addToggle("Show Durability Bar", workingCopy.showDurabilityBar, v -> {
            workingCopy.showDurabilityBar = v;
            onSettingChanged();
        });

        barSec.addToggle("Colored Text (Match Bar)", workingCopy.matchTextColorToBar, v -> {
            workingCopy.matchTextColorToBar = v;
            onSettingChanged();
        });

        // Tab 3: Hand Slots
        ConfigTab handsTab = addTab("Hand Slots");

        ConfigSection mainhandSec = handsTab.addSection("Main Hand Slot");
        mainhandSec.addToggle("Show Main Hand", workingCopy.showMainHandItem, v -> {
            workingCopy.showMainHandItem = v;
            onSettingChanged();
        });

        mainhandSec.addToggle("Tools & Weapons Only", workingCopy.mainHandToolsOnly, v -> {
            workingCopy.mainHandToolsOnly = v;
            onSettingChanged();
        });

        var handPosItems = List.of(
            new DropdownItem<>(MainHandPosition.ABOVE, "Above Armor", null),
            new DropdownItem<>(MainHandPosition.BELOW, "Below Armor", null)
        );
        mainhandSec.addDropdown("Main Hand Position", handPosItems, workingCopy.mainHandPosition, p -> {
            workingCopy.mainHandPosition = p;
            onSettingChanged();
        });

        ConfigSection offhandSec = handsTab.addSection("Off-Hand Slot");
        offhandSec.addToggle("Show Offhand", workingCopy.showOffhandItem, v -> {
            workingCopy.showOffhandItem = v;
            onSettingChanged();
        });

        // Tab 4: Warnings
        ConfigTab warningsTab = addTab("Warnings");

        ConfigSection alertsSec = warningsTab.addSection("Durability Alerts");
        alertsSec.addToggle("Durability Warning", workingCopy.durabilityWarning, v -> {
            workingCopy.durabilityWarning = v;
            onSettingChanged();
        });

        alertsSec.addSlider("Warning At", 1, 100, workingCopy.durabilityWarningThreshold, 20,
            val -> String.format(Locale.ROOT, "%.0f%%", val),
            val -> {
                workingCopy.durabilityWarningThreshold = (int) Math.round(val);
                onSettingChanged();
            });

        ConfigSection vanillaSec = warningsTab.addSection("Vanilla Integration");
        vanillaSec.addToggle("Hide Vanilla Armor Bar", workingCopy.hideVanillaArmorBar, v -> {
            workingCopy.hideVanillaArmorBar = v;
            onSettingChanged();
        });
    }

    private void openMoveHudScreen() {
        if (minecraft != null) {
            minecraft.setScreenAndShow(new HudRepositionScreen(this, workingCopy, () -> {
                config.apply(workingCopy);
                ConfigManager.save();
            }));
        }
    }

    @Override
    protected void onSave() {
        config.apply(workingCopy);
        ConfigManager.save();
    }

    @Override
    protected void onResetDefaults() {
        workingCopy = new ArmorHudConfig();
    }

    @Override
    protected void onThemeToggled(boolean isDark) {
        config.darkTheme = isDark;
        workingCopy.darkTheme = isDark;
        ConfigManager.save();
    }

    public static void drawRoundedRect(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int color) {
        com.astrolddd.commongui.draw.Draw.roundedRect(graphics, x, y, width, height, radius, color);
    }

    public static void drawRoundedPanel(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int x, int y, int width, int height, int radius, int background, int border) {
        com.astrolddd.commongui.draw.Draw.roundedRect(graphics, x, y, width, height, radius, background);
        com.astrolddd.commongui.draw.Draw.roundedOutline(graphics, x, y, width, height, radius, 1, border);
    }
}

