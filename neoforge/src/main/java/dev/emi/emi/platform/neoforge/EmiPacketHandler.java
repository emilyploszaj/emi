package dev.emi.emi.platform.neoforge;

import dev.emi.emi.network.CommandS2CPacket;
import dev.emi.emi.network.CreateItemC2SPacket;
import dev.emi.emi.network.EmiChessPacket;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.EmiPacket;
import dev.emi.emi.network.FillRecipeC2SPacket;
import dev.emi.emi.network.PingS2CPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.function.Function;

public class EmiPacketHandler {
    private static final CustomPayload.Id<EmiChessPacket.S2C> ID_CHESS_CLIENTBOUND = CustomPayload.id("emi:chess_s2c");
    private static final CustomPayload.Id<EmiChessPacket.C2S> ID_CHESS_SERVERBOUND = CustomPayload.id("emi:chess_c2s");

    public static void init(RegisterPayloadHandlerEvent event) {
        var registrar = event.registrar("emi").optional();

        registrar.play(EmiNetwork.FILL_RECIPE, makeReader(EmiNetwork.FILL_RECIPE, FillRecipeC2SPacket::new), EmiPacketHandler::handleServerbound);
        registrar.play(EmiNetwork.CREATE_ITEM, makeReader(EmiNetwork.CREATE_ITEM, CreateItemC2SPacket::new), EmiPacketHandler::handleServerbound);
        registrar.play(EmiNetwork.PING, makeReader(EmiNetwork.PING, PingS2CPacket::new), EmiPacketHandler::handleClientbound);
        registrar.play(EmiNetwork.COMMAND, makeReader(EmiNetwork.COMMAND, CommandS2CPacket::new), EmiPacketHandler::handleClientbound);
        registrar.play(ID_CHESS_SERVERBOUND, makeReader(ID_CHESS_SERVERBOUND, EmiChessPacket.C2S::new), EmiPacketHandler::handleServerbound);
        registrar.play(ID_CHESS_CLIENTBOUND, makeReader(ID_CHESS_CLIENTBOUND, EmiChessPacket.S2C::new), EmiPacketHandler::handleClientbound);
    }

    public static EmiPacket wrap(EmiPacket packet) {
        return packet;
    }

    private static <T extends EmiPacket> PacketCodec<RegistryByteBuf, T> makeReader(CustomPayload.Id<T> id, PacketDecoder<RegistryByteBuf, T> reader) {
        return PacketCodec.of(EmiPacket::write, reader);
    }

    private static void handleServerbound(EmiPacket packet, PlayPayloadContext context) {
        if (!context.flow().isServerbound()) {
            throw new IllegalArgumentException("Trying to handle serverbound packet on client: " + packet);
        }
        var player = context.player().orElse(null);
        context.workHandler().execute(() -> packet.apply(player));
    }

    private static void handleClientbound(EmiPacket packet, PlayPayloadContext context) {
        if (!context.flow().isClientbound()) {
            throw new IllegalArgumentException("Trying to handle clientbound packet on server: " + packet);
        }
        var player = context.player().orElse(null);
        context.workHandler().execute(() -> packet.apply(player));
    }
}
