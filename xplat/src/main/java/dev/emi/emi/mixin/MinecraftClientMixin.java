package dev.emi.emi.mixin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.runtime.EmiLog;
import dev.emi.emi.runtime.EmiReloadManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow
	public ClientWorld world;

	@Inject(at = @At("RETURN"), method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;")
	private void reloadResources(boolean force, CallbackInfoReturnable<CompletableFuture<Void>> info) {
		CompletableFuture<Void> future = info.getReturnValue();
		if (future != null) {
			future.thenRunAsync(() -> {
				MinecraftClient client = MinecraftClient.getInstance();
				if (client.world != null && client.world.getRecipeManager() != null) {
					EmiReloadManager.reload();
				}
			}, Executors.newFixedThreadPool(1));
		}
	}

	@Inject(at = @At("HEAD"), method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V")
	private void disconnect(CallbackInfo info) {
		EmiLog.info("Disconnecting from server, EMI data cleared");
		EmiReloadManager.clear();
		EmiClient.onServer = false;
	}
}
