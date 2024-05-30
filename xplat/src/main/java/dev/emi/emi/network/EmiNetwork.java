package dev.emi.emi.network;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EmiNetwork {
	public static final CustomPayload.Id<FillRecipeC2SPacket> FILL_RECIPE = new CustomPayload.Id<>(Identifier.of("emi:fill_recipe"));
	public static final CustomPayload.Id<CreateItemC2SPacket> CREATE_ITEM = new CustomPayload.Id<>(Identifier.of("emi:create_item"));
	public static final CustomPayload.Id<CommandS2CPacket> COMMAND = new CustomPayload.Id<>(Identifier.of("emi:command"));
	public static final CustomPayload.Id<EmiChessPacket> CHESS = new CustomPayload.Id<>(Identifier.of("emi:chess"));
	public static final CustomPayload.Id<PingS2CPacket> PING = new CustomPayload.Id<>(Identifier.of("emi:ping"));
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
