package dev.emi.emi.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public interface EmiPacket extends CustomPacketPayload {
	void write(RegistryFriendlyByteBuf buf);
	
	void apply(Player player);
}
