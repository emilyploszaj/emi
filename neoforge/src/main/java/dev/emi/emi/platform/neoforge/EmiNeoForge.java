package dev.emi.emi.platform.neoforge;

import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiMain;
import dev.emi.emi.registry.EmiCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod("emi")
public class EmiNeoForge {

	public EmiNeoForge() {
		EmiMain.init();
		EmiPacketHandler.init();
		EmiNetwork.initServer((player, packet) -> {
			EmiPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
		});
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
		NeoForge.EVENT_BUS.addListener(this::playerConnect);
	}

	public void registerCommands(RegisterCommandsEvent event) {
		EmiCommands.registerCommands(event.getDispatcher());
	}

	public void playerConnect(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayerEntity spe) {
			EmiNetwork.sendToClient(spe, new PingS2CPacket());
		}
	}
}
