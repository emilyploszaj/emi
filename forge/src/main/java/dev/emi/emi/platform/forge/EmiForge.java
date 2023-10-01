package dev.emi.emi.platform.forge;

import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiMain;
import dev.emi.emi.registry.EmiCommands;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod("emi")
public class EmiForge {

	public EmiForge() {
		EmiMain.init();
		EmiPacketHandler.init();
		EmiNetwork.initServer((player, packet) -> {
			EmiPacketHandler.CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
		});
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(this::playerConnect);
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
