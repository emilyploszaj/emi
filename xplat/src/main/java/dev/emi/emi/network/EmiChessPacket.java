package dev.emi.emi.network;

import java.util.UUID;

import dev.emi.emi.chess.EmiChess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public abstract class EmiChessPacket implements EmiPacket {
	protected final UUID uuid;
	protected final byte type, start, end;

	public EmiChessPacket(UUID uuid, byte type, byte start, byte end) {
		this.uuid = uuid;
		this.type = type;
		this.start = start;
		this.end = end;
	}

	public EmiChessPacket(PacketByteBuf buf) {
		this(buf.readUuid(), buf.readByte(), buf.readByte(), buf.readByte());
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeUuid(uuid);
		buf.writeByte(type);
		buf.writeByte(start);
		buf.writeByte(end);
	}

	@Override
	public Identifier getId() {
		return EmiNetwork.CHESS;
	}

	public static class S2C extends EmiChessPacket {

		public S2C(UUID uuid, byte type, byte start, byte end) {
			super(uuid, type, start, end);
		}

		public S2C(PacketByteBuf buf) {
			super(buf);
		}

		@Override
		public void apply(PlayerEntity player) {
			EmiChess.receiveNetwork(uuid, type, start, end);
		}
	}

	public static class C2S extends EmiChessPacket {

		public C2S(UUID uuid, byte type, byte start, byte end) {
			super(uuid, type, start, end);
		}

		public C2S(PacketByteBuf buf) {
			super(buf);
		}

		@Override
		public void apply(PlayerEntity player) {
			PlayerEntity opponent = player.getWorld().getPlayerByUuid(uuid);
			if (opponent instanceof ServerPlayerEntity spe) {
				EmiNetwork.sendToClient(spe, new EmiChessPacket.S2C(player.getUuid(), type, start, end));
			}
		}
	}
}
