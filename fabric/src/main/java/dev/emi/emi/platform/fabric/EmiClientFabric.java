package dev.emi.emi.platform.fabric;

import dev.emi.emi.runtime.EmiReloadManager;
import dev.emi.emi.runtime.ProxyRecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;


import dev.emi.emi.data.EmiData;
import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.packs.PackType;

public class EmiClientFabric implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EmiClient.init();
		EmiData.init(reloader -> ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(reloader.getEmiId(), reloader));

		ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) -> {
			ProxyRecipeManager.setRecipes(RecipeMap.create(recipes.recipes()));
			EmiReloadManager.reloadRecipes();
		});

		EmiNetwork.initClient(packet -> {
			if (ClientPlayNetworking.canSend(packet.type())) {
				ClientPlayNetworking.send(packet);
			}
		});

		registerPacketReader(EmiNetwork.PING, PingS2CPacket::new);
		registerPacketReader(EmiNetwork.COMMAND, CommandS2CPacket::new);
		registerPacketReader(EmiNetwork.CHESS, EmiChessPacket.S2C::new);
	}

	private <T extends EmiPacket> void registerPacketReader(CustomPacketPayload.Type<T> id, StreamDecoder<RegistryFriendlyByteBuf, T> decode) {
		ClientPlayNetworking.registerGlobalReceiver(id, (payload, context) -> {
			context.client().execute(() -> {
				payload.apply(context.client().player);
			});
		});
	}
}
