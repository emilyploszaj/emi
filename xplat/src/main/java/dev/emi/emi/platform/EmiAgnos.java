package dev.emi.emi.platform;

import java.nio.file.Path;
import java.util.List;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

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

	public static Text getFluidName(Fluid fluid, NbtCompound nbt) {
		return delegate.getFluidNameAgnos(fluid, nbt);
	}

	protected abstract Text getFluidNameAgnos(Fluid fluid, NbtCompound nbt);

	public static List<Text> getFluidTooltip(Fluid fluid, NbtCompound nbt) {
		return delegate.getFluidTooltipAgnos(fluid, nbt);
	}

	protected abstract List<Text> getFluidTooltipAgnos(Fluid fluid, NbtCompound nbt);

	public static boolean canBatch(Item item) {
		return delegate.canBatchAgnos(item);
	}
	
	protected abstract boolean canBatchAgnos(Item item);
}
