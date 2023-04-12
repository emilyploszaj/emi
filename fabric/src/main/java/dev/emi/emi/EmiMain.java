package dev.emi.emi;

import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.PingS2CPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;

public class EmiMain implements ModInitializer {

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) -> EmiCommands.registerCommands(dispatcher));
		EmiNetwork.initServer((clazz, id, create) -> {
			ServerPlayNetworking.registerGlobalReceiver(id, (server, player, networkHandler, buf, sender) -> {
				EmiPacket packet = create.apply(player, buf);
				server.execute(() -> {
					packet.apply(player);
				});
			});
		}, (player, packet) -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			packet.write(buf);
			ServerPlayNetworking.send(player, packet.getId(), buf);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			EmiNetwork.sendToClient(handler.player, new PingS2CPacket());
		});
	}
}