package dev.emi.emi.network;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
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

	public static void initServer(PacketRegister server, BiConsumer<ServerPlayerEntity, EmiPacket> sender) {
		clientSender = sender;
		server.register(FillRecipeC2SPacket.class, FILL_RECIPE, FillRecipeC2SPacket::new);
		server.registerSimple(CreateItemC2SPacket.class, CREATE_ITEM, CreateItemC2SPacket::new);
		server.registerSimple(EmiChessPacket.C2S.class, CHESS, EmiChessPacket.C2S::new);
	}

	public static void initClient(PacketRegister client, Consumer<EmiPacket> sender) {
		serverSender = sender;
		client.registerSimple(PingS2CPacket.class, PING, PingS2CPacket::new);
		client.registerSimple(CommandS2CPacket.class, COMMAND, CommandS2CPacket::new);
		client.registerSimple(EmiChessPacket.S2C.class, CHESS, EmiChessPacket.S2C::new);
	}

	public static void sendToClient(ServerPlayerEntity player, EmiPacket packet) {
		clientSender.accept(player, packet);
	}

	public static void sendToServer(EmiPacket packet) {
		serverSender.accept(packet);
	}

	public static interface PacketRegister {

		void register(Class<? extends EmiPacket> clazz, Identifier id, BiFunction<PlayerEntity, PacketByteBuf, EmiPacket> create);

		default void registerSimple(Class<? extends EmiPacket> clazz, Identifier id, Function<PacketByteBuf, EmiPacket> create) {
			register(clazz, id, (player, buf) -> create.apply(buf));
		}
	}
}
