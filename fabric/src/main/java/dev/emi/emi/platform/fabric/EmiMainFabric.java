package dev.emi.emi.platform.fabric;

import dev.emi.emi.runtime.EmiLog;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;

import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiMain;
import dev.emi.emi.registry.EmiCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class EmiMainFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		EmiMain.init();
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> EmiCommands.registerCommands(dispatcher));

		EmiNetwork.initServer(ServerPlayNetworking::send);

		registerPacketReader(EmiNetwork.FILL_RECIPE, FillRecipeC2SPacket::new);
		registerPacketReader(EmiNetwork.CREATE_ITEM, CreateItemC2SPacket::new);
		registerPacketReader(EmiNetwork.CHESS, EmiChessPacket.C2S::new);

		PayloadTypeRegistry.clientboundPlay().register(EmiNetwork.PING, StreamCodec.of((buf, v) -> v.write(buf), PingS2CPacket::new));
		PayloadTypeRegistry.clientboundPlay().register(EmiNetwork.COMMAND, StreamCodec.of((buf, v) -> v.write(buf), CommandS2CPacket::new));
		PayloadTypeRegistry.clientboundPlay().register(EmiNetwork.CHESS, StreamCodec.of((buf, v) -> v.write(buf), EmiChessPacket.S2C::new));

		// Vanilla no longer sends recipes to clients, sync every serializer so EMI can see all recipes
		for (RecipeSerializer<?> serializer : BuiltInRegistries.RECIPE_SERIALIZER) {
			synchronizeRecipes(serializer);
		}
		RegistryEntryAddedCallback.event(BuiltInRegistries.RECIPE_SERIALIZER).register((rawId, id, serializer) -> synchronizeRecipes(serializer));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			EmiNetwork.sendToClient(handler.player, new PingS2CPacket());
		});
	}

	private static void synchronizeRecipes(RecipeSerializer<?> serializer) {
		try {
			RecipeSynchronization.synchronizeRecipeSerializer(serializer);
		} catch (Throwable t) {
			EmiLog.error("Unable to synchronize recipe serializer " + BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer), t);
		}
	}

	private <T extends EmiPacket> void registerPacketReader(CustomPacketPayload.Type<T> id, StreamDecoder<RegistryFriendlyByteBuf, T> decode) {
		PayloadTypeRegistry.serverboundPlay().register(id, StreamCodec.of((buf, v) -> v.write(buf), decode));
		ServerPlayNetworking.registerGlobalReceiver(id, (payload, context) -> {
			context.server().execute(() -> {
				((EmiPacket)payload).apply(context.player());
			});
		});
	}
}