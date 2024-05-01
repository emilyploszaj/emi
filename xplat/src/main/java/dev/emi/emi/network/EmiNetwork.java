package dev.emi.emi.network;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class EmiNetwork {
	public static final Identifier FILL_RECIPE = new Identifier("emi:fill_recipe");
	public static final Identifier CREATE_ITEM = new Identifier("emi:create_item");
	public static final Identifier COMMAND = new Identifier("emi:command");
	public static final Identifier CHESS = new Identifier("emi:chess");
	public static final Identifier PING = new Identifier("emi:ping");
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
