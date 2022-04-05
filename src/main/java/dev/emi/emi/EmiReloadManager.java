package dev.emi.emi;

import java.util.HashMap;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.screen.EmiScreenManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

public class EmiReloadManager {
	private static volatile boolean restart = false;
	private static Thread thread;
	
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
			do {
				restart = false;
				EmiRecipeFiller.RECIPE_HANDLERS.clear();
				EmiComparisonDefaults.comparisons = new HashMap<>();
				EmiStackList.reload();
				if (restart) {
					continue;
				}
				EmiRecipes.reload();
				if (restart) {
					continue;
				}
				EmiRegistry registry = new EmiRegistryImpl();
				for (EntrypointContainer<EmiPlugin> plugin : FabricLoader.getInstance().getEntrypointContainers("emi", EmiPlugin.class)) {
					long start = System.currentTimeMillis();
					plugin.getEntrypoint().register(registry);
					System.out.println("[emi] Reloaded plugin from " + plugin.getProvider().getMetadata().getName() + " in "
						+ (System.currentTimeMillis() - start) + "ms");
					if (restart) {
						continue;
					}
				}
				if (restart) {
					continue;
				}
				EmiStackList.bake();
				EmiRecipes.bake();
				BoM.reload();
				EmiLog.bake();
				// Update search
				EmiScreenManager.search.setText(EmiScreenManager.search.getText());
			} while (restart);
			finish();
		}
	}
}
