package dev.emi.emi.platform.neoforge;

import java.util.function.BiConsumer;

import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.simple.MessageFunctions;
import net.neoforged.neoforge.network.simple.SimpleChannel;

public class EmiPacketHandler {
	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(new Identifier("emi:emi"))
		.networkProtocolVersion(() -> "1")
		.clientAcceptedVersions((version) -> true)
		.serverAcceptedVersions((version) -> true)
		.simpleChannel();

	public static void init() {
		int i = 0;
		CHANNEL.messageBuilder(FillRecipeC2SPacket.class, i++, PlayNetworkDirection.PLAY_TO_SERVER)
			.encoder(FillRecipeC2SPacket::write).decoder(FillRecipeC2SPacket::new)
			.consumerMainThread(serverHandler(FillRecipeC2SPacket::apply)).add();
		CHANNEL.messageBuilder(CreateItemC2SPacket.class, i++, PlayNetworkDirection.PLAY_TO_SERVER)
			.encoder(CreateItemC2SPacket::write).decoder(CreateItemC2SPacket::new)
			.consumerMainThread(serverHandler(CreateItemC2SPacket::apply)).add();
		CHANNEL.messageBuilder(EmiChessPacket.C2S.class, i++, PlayNetworkDirection.PLAY_TO_SERVER)
			.encoder(EmiChessPacket.C2S::write).decoder(EmiChessPacket.C2S::new)
			.consumerMainThread(serverHandler(EmiChessPacket.C2S::apply)).add();

		CHANNEL.messageBuilder(PingS2CPacket.class, i++, PlayNetworkDirection.PLAY_TO_CLIENT)
			.encoder(PingS2CPacket::write).decoder(PingS2CPacket::new)
			.consumerMainThread(clientHandler(PingS2CPacket::apply)).add();
		CHANNEL.messageBuilder(CommandS2CPacket.class, i++, PlayNetworkDirection.PLAY_TO_CLIENT)
			.encoder(CommandS2CPacket::write).decoder(CommandS2CPacket::new)
			.consumerMainThread(clientHandler(CommandS2CPacket::apply)).add();
		CHANNEL.messageBuilder(EmiChessPacket.S2C.class, i++, PlayNetworkDirection.PLAY_TO_CLIENT)
			.encoder(EmiChessPacket.S2C::write).decoder(EmiChessPacket.S2C::new)
			.consumerMainThread(clientHandler(EmiChessPacket.S2C::apply)).add();
	}

	private static <T> MessageFunctions.MessageConsumer<T> serverHandler(BiConsumer<T, PlayerEntity> handler) {
		return (t, context) -> {
			handler.accept(t, context.getSender());
			context.setPacketHandled(true);
		};
	}

	private static <T> MessageFunctions.MessageConsumer<T> clientHandler(BiConsumer<T, PlayerEntity> handler) {
		return (t, context) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			handler.accept(t, client.player);
			context.setPacketHandled(true);
		};
	}
}
