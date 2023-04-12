package dev.emi.emi.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface EmiPacket {
	
	void write(PacketByteBuf buf);
	
	void apply(PlayerEntity player);

	Identifier getId();
}
