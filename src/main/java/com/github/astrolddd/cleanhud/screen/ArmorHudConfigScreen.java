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

public class ArmorHudConfigScreen extends BaseConfigScreen {
    private final ArmorHudConfig config;
    private ArmorHudConfig workingCopy;

    public ArmorHudConfigScreen() {
        this(null);
    }

    public ArmorHudConfigScreen(Screen parent) {
        super(Component.translatable("screen.cleanhud.config"), parent, "cleanhud", "cleanarmorhud", "CleanArmorHUD");
        this.config = ConfigManager.get();
        this.workingCopy = config.copy();
        UiTheme.setDarkMode(workingCopy.darkTheme);
        UiTheme.setTransparency(workingCopy.guiOpacity);
        this.guiScaleMultiplier = workingCopy.guiScale;
    }

    @Override
    protected void buildConfig() {
        ConfigTab layoutTab = addTab("Layout");

        ConfigSection displaySec = layoutTab.addSection("HUD Display & Alignment", "Visibility, scale, rotation, and spacing");
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

        ConfigSection bgSec = layoutTab.addSection("Background & Outline", "Slot appearance, opacity, and borders");
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

        bgSec.addToggle("Vanilla Style", workingCopy.isVanillaStyle(), v -> {
            workingCopy.setVanillaStyle(v);
            onSettingChanged();
        });

        ConfigSection posSec = layoutTab.addSection("HUD Position", "Drag and drop positioning on screen");
        posSec.addButton("Drag & Drop Reposition HUD", b -> openMoveHudScreen());

        ConfigTab durTab = addTab("Durability");

        ConfigSection readoutSec = durTab.addSection("Durability Readout", "Text format, scale, and numeric values");
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

        ConfigSection barSec = durTab.addSection("Durability Bar & Color", "Progress bars and dynamic coloring");
        barSec.addToggle("Show Durability Bar", workingCopy.showDurabilityBar, v -> {
            workingCopy.showDurabilityBar = v;
            onSettingChanged();
        });

        barSec.addToggle("Colored Text (Match Bar)", workingCopy.matchTextColorToBar, v -> {
            workingCopy.matchTextColorToBar = v;
            onSettingChanged();
        });

        ConfigTab handsTab = addTab("Hand Slots");

        ConfigSection mainhandSec = handsTab.addSection("Main Hand Slot", "Display held item in primary hand");
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

        ConfigSection offhandSec = handsTab.addSection("Off-Hand Slot", "Display shield or held secondary item");
        offhandSec.addToggle("Show Offhand", workingCopy.showOffhandItem, v -> {
            workingCopy.showOffhandItem = v;
            onSettingChanged();
        });

        ConfigTab warningsTab = addTab("Warnings");

        ConfigSection alertsSec = warningsTab.addSection("Durability Alerts", "Item break warnings and sound cues");
        alertsSec.addToggle("Durability Warning", workingCopy.durabilityWarning, v -> {
            workingCopy.durabilityWarning = v;
            onSettingChanged();
        });

        alertsSec.addToggle("Play Break Sound", workingCopy.playBreakSound, v -> {
            workingCopy.playBreakSound = v;
            onSettingChanged();
        });

        alertsSec.getEntries().add(new com.astrolddd.commongui.screen.ConfigEntry() {
            @Override
            public int getHeight() {
                return 23;
            }

            @Override
            public List<com.astrolddd.commongui.widget.Widget> createWidgets(int x, int y, int width) {
                int ctrlW = 52;
                int labelW = width - ctrlW - 8;
                com.astrolddd.commongui.widget.Widget labelWidget = new com.astrolddd.commongui.widget.Widget(x, y, labelW, 23) {
                    @Override
                    public void extractWidgetRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
                        net.minecraft.client.gui.Font f = net.minecraft.client.Minecraft.getInstance().font;
                        g.text(f, UiTheme.text("Sound Plays"), this.x, this.y + 5, UiTheme.TEXT_PRIMARY, false);
                    }
                };
                NumberStepperWidget stepper = new NumberStepperWidget(x + width - ctrlW, y + 2, ctrlW, 17, 1, 99, workingCopy.breakSoundCount, v -> {
                    workingCopy.breakSoundCount = v;
                    onSettingChanged();
                });
                return List.of(labelWidget, stepper);
            }
        });

        alertsSec.addSlider("Sound Interval", 0.1, 10.0, workingCopy.breakSoundInterval, 1.0,
            val -> String.format(Locale.ROOT, "%.1fs", val),
            val -> {
                workingCopy.breakSoundInterval = Math.round(val.floatValue() * 10.0f) / 10.0f;
                onSettingChanged();
            });

        alertsSec.getEntries().add(new com.astrolddd.commongui.screen.ConfigEntry() {
            @Override
            public int getHeight() {
                return 23;
            }

            @Override
            public List<com.astrolddd.commongui.widget.Widget> createWidgets(int x, int y, int width) {
                int btnW = 64;
                int labelW = width - btnW - 8;
                com.astrolddd.commongui.widget.Widget labelWidget = new com.astrolddd.commongui.widget.Widget(x, y, labelW, 23) {
                    @Override
                    public void extractWidgetRenderState(net.minecraft.client.gui.GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
                        net.minecraft.client.gui.Font f = net.minecraft.client.Minecraft.getInstance().font;
                        g.text(f, UiTheme.text("Test Sound"), this.x, this.y + 5, UiTheme.TEXT_PRIMARY, false);
                    }
                };
                com.astrolddd.commongui.widget.RoundedButton testBtn = new com.astrolddd.commongui.widget.RoundedButton(
                    x + width - btnW, y + 2, btnW, 17, Component.literal("Play"),
                    b -> com.github.astrolddd.cleanhud.CleanHudClient.triggerWarningSoundBurst(workingCopy.breakSoundCount, workingCopy.breakSoundInterval)
                );
                return List.of(labelWidget, testBtn);
            }
        });

        alertsSec.addSlider("Warning At", 1, 100, workingCopy.durabilityWarningThreshold, 20,
            val -> String.format(Locale.ROOT, "%.0f%%", val),
            val -> {
                workingCopy.durabilityWarningThreshold = (int) Math.round(val);
                onSettingChanged();
            });

        ConfigSection vanillaSec = warningsTab.addSection("Vanilla Integration", "Vanilla armor bar replacement settings");
        vanillaSec.addToggle("Hide Vanilla Armor Bar", workingCopy.hideVanillaArmorBar, v -> {
            workingCopy.hideVanillaArmorBar = v;
            onSettingChanged();
        });

        ConfigTab guiTab = addTab("GUI");

        ConfigSection windowSec = guiTab.addSection("Window Appearance", "Scale and transparency of the configuration window");

        windowSec.addSlider("GUI Size", 0.70, 1.30, workingCopy.guiScale, 1.00,
            val -> String.format(Locale.ROOT, "%.0f%%", val * 100),
            val -> {
                workingCopy.guiScale = val.floatValue();
                setGuiScaleMultiplier(workingCopy.guiScale);
                onSettingChanged();
            });

        windowSec.addSlider("Transparency Level", 0.20, 1.00, workingCopy.guiOpacity, 0.80,
            val -> val >= 0.99 ? "100% (Opaque)" : String.format(Locale.ROOT, "%.0f%%", val * 100),
            val -> {
                workingCopy.guiOpacity = val.floatValue();
                UiTheme.setTransparency(workingCopy.guiOpacity);
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
        UiTheme.setDarkMode(workingCopy.darkTheme);
        UiTheme.setTransparency(workingCopy.guiOpacity);
        setGuiScaleMultiplier(workingCopy.guiScale);
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
