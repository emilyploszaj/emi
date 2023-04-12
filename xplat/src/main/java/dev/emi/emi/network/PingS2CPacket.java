package dev.emi.emi.network;

import dev.emi.emi.EmiClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class PingS2CPacket implements EmiPacket {

	public PingS2CPacket() {
	}

	public PingS2CPacket(PacketByteBuf buf) {
	}

	@Override
	public void write(PacketByteBuf buf) {
	}

	@Override
	public void apply(PlayerEntity player) {
		EmiClient.onServer = true;
	}

	@Override
	public Identifier getId() {
		return EmiNetwork.PING;
	}
}
