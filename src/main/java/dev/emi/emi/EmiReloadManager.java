package dev.emi.emi;

import java.util.HashMap;
import java.util.function.Consumer;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.screen.EmiScreenManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

public class EmiReloadManager {
	private static volatile boolean clear = false, restart = false;
	private static Thread thread;

	public synchronized static void clear() {
		clear = true;
		if (isReloading()) {
			restart = true;
		} else {
			thread = new Thread(new ReloadWorker());
			thread.setDaemon(true);
			thread.start();
		}
	}
	
	public synchronized static void reload() {
		if (isReloading()) {
			restart = true;
		} else {
			thread = new Thread(new ReloadWorker());
			thread.setDaemon(true);
			thread.start();
		}
	}

	public static boolean isReloading() {
		return thread != null && thread.isAlive();
	}

	private synchronized static void finish() {
		thread = null;
	}
	
	private static class ReloadWorker implements Runnable {

		@Override
		public void run() {
			outer:
			do {
				long reloadStart = System.currentTimeMillis();
				restart = false;
				EmiRecipes.clear();
				EmiStackList.clear();
				EmiExclusionAreas.clear();
				EmiDragDropHandlers.clear();
				EmiStackProviders.clear();
				if (clear) {
					clear = false;
					break;
				}

				EmiClient.itemTags = Registry.ITEM.streamTags()
					.filter(key -> !EmiClient.excludedTags.contains(key.id()))
					.sorted((a, b) -> Long.compare(EmiUtil.values(b).count(), EmiUtil.values(a).count()))
					.toList();
				if (EmiConfig.logUntranslatedTags) {
					boolean warned = false;
					for (TagKey<Item> tag : EmiClient.itemTags) {
						String translation = EmiUtil.translateId("tag.", tag.id());
						if (!I18n.hasTranslation(translation)) {
							warned = true;
							EmiLog.warn("No translation for tag #" + tag.id());
						}
					}
					if (warned) {
						EmiLog.warn("Tag warning can be disabled in the config");
						EmiLog.warn("EMI docs describe how to add a translation or exclude tags.");
					}
				}
				EmiRecipeFiller.handlers.clear();
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
						EmiLog.warn("[emi] Exception loading plugin provided by " + plugin.getProvider().getMetadata().getId());
						EmiLog.error(e);
						if (restart) {
							continue outer;
						}
						continue;
					}
					System.out.println("[emi] Reloaded plugin from " + plugin.getProvider().getMetadata().getName() + " in "
						+ (System.currentTimeMillis() - start) + "ms");
					if (restart) {
						continue outer;
					}
				}
				if (restart) {
					continue;
				}
				EmiStackList.bake();
				Consumer<EmiRecipe> registerLateRecipe = registry::addRecipe;
				for (Consumer<Consumer<EmiRecipe>> consumer : EmiRecipes.lateRecipes) {
					consumer.accept(registerLateRecipe);
				}
				EmiRecipes.bake();
				BoM.reload();
				EmiLog.bake();
				EmiPersistentData.load();
				// Update search
				EmiScreenManager.search.setText(EmiScreenManager.search.getText());
				System.out.println("[emi] Reloaded EMI in " + (System.currentTimeMillis() - reloadStart) + "ms");
			} while (restart);
			finish();
		}

		private final static int entrypointPriority(EntrypointContainer<EmiPlugin> entrypoint) {
			return entrypoint.getProvider().getMetadata().getId().equals("emi") ? 0 : 1;
		}
	}
}
