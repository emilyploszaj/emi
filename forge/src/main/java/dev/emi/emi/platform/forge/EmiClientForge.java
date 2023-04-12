package dev.emi.emi.platform.forge;

import dev.emi.emi.EmiClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "emi", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EmiClientForge {
	
	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		EmiClient.init();
	}
}
