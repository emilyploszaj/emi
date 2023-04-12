package dev.emi.emi.fabric;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import dev.emi.emi.platform.EmiAgnos;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class EmiAgnosFabric extends EmiAgnos {
	static {
		EmiAgnos.delegate = new EmiAgnosFabric();
	}

	@Override
	protected String getModNameAgnos(String namespace) {
		if (namespace.equals("c")) {
			return "Common";
		}
		Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(namespace);
		if (container.isPresent()) {
			return container.get().getMetadata().getName();
		}
		return namespace;
	}

	@Override
	protected Path getConfigDirectoryAgnos() {
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	protected boolean isDevelopmentEnvironmentAgnos() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	protected boolean isModLoadedAgnos(String id) {
		return FabricLoader.getInstance().isModLoaded(id);
	}

	@Override
	protected List<String> getAllModNamesAgnos() {
		return FabricLoader.getInstance().getAllMods().stream().map(c -> c.getMetadata().getName()).toList();
	}

	@Override
	protected List<String> getAllModAuthorsAgnos() {
		return FabricLoader.getInstance().getAllMods().stream().flatMap(c -> c.getMetadata().getAuthors().stream())
			.map(p -> p.getName()).distinct().toList();
	}

	@Override
	protected Text getFluidNameAgnos(Fluid fluid, NbtCompound nbt) {
		return FluidVariantAttributes.getName(FluidVariant.of(fluid, nbt));
	}

	@Override
	protected List<Text> getFluidTooltipAgnos(Fluid fluid, NbtCompound nbt) {
		return FluidVariantRendering.getTooltip(FluidVariant.of(fluid, nbt));
	}

	@Override
	protected boolean canBatchAgnos(Item item) {
		return ColorProviderRegistry.ITEM.get(item) == null;
	}
}
