package dev.emi.emi;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

public class EmiReloadManager {
	private static volatile boolean clear = false, restart = false, populated = false;
	private static Thread thread;
	public static volatile boolean receivedInitialData;

	public static void clear() {
		synchronized (EmiReloadManager.class) {
			clear = true;
			populated = false;
			receivedInitialData = false;
			if (thread != null && thread.isAlive()) {
				restart = true;
			} else {
				thread = new Thread(new ReloadWorker());
				thread.setDaemon(true);
				thread.start();
			}
		}
	}
	
	public static void reload() {
		synchronized (EmiReloadManager.class) {
			receivedInitialData = true;
			if (thread != null && thread.isAlive()) {
				restart = true;
			} else {
				clear = false;
				thread = new Thread(new ReloadWorker());
				thread.setDaemon(false);
				thread.start();
			}
		}
	}

	public static boolean isReloading() {
		return !populated || (thread != null && thread.isAlive());
	}
	
	private static class ReloadWorker implements Runnable {

		@Override
		public void run() {
			outer:
			do {
				try {
					if (!clear) {
						EmiLog.info("Starting EMI reload...");
					}
					long reloadStart = System.currentTimeMillis();
					restart = false;
					EmiRecipes.clear();
					EmiStackList.clear();
					EmiExclusionAreas.clear();
					EmiDragDropHandlers.clear();
					EmiStackProviders.clear();
					EmiRecipeFiller.handlers.clear();
					if (clear) {
						clear = false;
						continue;
					}
					MinecraftClient client = MinecraftClient.getInstance();
					if (client.world.getRecipeManager() == null) {
						EmiReloadLog.warn("Recipe Manager is null");
						break;
					}
	
					EmiClient.itemTags = Registry.ITEM.streamTags()
						.filter(key -> !EmiClient.excludedTags.contains(key.id()))
						.sorted((a, b) -> Long.compare(EmiUtil.values(b).count(), EmiUtil.values(a).count()))
						.toList();
					if (EmiConfig.logUntranslatedTags) {
						List<String> tags = Lists.newArrayList();
						for (TagKey<Item> tag : EmiClient.itemTags) {
							String translation = EmiUtil.translateId("tag.", tag.id());
							if (!I18n.hasTranslation(translation)) {
								tags.add(tag.id().toString());
							}
						}
						if (!tags.isEmpty()) {
							for (String tag : tags.stream().sorted().toList()) {
								EmiReloadLog.warn("Untranslated tag #" + tag);
							}
							EmiReloadLog.info(" Tag warning can be disabled in the config, EMI docs describe how to add a translation or exclude tags.");
						}
					}
					EmiComparisonDefaults.comparisons = new HashMap<>();
					EmiStackList.reload();
					if (restart) {
						continue;
					}
					EmiRegistry registry = new EmiRegistryImpl();
					for (EntrypointContainer<EmiPlugin> plugin : FabricLoader.getInstance()
							.getEntrypointContainers("emi", EmiPlugin.class).stream()
							.sorted((a, b) -> Integer.compare(entrypointPriority(a), entrypointPriority(b))).toList()) {
						long start = System.currentTimeMillis();
						try {
							plugin.getEntrypoint().register(registry);
						} catch (Exception e) {
							EmiReloadLog.warn("Exception loading plugin provided by " + plugin.getProvider().getMetadata().getId());
							EmiReloadLog.error(e);
							if (restart) {
								continue outer;
							}
							continue;
						}
						EmiLog.info("Reloaded plugin from " + plugin.getProvider().getMetadata().getName() + " in "
							+ (System.currentTimeMillis() - start) + "ms");
						if (restart) {
							continue outer;
						}
					}
					if (restart) {
						continue;
					}
					populated = true;
					EmiStackList.bake();
					Consumer<EmiRecipe> registerLateRecipe = registry::addRecipe;
					for (Consumer<Consumer<EmiRecipe>> consumer : EmiRecipes.lateRecipes) {
						consumer.accept(registerLateRecipe);
					}
					EmiRecipes.bake();
					BoM.reload();
					EmiPersistentData.load();
					EmiSearch.bake();
					// Update search
					EmiScreenManager.search.update();
					EmiReloadLog.bake();
					EmiLog.info("Reloaded EMI in " + (System.currentTimeMillis() - reloadStart) + "ms");
				} catch (Exception e) {
					EmiLog.error("Critical error occured during reload:");
					e.printStackTrace();
				}
			} while (restart);
			thread = null;
		}

		private final static int entrypointPriority(EntrypointContainer<EmiPlugin> entrypoint) {
			return entrypoint.getProvider().getMetadata().getId().equals("emi") ? 0 : 1;
		}
	}
}
