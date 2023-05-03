package dev.emi.emi.platform;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.registry.EmiPluginContainer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public abstract class EmiAgnos {
	public static EmiAgnos delegate;

	static {
		try {
			Class.forName("dev.emi.emi.platform.fabric.EmiAgnosFabric");
		} catch (Throwable t) {
		}
		try {
			Class.forName("dev.emi.emi.platform.forge.EmiAgnosForge");
		} catch (Throwable t) {
		}
	}

	public static boolean isForge() {
		return delegate.isForgeAgnos();
	}

	protected abstract boolean isForgeAgnos();

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

	public static List<EmiPluginContainer> getPlugins() {
		return delegate.getPluginsAgnos();
	}

	protected abstract List<EmiPluginContainer> getPluginsAgnos();

	public static void addBrewingRecipes(EmiRegistry registry) {
		delegate.addBrewingRecipesAgnos(registry);
	}

	protected abstract void addBrewingRecipesAgnos(EmiRegistry registry);

	public static Text getFluidName(Fluid fluid, NbtCompound nbt) {
		return delegate.getFluidNameAgnos(fluid, nbt);
	}

	protected abstract Text getFluidNameAgnos(Fluid fluid, NbtCompound nbt);

	public static List<Text> getFluidTooltip(Fluid fluid, NbtCompound nbt) {
		return delegate.getFluidTooltipAgnos(fluid, nbt);
	}

	protected abstract List<Text> getFluidTooltipAgnos(Fluid fluid, NbtCompound nbt);

	public static void renderFluid(FluidEmiStack stack, MatrixStack matrices, int x, int y, float delta) {
		delegate.renderFluidAgnos(stack, matrices, x, y, delta);
	}

	protected abstract void renderFluidAgnos(FluidEmiStack stack, MatrixStack matrices, int x, int y, float delta);

	public static EmiStack createFluidStack(Object object) {
		return delegate.createFluidStackAgnos(object);
	}

	protected abstract EmiStack createFluidStackAgnos(Object object);

	public static boolean canBatch(ItemStack stack) {
		return delegate.canBatchAgnos(stack);
	}
	
	protected abstract boolean canBatchAgnos(ItemStack stack);

	public static Map<Item, Integer> getFuelMap() {
		return delegate.getFuelMapAgnos();
	}

	protected abstract Map<Item, Integer> getFuelMapAgnos();
}
