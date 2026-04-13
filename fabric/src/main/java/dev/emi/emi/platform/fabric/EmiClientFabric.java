package dev.emi.emi.platform.fabric;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import dev.emi.emi.data.EmiData;
import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.runtime.EmiReloadManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.jspecify.annotations.NonNull;

public class EmiClientFabric implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EmiClient.init();
		EmiData.init(reloader -> {
			ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {

                @Override
                public CompletableFuture<Void> reload(Store store, Executor prepareExecutor,
                                                      Synchronizer reloadSynchronizer, Executor applyExecutor) {
                    return reloader.reload(store, prepareExecutor, reloadSynchronizer, applyExecutor);
                }

                @Override
				public String getName() {
					return reloader.getName();
				}

				@Override
				public @NonNull Identifier getFabricId() {
					return reloader.getEmiId();
				}
			});
		});

        ClientRecipeSynchronizedEvent.EVENT.register((minecraft, synchronizedRecipes) -> {
            EmiReloadManager.reloadRecipes();
        });

//		PreparableModelLoadingPlugin.<List<Identifier>>register((manager, executor) -> {
//			return CompletableFuture.supplyAsync(() -> {
//				List<Identifier> ids = Lists.newArrayList();
//				EmiTags.registerTagModels(manager, id -> ids.add(id.id()), "");
//				return ids;
//			}, executor);
//		}, (ids, context) -> {
//			context.addModels(ids);
//		}); TODO

		EmiNetwork.initClient(packet -> {
			if (ClientPlayNetworking.canSend(packet.getId())) {
				ClientPlayNetworking.send(packet);
			}
		});

		registerPacketReader(EmiNetwork.PING, PingS2CPacket::new);
		registerPacketReader(EmiNetwork.COMMAND, CommandS2CPacket::new);
		registerPacketReader(EmiNetwork.CHESS, EmiChessPacket.S2C::new);
	}

	private <T extends EmiPacket> void registerPacketReader(CustomPayload.Id<T> id, PacketDecoder<RegistryByteBuf, T> decode) {
		ClientPlayNetworking.registerGlobalReceiver(id, (payload, context) -> {
			context.client().execute(() -> {
				payload.apply(context.client().player);
			});
		});
	}
}
