package com.github.astrolddd.cleanhud.mixin;

import com.github.astrolddd.cleanhud.CleanHudClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
	@Inject(method = "tick", at = @At("TAIL"))
	private void cleanhud$tick(CallbackInfo ci) {
		CleanHudClient.handleClientTick((Minecraft) (Object) this);
	}
}












