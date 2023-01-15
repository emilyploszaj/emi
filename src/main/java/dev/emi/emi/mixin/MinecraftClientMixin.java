package dev.emi.emi.mixin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.emi.EmiReloadManager;
import net.minecraft.client.MinecraftClient;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Inject(at = @At("RETURN"), method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;")
	private void reloadResources(boolean force, CallbackInfoReturnable<CompletableFuture<Void>> info) {
		CompletableFuture<Void> future = info.getReturnValue();
		if (future != null) {
			future.thenRunAsync(() -> {
				if (EmiReloadManager.receivedInitialData) {
					EmiReloadManager.reload();
				}
			}, Executors.newFixedThreadPool(1));
		}
	}
}
