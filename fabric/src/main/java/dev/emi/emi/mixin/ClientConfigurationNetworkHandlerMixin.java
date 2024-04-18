package dev.emi.emi.mixin;

import dev.emi.emi.runtime.EmiReloadManager;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationNetworkHandler.class)
public class ClientConfigurationNetworkHandlerMixin {
    @Inject(at = @At("RETURN"), method = "onSynchronizeTags")
    private void refreshTagBasedData(CallbackInfo info) {
        EmiReloadManager.reloadTags();
    }
}
