package dev.emi.emi.platform.neoforge;

import net.minecraft.recipe.RecipeType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.network.PingS2CPacket;
import dev.emi.emi.platform.EmiMain;
import dev.emi.emi.registry.EmiCommands;
import dev.emi.emi.runtime.EmiLog;

@Mod("emi")
public class EmiNeoForge {

	public EmiNeoForge(IEventBus modEventBus) {
		EmiMain.init();
		modEventBus.addListener(EmiPacketHandler::init);
		EmiNetwork.initServer((player, packet) -> {
			if (player.networkHandler.hasChannel(packet)) {
				PacketDistributor.sendToPlayer(player, EmiPacketHandler.wrap(packet));
			} else {
				EmiLog.warn("Can't send EMI packet to " + player + " as they're missing the channel");
			}
		});
		NeoForge.EVENT_BUS.addListener(this::registerCommands);
		NeoForge.EVENT_BUS.addListener(this::playerConnect);
        NeoForge.EVENT_BUS.addListener(this::datapackSync);
	}

	public void registerCommands(RegisterCommandsEvent event) {
		EmiCommands.registerCommands(event.getDispatcher());
	}

    public void playerConnect(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayerEntity spe) {
            EmiNetwork.sendToClient(spe, new PingS2CPacket());
        }
    }

    public void datapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(
                RecipeType.CRAFTING,
                RecipeType.STONECUTTING,
                RecipeType.SMELTING,
                RecipeType.SMOKING,
                RecipeType.BLASTING,
                RecipeType.CAMPFIRE_COOKING,
                RecipeType.SMITHING
        );
    }
}
