package dev.emi.emi.network;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dev.emi.emi.EmiPort;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public class EmiNetwork {
	public static final CustomPayload.Id<FillRecipeC2SPacket> FILL_RECIPE = new CustomPayload.Id<FillRecipeC2SPacket>(EmiPort.id("emi:fill_recipe"));
	public static final CustomPayload.Id<CreateItemC2SPacket> CREATE_ITEM = new CustomPayload.Id<CreateItemC2SPacket>(EmiPort.id("emi:create_item"));
	public static final CustomPayload.Id<CommandS2CPacket> COMMAND = new CustomPayload.Id<CommandS2CPacket>(EmiPort.id("emi:command"));
	public static final CustomPayload.Id<EmiChessPacket> CHESS = new CustomPayload.Id<EmiChessPacket>(EmiPort.id("emi:chess"));
	public static final CustomPayload.Id<PingS2CPacket> PING = new CustomPayload.Id<PingS2CPacket>(EmiPort.id("emi:ping"));
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
