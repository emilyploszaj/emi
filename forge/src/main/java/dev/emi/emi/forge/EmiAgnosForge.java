package dev.emi.emi.forge;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import dev.emi.emi.platform.EmiAgnos;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLLoader;

public class EmiAgnosForge extends EmiAgnos {
	static {
		EmiAgnos.delegate = new EmiAgnosForge();
	}

	@Override
	protected String getModNameAgnos(String namespace) {
		if (namespace.equals("c")) {
			return "Common";
		}
		Optional<? extends ModContainer> container = ModList.get().getModContainerById(namespace);
		if (container.isPresent()) {
			return container.get().getModInfo().getDisplayName();
		}
		return namespace;
	}

	@Override
	protected Path getConfigDirectoryAgnos() {
		return Path.of(FMLConfig.defaultConfigPath());
	}

	@Override
	protected boolean isDevelopmentEnvironmentAgnos() {
		return !FMLLoader.isProduction();
	}

	@Override
	protected boolean isModLoadedAgnos(String id) {
		return ModList.get().isLoaded(id);
	}

	@Override
	protected List<String> getAllModNamesAgnos() {
		return ModList.get().getMods().stream().map(m -> m.getDisplayName()).toList();
	}

	@Override
	protected List<String> getAllModAuthorsAgnos() {
		return ModList.get().getMods().stream().flatMap(m -> {
			Optional<Object> opt = m.getConfig().getConfigElement("authors");
			if (opt.isPresent()) {
				String authors = (String) opt.get();
				return Lists.newArrayList(authors.split("\\,")).stream().map(s -> s.trim());
			}
			return Stream.empty();
		}).distinct().toList();
	}
}
