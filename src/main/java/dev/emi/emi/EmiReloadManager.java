package dev.emi.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

public class EmiReloadManager {
	private static volatile boolean restart = false;
	private static Thread thread;
	
	public synchronized static void reload() {
		if (thread != null && thread.isAlive()) {
			restart = true;
		} else {
			thread = new Thread(new ReloadWorker());
			thread.setDaemon(true);
			thread.start();
		}
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
					plugin.getEntrypoint().register(registry);
				}
				if (restart) {
					continue;
				}
				EmiRecipes.bake();
			} while (restart);
			finish();
		}
	}
}
