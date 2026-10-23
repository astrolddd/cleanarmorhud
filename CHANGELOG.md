# Changelog

## 2.1.10-mc26.2
- Optimised mod jar size and removed lag while opening/closing gui.
- Gui revamped.
- Layout related bug fixes.
 
## 2.1.9-mc26.2
- Upgraded CommonGUI to 1.0.31: eliminated `extractBlurredBackground` to remove opening animation blur lag, and updated version checker logic.

## 2.1.8-mc26.2
- deleted the excess font files causing too much file size

## 2.1.6-mc26.2
- Added durability warning sound plays counter (1-99), interval slider (0.1s - 10.0s), and preview test button.
- Gui makeover tried to give it a modern style.
- New features related to warning and bug fixes related to offhand/mainhand.

## 2.1.5-mc26.2
- Prioritized offhand slot (`-1`) immediately above the armor block when Main Hand Position is set to `Above`, placing the mainhand slot (`-2`) above the offhand slot in both Clean and Vanilla Style modes.

## 2.1.4-mc26.2
- Updated CommonGUI to 1.0.25: removed window opening scale/fade animation and duplicate per-frame background blur call so the GUI opens and closes instantaneously without blur glitching, while keeping all widget animations intact.

## 2.1.3-mc26.2
- Upgraded CommonGUI to 1.0.21:
  - Default GUI theme set to macOS Light (white frosted glass) and default transparency set to 80%.
  - Added "GUI" tab with "GUI Size" (deferred layout scaling from 70% to 130%) and "Transparency Level" (20% to 100% Opaque).
  - High-resolution procedural vector SDF icons, smooth instant modal exit transitions, and auto-hinter unlocked Quicksand typography.
  - Dynamic update pill button taking users to Modrinth when an update is available.
- Fixed Vanilla Style button animation: removed redundant `rebuildTabs()` call on toggle to restore smooth sliding animation.

## 2.1.2-mc26.2
- Integrated CommonGUI 1.0.7 with normal-weight section headers, light-colored subtitle descriptions, and clean typography.
- Replaced legacy outline renderer with VanillaHotbarRenderer for authentic pixel-perfect vanilla hotbar frame and slot rendering.
- Preserved HUD anchor invariant: armor slots remain anchored at origin 0 and never shift downward when items are held in hands (hand slots extend upward into negative offsets).
- Added item break warning sound cues when armor drops below critical durability threshold.
- Updated mod branding to CleanArmorHUD (`clean-armorhud`) and refreshed Mod Menu icon to square 256x256 format.
- Removed obsolete custom modern screen widgets.
