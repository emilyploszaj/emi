package dev.emi.emi.platform.forge;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class EmiPacketHandler {
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new Identifier("emi:emi"), () -> "0", NetworkRegistry.acceptMissingOr("0"), NetworkRegistry.acceptMissingOr("0"));
	
	public static void init() {
		int i = 0;
		CHANNEL.registerMessage(i++, FillRecipeC2SPacket.class, FillRecipeC2SPacket::write, FillRecipeC2SPacket::new,
			serverHandler(FillRecipeC2SPacket::apply), Optional.of(NetworkDirection.PLAY_TO_SERVER));
		CHANNEL.registerMessage(i++, CreateItemC2SPacket.class, CreateItemC2SPacket::write, CreateItemC2SPacket::new,
			serverHandler(CreateItemC2SPacket::apply), Optional.of(NetworkDirection.PLAY_TO_SERVER));
		CHANNEL.registerMessage(i++, EmiChessPacket.C2S.class, EmiChessPacket.C2S::write, EmiChessPacket.C2S::new,
			serverHandler(EmiChessPacket.C2S::apply), Optional.of(NetworkDirection.PLAY_TO_SERVER));

		CHANNEL.registerMessage(i++, PingS2CPacket.class, PingS2CPacket::write, PingS2CPacket::new,
			clientHandler(PingS2CPacket::apply), Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(i++, CommandS2CPacket.class, CommandS2CPacket::write, CommandS2CPacket::new,
			clientHandler(CommandS2CPacket::apply), Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(i++, EmiChessPacket.S2C.class, EmiChessPacket.S2C::write, EmiChessPacket.S2C::new,
			clientHandler(EmiChessPacket.S2C::apply), Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}

	private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> serverHandler(BiConsumer<T, PlayerEntity> handler) {
		return (t, context) -> {
			context.get().enqueueWork(() -> {
				handler.accept(t, context.get().getSender());
				context.get().setPacketHandled(true);
			});
		};
	}

	private static <T> BiConsumer<T, Supplier<NetworkEvent.Context>> clientHandler(BiConsumer<T, PlayerEntity> handler) {
		return (t, context) -> {
			context.get().enqueueWork(() -> {
				MinecraftClient client = MinecraftClient.getInstance();
				handler.accept(t, client.player);
				context.get().setPacketHandled(true);
			});
		};
	}
}
