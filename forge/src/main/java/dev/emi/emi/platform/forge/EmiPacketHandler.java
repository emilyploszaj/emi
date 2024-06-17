package dev.emi.emi.platform.forge;

import java.util.function.BiConsumer;

import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

public class EmiPacketHandler {
	public static final SimpleChannel CHANNEL = ChannelBuilder.named(new Identifier("emi:emi"))
		.acceptedVersions((status, version) -> true).simpleChannel();
	
	public static void init() {
		int i = 0;
		CHANNEL.messageBuilder(FillRecipeC2SPacket.class, i++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(FillRecipeC2SPacket::write).decoder(FillRecipeC2SPacket::new)
			.consumerMainThread(serverHandler(FillRecipeC2SPacket::apply)).add();
		CHANNEL.messageBuilder(CreateItemC2SPacket.class, i++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(CreateItemC2SPacket::write).decoder(CreateItemC2SPacket::new)
			.consumerMainThread(serverHandler(CreateItemC2SPacket::apply)).add();
		CHANNEL.messageBuilder(EmiChessPacket.C2S.class, i++, NetworkDirection.PLAY_TO_SERVER)
			.encoder(EmiChessPacket.C2S::write).decoder(EmiChessPacket.C2S::new)
			.consumerMainThread(serverHandler(EmiChessPacket.C2S::apply)).add();

		CHANNEL.messageBuilder(PingS2CPacket.class, i++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(PingS2CPacket::write).decoder(PingS2CPacket::new)
			.consumerMainThread(clientHandler(PingS2CPacket::apply)).add();
		CHANNEL.messageBuilder(CommandS2CPacket.class, i++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(CommandS2CPacket::write).decoder(CommandS2CPacket::new)
			.consumerMainThread(clientHandler(CommandS2CPacket::apply)).add();
		CHANNEL.messageBuilder(EmiChessPacket.S2C.class, i++, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(EmiChessPacket.S2C::write).decoder(EmiChessPacket.S2C::new)
			.consumerMainThread(clientHandler(EmiChessPacket.S2C::apply)).add();
	}

	private static <T> BiConsumer<T, CustomPayloadEvent.Context> serverHandler(BiConsumer<T, PlayerEntity> handler) {
		return (t, context) -> {
			context.enqueueWork(() -> {
				handler.accept(t, context.getSender());
				context.setPacketHandled(true);
			});
		};
	}

	private static <T> BiConsumer<T, CustomPayloadEvent.Context> clientHandler(BiConsumer<T, PlayerEntity> handler) {
		return (t, context) -> {
			context.enqueueWork(() -> {
				MinecraftClient client = MinecraftClient.getInstance();
				handler.accept(t, client.player);
				context.setPacketHandled(true);
			});
		};
	}
}
