package dev.emi.emi.mixin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.emi.emi.runtime.ProxyRecipeManager;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
	@Shadow
	public ClientLevel level;

	@Inject(at = @At("RETURN"), method = "reloadResourcePacks(ZLnet/minecraft/client/Minecraft$GameLoadCookie;)Ljava/util/concurrent/CompletableFuture;")
	private void reloadResources(boolean force, @Coerce Object loadingContext, CallbackInfoReturnable<CompletableFuture<Void>> info) {
		CompletableFuture<Void> future = info.getReturnValue();
		if (future != null) {
			future.thenRunAsync(() -> {
				if (ProxyRecipeManager.isAvailable()) {
					EmiReloadManager.reload();
				}
			}, Executors.newFixedThreadPool(1));
		}
	}

	@Inject(at = @At("HEAD"), method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V")
	private void disconnect(CallbackInfo info) {
		EmiLog.info("Disconnecting from server, EMI data cleared");
		EmiReloadManager.clear();
		EmiClient.onServer = false;
	}
}
