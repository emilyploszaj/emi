package dev.emi.emi.platform.neoforge;

import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.function.Function;

public class EmiPacketHandler {
    private static final Identifier ID_CHESS_CLIENTBOUND = new Identifier("emi", "chess_s2c");
    private static final Identifier ID_CHESS_SERVERBOUND = new Identifier("emi", "chess_c2s");

    public static void init(RegisterPayloadHandlerEvent event) {
        var registrar = event.registrar("emi").optional();

        registrar.play(EmiNetwork.FILL_RECIPE, makeReader(EmiNetwork.FILL_RECIPE, FillRecipeC2SPacket::new), EmiPacketHandler::handleServerbound);
        registrar.play(EmiNetwork.CREATE_ITEM, makeReader(EmiNetwork.CREATE_ITEM, CreateItemC2SPacket::new), EmiPacketHandler::handleServerbound);
        registrar.play(EmiNetwork.PING, makeReader(EmiNetwork.PING, PingS2CPacket::new), EmiPacketHandler::handleClientbound);
        registrar.play(EmiNetwork.COMMAND, makeReader(EmiNetwork.COMMAND, CommandS2CPacket::new), EmiPacketHandler::handleClientbound);
        registrar.play(ID_CHESS_SERVERBOUND, makeReader(ID_CHESS_SERVERBOUND, EmiChessPacket.C2S::new), EmiPacketHandler::handleServerbound);
        registrar.play(ID_CHESS_CLIENTBOUND, makeReader(ID_CHESS_CLIENTBOUND, EmiChessPacket.S2C::new), EmiPacketHandler::handleClientbound);
    }

    public static CustomPayload wrap(EmiPacket packet) {
        // Special casing for chess since it's reusing the same ID in EMI for both
        if (packet instanceof EmiChessPacket.S2C) {
            return new PayloadWrapper<>(ID_CHESS_CLIENTBOUND, packet);
        } else if (packet instanceof EmiChessPacket.C2S) {
            return new PayloadWrapper<>(ID_CHESS_SERVERBOUND, packet);
        } else {
            return new PayloadWrapper<>(packet.getId(), packet);
        }
    }

    // Neoforge requires us to implement this interface, we use a wrapper-record to do this on top of EmiPacket
    private record PayloadWrapper<T extends EmiPacket>(Identifier id, T packet) implements CustomPayload {
        @Override
        public void write(PacketByteBuf buf) {
            packet.write(buf);
        }
    }

    private static <T extends EmiPacket> PacketByteBuf.PacketReader<PayloadWrapper<T>> makeReader(Identifier id, Function<PacketByteBuf, T> reader) {
        return buffer -> {
            // Read EMI packet and wrap
            return new PayloadWrapper<>(id, reader.apply(buffer));
        };
    }

    private static void handleServerbound(PayloadWrapper<?> wrapper, PlayPayloadContext context) {
        var packet = wrapper.packet();
        if (!context.flow().isServerbound()) {
            throw new IllegalArgumentException("Trying to handle serverbound packet on client: " + packet);
        }
        var player = context.player().orElse(null);
        context.workHandler().execute(() -> packet.apply(player));
    }

    private static void handleClientbound(PayloadWrapper<?> wrapper, PlayPayloadContext context) {
        var packet = wrapper.packet();
        if (!context.flow().isClientbound()) {
            throw new IllegalArgumentException("Trying to handle clientbound packet on server: " + packet);
        }
        var player = context.player().orElse(null);
        context.workHandler().execute(() -> packet.apply(player));
    }
}
