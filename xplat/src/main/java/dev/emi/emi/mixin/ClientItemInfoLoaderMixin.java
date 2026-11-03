package dev.emi.emi.mixin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.emi.emi.registry.EmiTags;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(ClientItemInfoLoader.class)
public class ClientItemInfoLoaderMixin {

	// Tag models are exposed as item models of the same id, so they can be rendered through vanilla item rendering
	@Inject(at = @At("RETURN"), method = "scheduleLoad", cancellable = true)
	private static void scheduleLoad(ResourceManager manager, Executor executor, CallbackInfoReturnable<CompletableFuture<ClientItemInfoLoader.LoadedClientInfos>> info) {
		info.setReturnValue(info.getReturnValue().thenApply(infos -> {
			Map<Identifier, ClientItem> contents = new HashMap<>(infos.contents());
			EmiTags.registerTagModels(manager, id -> contents.putIfAbsent(id,
				new ClientItem(new CuboidItemModelWrapper.Unbaked(id, Optional.empty(), List.of()), ClientItem.Properties.DEFAULT)));
			return new ClientItemInfoLoader.LoadedClientInfos(contents);
		}));
	}
}
