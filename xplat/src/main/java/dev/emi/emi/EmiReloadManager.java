package dev.emi.emi;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.Lists;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.EmiPluginContainer;
import dev.emi.emi.screen.EmiScreenManager;
import dev.emi.emi.search.EmiSearch;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;

public class EmiReloadManager {
	private static volatile boolean clear = false, restart = false;
	// 0 - empty, 1 - reloading, 2 - loaded, -1 - error
	private static volatile int status = 0;
	private static Thread thread;
	public static volatile Text reloadStep = EmiPort.literal("");
	public static volatile long reloadWorry = Long.MAX_VALUE;

	public static void clear() {
		synchronized (EmiReloadManager.class) {
			clear = true;
			status = 0;
			reloadWorry = Long.MAX_VALUE;
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
			step(EmiPort.literal("Starting Reload"));
			status = 1;
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

	public static void step(Text text) {
		step(text, 5_000);
	}

	public static void step(Text text, long worry) {
		reloadStep = text;
		reloadWorry = System.currentTimeMillis() + worry;
	}

	public static boolean isLoaded() {
		return status == 2 && (thread == null || !thread.isAlive());
	}

	public static int getStatus() {
		return status;
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
					step(EmiPort.literal("Clearing data"));
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
	
					step(EmiPort.literal("Processing tags"));
					EmiClient.itemTags = EmiPort.getItemRegistry().streamTags()
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
					step(EmiPort.literal("Constructing index"));
					EmiComparisonDefaults.comparisons = new HashMap<>();
					EmiStackList.reload();
					if (restart) {
						continue;
					}
					EmiRegistry registry = new EmiRegistryImpl();
					for (EmiPluginContainer container : EmiAgnos.getPlugins().stream()
							.sorted((a, b) -> Integer.compare(entrypointPriority(a), entrypointPriority(b))).toList()) {
						step(EmiPort.literal("Loading plugin from " + container.id()), 10_000);
						long start = System.currentTimeMillis();
						try {
							container.plugin().register(registry);
						} catch (Throwable e) {
							EmiReloadLog.warn("Exception loading plugin provided by " + container.id());
							EmiReloadLog.error(e);
							if (restart) {
								continue outer;
							}
							continue;
						}
						EmiLog.info("Reloaded plugin from " + container.id() + " in "
							+ (System.currentTimeMillis() - start) + "ms");
						if (restart) {
							continue outer;
						}
					}
					if (restart) {
						continue;
					}
					step(EmiPort.literal("Baking index"));
					EmiStackList.bake();
					step(EmiPort.literal("Registering late recipes"), 10_000);
					Consumer<EmiRecipe> registerLateRecipe = registry::addRecipe;
					for (Consumer<Consumer<EmiRecipe>> consumer : EmiRecipes.lateRecipes) {
						try {
							consumer.accept(registerLateRecipe);
						} catch (Exception e) {
							EmiReloadLog.warn("Exception loading late recipes for plugins:");
							EmiReloadLog.error(e);
							if (restart) {
								continue outer;
							}
						}
					}
					step(EmiPort.literal("Baking recipes"), 15_000);
					EmiRecipes.bake();
					step(EmiPort.literal("Finishing up"));
					BoM.reload();
					EmiPersistentData.load();
					EmiSearch.bake();
					// Update search
					EmiScreenManager.search.update();
					EmiReloadLog.bake();
					EmiLog.info("Reloaded EMI in " + (System.currentTimeMillis() - reloadStart) + "ms");
					status = 2;
				} catch (Throwable e) {
					EmiLog.error("Critical error occured during reload:");
					e.printStackTrace();
					status = -1;
				}
			} while (restart);
			thread = null;
		}

		private final static int entrypointPriority(EmiPluginContainer container) {
			return container.id().equals("emi") ? 0 : 1;
		}
	}
}
