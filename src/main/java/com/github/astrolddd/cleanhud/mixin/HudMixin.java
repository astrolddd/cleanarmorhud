package com.github.astrolddd.cleanhud.mixin;

import com.github.astrolddd.cleanhud.CleanHudClient;
import com.github.astrolddd.cleanhud.config.ConfigManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void cleanhud$renderArmorHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		CleanHudClient.renderArmorHud(graphics, deltaTracker);
	}

	@Inject(method = "extractArmor", at = @At("HEAD"), cancellable = true, require = 0)
	private static void cleanhud$hideVanillaArmor(GuiGraphicsExtractor graphics, Player player, int i, int j, int k, int l, CallbackInfo ci) {
		if (ConfigManager.get() != null && ConfigManager.get().hideVanillaArmorBar) {
			ci.cancel();
		}
	}
}
