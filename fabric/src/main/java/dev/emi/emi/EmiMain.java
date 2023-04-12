package dev.emi.emi;

import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.RecipeFillC2SPacket;
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
		EmiNetwork.initServer((player, packet) -> {
			PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
			packet.write(buf);
			ServerPlayNetworking.send(player, packet.getId(), buf);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			sender.sendPacket(EmiNetwork.PING, new PacketByteBuf(Unpooled.EMPTY_BUFFER));
		});

		ServerPlayNetworking.registerGlobalReceiver(EmiNetwork.FILL_RECIPE, (server, player, networkHandler, buf, sender) -> {
			EmiPacket packet = new RecipeFillC2SPacket(player, buf);
			server.execute(() -> {
				packet.apply(player);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(EmiNetwork.CREATE_ITEM, (server, player, networkHandler, buf, sender) -> {
			EmiPacket packet = new CreateItemC2SPacket(buf);
			server.execute(() -> {
				packet.apply(player);
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(EmiNetwork.CHESS, (server, player, networkHandler, buf, sender) -> {
			EmiPacket packet = new EmiChessPacket.S2C(buf);
			server.execute(() -> {
				packet.apply(player);
			});
		});
	}
}