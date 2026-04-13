package dev.emi.emi.platform.fabric;

import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiMain;
import dev.emi.emi.registry.EmiCommands;
import dev.emi.emi.runtime.EmiLog;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class EmiMainFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		EmiMain.init();
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> EmiCommands.registerCommands(dispatcher));

		EmiNetwork.initServer(ServerPlayNetworking::send);

		registerPacketReader(EmiNetwork.FILL_RECIPE, FillRecipeC2SPacket::new);
		registerPacketReader(EmiNetwork.CREATE_ITEM, CreateItemC2SPacket::new);
		registerPacketReader(EmiNetwork.CHESS, EmiChessPacket.C2S::new);

		PayloadTypeRegistry.playS2C().register(EmiNetwork.PING, PacketCodec.ofStatic((buf, v) -> v.write(buf), PingS2CPacket::new));
		PayloadTypeRegistry.playS2C().register(EmiNetwork.COMMAND, PacketCodec.ofStatic((buf, v) -> v.write(buf), CommandS2CPacket::new));
		PayloadTypeRegistry.playS2C().register(EmiNetwork.CHESS, PacketCodec.ofStatic((buf, v) -> v.write(buf), EmiChessPacket.S2C::new));

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			EmiNetwork.sendToClient(handler.player, new PingS2CPacket());
		});

        // Run through vanilla recipe serializers and sync them
        for (var entry : Registries.RECIPE_SERIALIZER.getEntrySet()) {
            RegistryKey<RecipeSerializer<?>> resourceKey = entry.getKey();
            if (resourceKey.getValue().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
                RecipeSerializer<?> serializer = entry.getValue();
                try {
                    RecipeSynchronization.synchronizeRecipeSerializer(serializer);
                } catch (RuntimeException e) {
                    EmiLog.error("Failed to synchronize recipe serializer", e);
                }
            }
        }
	}

	private <T extends EmiPacket> void registerPacketReader(CustomPayload.Id<T> id, PacketDecoder<RegistryByteBuf, T> decode) {
		PayloadTypeRegistry.playC2S().register(id, PacketCodec.ofStatic((buf, v) -> v.write(buf), decode));
		ServerPlayNetworking.registerGlobalReceiver(id, (payload, context) -> {
			MinecraftClient.getInstance().getServer().execute(() -> {
				((EmiPacket) payload).apply(context.player());
			});
		});
	}
}