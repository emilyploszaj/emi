package dev.emi.emi.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public interface EmiPacket extends CustomPayload {
	void write(RegistryByteBuf buf);
	
	void apply(PlayerEntity player);
}
