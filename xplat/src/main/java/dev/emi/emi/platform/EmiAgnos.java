package dev.emi.emi.platform;

import java.nio.file.Path;
import java.util.List;

public abstract class EmiAgnos {
	public static EmiAgnos delegate;

	static {
		try {
			Class.forName("dev.emi.emi.fabric.EmiAgnosFabric");
		} catch (Throwable t) {
		}
		try {
			Class.forName("dev.emi.emi.forge.EmiAgnosForge");
		} catch (Throwable t) {
		}
	}

	public static String getModName(String namespace) {
		return delegate.getModNameAgnos(namespace);
	}

	protected abstract String getModNameAgnos(String namespace);

	public static Path getConfigDirectory() {
		return delegate.getConfigDirectoryAgnos();
	}

	protected abstract Path getConfigDirectoryAgnos();

	public static boolean isDevelopmentEnvironment() {
		return delegate.isDevelopmentEnvironmentAgnos();
	}

	protected abstract boolean isDevelopmentEnvironmentAgnos();

	public static boolean isModLoaded(String id) {
		return delegate.isModLoadedAgnos(id);
	}

	protected abstract boolean isModLoadedAgnos(String id);

	public static List<String> getAllModNames() {
		return delegate.getAllModNamesAgnos();
	}

	protected abstract List<String> getAllModNamesAgnos();

	public static List<String> getAllModAuthors() {
		return delegate.getAllModAuthorsAgnos();
	}

	protected abstract List<String> getAllModAuthorsAgnos();
}
