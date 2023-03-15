package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.EmiClient;
import dev.emi.emi.EmiLog;
import dev.emi.emi.EmiReloadManager;
import net.minecraft.client.MinecraftClientGame;

@Mixin(MinecraftClientGame.class)
public class MinecraftClientGameMixin {
	
	@Inject(at = @At("HEAD"), method = "onStartGameSession")
	public void onStartGameSession(CallbackInfo info) {
		EmiLog.info("Joining server, EMI waiting for data from server...");
	}
	
	@Inject(at = @At("HEAD"), method = "onLeaveGameSession")
	public void onLeaveGameSession(CallbackInfo info) {
		EmiLog.info("Disconnecting from server, EMI data cleared");
		EmiReloadManager.clear();
		EmiClient.onServer = false;
	}
}
