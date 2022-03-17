package dev.emi.emi.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.EmiReloadManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Unique
	private boolean firstInfo = true;

	@Inject(at = @At("RETURN"), method = "onSynchronizeRecipes")
	public void onSynchronizeRecipes(SynchronizeRecipesS2CPacket packet, CallbackInfo info) {
		if (!firstInfo) {
			EmiReloadManager.reload();
		}
		firstInfo = false;
	}

	@Inject(at = @At("RETURN"), method = "onSynchronizeTags")
	public void onSynchronizeTags(SynchronizeTagsS2CPacket packet, CallbackInfo info) {
		if (!firstInfo) {
			EmiReloadManager.reload();
		}
		firstInfo = false;
	}
}
