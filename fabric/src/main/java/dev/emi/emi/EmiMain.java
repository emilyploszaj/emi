package dev.emi.emi;

import java.util.function.Function;

import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class EmiMain implements ModInitializer {

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> EmiCommands.registerCommands(dispatcher));

		EmiNetwork.initServer((player, packet) -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			packet.write(buf);
			ServerPlayNetworking.send(player, packet.getId(), buf);
		});

		registerPacketReader(EmiNetwork.FILL_RECIPE, FillRecipeC2SPacket::new);
		registerPacketReader(EmiNetwork.CREATE_ITEM, CreateItemC2SPacket::new);
		registerPacketReader(EmiNetwork.CHESS, EmiChessPacket.C2S::new);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			EmiNetwork.sendToClient(handler.player, new PingS2CPacket());
		});
	}

	private void registerPacketReader(Identifier id, Function<PacketByteBuf, EmiPacket> create) {
		ServerPlayNetworking.registerGlobalReceiver(id, (server, player, networkHandler, buf, sender) -> {
			EmiPacket packet = create.apply(buf);
			server.execute(() -> {
				packet.apply(player);
			});
		});
	}
}