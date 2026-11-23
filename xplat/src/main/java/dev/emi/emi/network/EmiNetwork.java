package dev.emi.emi.network;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import dev.emi.emi.EmiPort;

public class EmiNetwork {
	public static final CustomPacketPayload.Type<FillRecipeC2SPacket> FILL_RECIPE = new CustomPacketPayload.Type<FillRecipeC2SPacket>(EmiPort.id("emi:fill_recipe"));
	public static final CustomPacketPayload.Type<CreateItemC2SPacket> CREATE_ITEM = new CustomPacketPayload.Type<CreateItemC2SPacket>(EmiPort.id("emi:create_item"));
	public static final CustomPacketPayload.Type<CommandS2CPacket> COMMAND = new CustomPacketPayload.Type<CommandS2CPacket>(EmiPort.id("emi:command"));
	public static final CustomPacketPayload.Type<EmiChessPacket> CHESS = new CustomPacketPayload.Type<EmiChessPacket>(EmiPort.id("emi:chess"));
	public static final CustomPacketPayload.Type<PingS2CPacket> PING = new CustomPacketPayload.Type<PingS2CPacket>(EmiPort.id("emi:ping"));
	private static BiConsumer<ServerPlayer, EmiPacket> clientSender;
	private static Consumer<EmiPacket> serverSender;

	public static void initServer(BiConsumer<ServerPlayer, EmiPacket> sender) {
		clientSender = sender;
	}

	public static void initClient(Consumer<EmiPacket> sender) {
		serverSender = sender;
	}

	public static void sendToClient(ServerPlayer player, EmiPacket packet) {
		clientSender.accept(player, packet);
	}

	public static void sendToServer(EmiPacket packet) {
		serverSender.accept(packet);
	}
}
