package dev.emi.emi.platform.neoforge;

import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiMain;
import dev.emi.emi.registry.EmiCommands;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod("emi")
public class EmiNeoForge {

	public EmiNeoForge(IEventBus modEventBus) {
		EmiMain.init();
		modEventBus.addListener(EmiPacketHandler::init);
		EmiNetwork.initServer((player, packet) -> {
			if (player.connection.hasChannel(packet)) {
				PacketDistributor.sendToPlayer(player, EmiPacketHandler.wrap(packet));
			} else {
				EmiLog.warn("Can't send EMI packet to " + player + " as they're missing the channel");
			}
		});
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
		NeoForge.EVENT_BUS.addListener(this::playerConnect);
	}

	public void registerCommands(RegisterCommandsEvent event) {
		EmiCommands.registerCommands(event.getDispatcher());
	}

	public void playerConnect(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer spe) {
			EmiNetwork.sendToClient(spe, new PingS2CPacket());
		}
	}
}
