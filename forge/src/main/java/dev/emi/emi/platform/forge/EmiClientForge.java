package dev.emi.emi.platform.forge;

import java.util.Arrays;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.emi.emi.screen.StackBatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "emi", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EmiClientForge {
	
	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		StackBatcher.EXTRA_RENDER_LAYERS.addAll(Arrays.stream(ForgeRenderTypes.values()).map(f -> f.get()).toList());
		EmiClient.init();
		EmiNetwork.initClient(packet -> EmiPacketHandler.CHANNEL.sendToServer(packet));
		MinecraftForge.EVENT_BUS.addListener(EmiClientForge::recipesReloaded);
		MinecraftForge.EVENT_BUS.addListener(EmiClientForge::tagsReloaded);
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

	public static void recipesReloaded(RecipesUpdatedEvent event) {
		EmiReloadManager.reloadRecipes();
	}

	public static void tagsReloaded(TagsUpdatedEvent event) {
		EmiReloadManager.reloadTags();
	}
}
