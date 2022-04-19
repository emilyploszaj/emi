package dev.emi.emi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.screen.EmiScreenManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
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
				restart = false;
				EmiRecipes.clear();
				EmiStackList.clear();
				EmiExclusionAreas.clear();
				if (clear) {
					clear = false;
					break;
				}

				EmiClient.itemTags = Registry.ITEM.streamTags()
					.filter(key -> !EmiClient.excludedTags.contains(key.id()))
					.sorted((a, b) -> Long.compare(EmiUtil.values(b).count(), EmiUtil.values(a).count()))
					.toList();
				EmiRecipeFiller.RECIPE_HANDLERS.clear();
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
						StringWriter writer = new StringWriter();
						e.printStackTrace(new PrintWriter(writer, true));
						String[] strings = writer.getBuffer().toString().split("/");
						for (String s : strings) {
							EmiLog.warn(s);
						}
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
				EmiRecipes.bake();
				BoM.reload();
				EmiLog.bake();
				EmiPersistentData.load();
				// Update search
				EmiScreenManager.search.setText(EmiScreenManager.search.getText());
			} while (restart);
			finish();
		}

		private final static int entrypointPriority(EntrypointContainer<EmiPlugin> entrypoint) {
			return entrypoint.getProvider().getMetadata().getId().equals("emi") ? 0 : 1;
		}
	}
}
