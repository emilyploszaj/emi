package dev.emi.emi.network;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EmiNetwork {
	// TODO
	public static final CustomPayload.Id<FillRecipeC2SPacket> FILL_RECIPE = CustomPayload.id("emi/fill_recipe");
	public static final CustomPayload.Id<CreateItemC2SPacket> CREATE_ITEM = CustomPayload.id("emi/create_item");
	public static final CustomPayload.Id<CommandS2CPacket> COMMAND = CustomPayload.id("emi/command");
	public static final CustomPayload.Id<EmiChessPacket> CHESS = CustomPayload.id("emi/chess");
	public static final CustomPayload.Id<PingS2CPacket> PING = CustomPayload.id("emi/ping");
	private static BiConsumer<ServerPlayerEntity, EmiPacket> clientSender;
	private static Consumer<EmiPacket> serverSender;

	public static void initServer(BiConsumer<ServerPlayerEntity, EmiPacket> sender) {
		clientSender = sender;
	}

	public static void initClient(Consumer<EmiPacket> sender) {
		serverSender = sender;
	}

	public static void sendToClient(ServerPlayerEntity player, EmiPacket packet) {
		clientSender.accept(player, packet);
	}

	public static void sendToServer(EmiPacket packet) {
		serverSender.accept(packet);
	}
}
