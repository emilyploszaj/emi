package dev.emi.emi.platform.neoforge;

import java.util.Arrays;

import dev.emi.emi.EmiPort;
import dev.emi.emi.data.EmiData;
import dev.emi.emi.network.EmiNetwork;
import dev.emi.emi.platform.EmiClient;
import dev.emi.emi.registry.EmiTags;
import dev.emi.emi.runtime.EmiDrawContext;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.emi.emi.screen.ConfigScreen;
import dev.emi.emi.screen.EmiScreenBase;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.screen.StackBatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "emi", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class EmiClientNeoForge {
	
	@SubscribeEvent
	public static void clientInit(FMLClientSetupEvent event) {
		StackBatcher.EXTRA_RENDER_LAYERS.addAll(Arrays.stream(NeoForgeRenderTypes.values()).map(f -> f.get()).toList());
		EmiClient.init();
		EmiNetwork.initClient(packet -> PacketDistributor.SERVER.noArg().send(EmiPacketHandler.wrap(packet)));
		NeoForge.EVENT_BUS.addListener(EmiClientNeoForge::recipesReloaded);
		NeoForge.EVENT_BUS.addListener(EmiClientNeoForge::tagsReloaded);
		NeoForge.EVENT_BUS.addListener(EmiClientNeoForge::renderScreenForeground);
		NeoForge.EVENT_BUS.addListener(EmiClientNeoForge::postRenderScreen);
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
			() -> new ConfigScreenHandler.ConfigScreenFactory((client, last) -> new ConfigScreen(last)));
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

	public static void renderScreenForeground(ContainerScreenEvent.Render.Foreground event) {
		EmiDrawContext context = EmiDrawContext.wrap(event.getGuiGraphics());
		HandledScreen<?> screen = event.getContainerScreen();
		EmiScreenBase base = EmiScreenBase.of(screen);
		if (base != null) {
			MinecraftClient client = MinecraftClient.getInstance();
			context.push();
			context.matrices().translate(-screen.getGuiLeft(), -screen.getGuiTop(), 0.0);
			EmiPort.setPositionTexShader();
			EmiScreenManager.render(context, event.getMouseX(), event.getMouseY(), client.getTickDelta());
			context.pop();
		}
	}

	public static void postRenderScreen(ScreenEvent.Render.Post event) {
		EmiDrawContext context = EmiDrawContext.wrap(event.getGuiGraphics());
		Screen screen = event.getScreen();
		if (!(screen instanceof HandledScreen<?>)) {
			return;
		}
		EmiScreenBase base = EmiScreenBase.of(screen);
		if (base != null) {
			MinecraftClient client = MinecraftClient.getInstance();
			context.push();
			EmiPort.setPositionTexShader();
			EmiScreenManager.drawForeground(context, event.getMouseX(), event.getMouseY(), client.getTickDelta());
			context.pop();
		}
	}
}
