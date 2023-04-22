package dev.emi.emi.platform.forge;

import dev.emi.emi.data.EmiData;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.registry.EmiTags;
import net.minecraft.client.MinecraftClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "emi", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EmiClientForge {
	
	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		EmiClient.init();
		EmiNetwork.initClient(packet -> EmiPacketHandler.CHANNEL.sendToServer(packet));
	}

	@SubscribeEvent
	public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
		MinecraftClient client = MinecraftClient.getInstance();
		EmiTags.registerTagModels(client.getResourceManager(), event::register);
	}

	@SubscribeEvent
	public static void registerResourceReloaders(RegisterClientReloadListenersEvent event) {
		EmiData.init(reloader -> event.registerReloadListener(reloader));
	}
}
