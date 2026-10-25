package dev.emi.emi.platform;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.registry.EmiPluginContainer;

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
		try {
			Class.forName("dev.emi.emi.platform.neoforge.EmiAgnosNeoForge");
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

	public static List<String> getModsWithPlugins() {
		return delegate.getModsWithPluginsAgnos();
	}

	protected abstract List<String> getModsWithPluginsAgnos();

	public static List<EmiPluginContainer> getPlugins() {
		return delegate.getPluginsAgnos();
	}

	protected abstract List<EmiPluginContainer> getPluginsAgnos();

	public static void addBrewingRecipes(EmiRegistry registry) {
		delegate.addBrewingRecipesAgnos(registry);
	}

	protected abstract void addBrewingRecipesAgnos(EmiRegistry registry);

	public static List<ClientTooltipComponent> getItemTooltip(ItemStack stack) {
		return delegate.getItemTooltipAgnos(stack);
	}

	protected abstract List<ClientTooltipComponent> getItemTooltipAgnos(ItemStack stack);

	public static Component getFluidName(Fluid fluid, DataComponentPatch componentChanges) {
		return delegate.getFluidNameAgnos(fluid, componentChanges);
	}

	protected abstract Component getFluidNameAgnos(Fluid fluid, DataComponentPatch componentChanges);

	public static List<Component> getFluidTooltip(Fluid fluid, DataComponentPatch componentChanges) {
		return delegate.getFluidTooltipAgnos(fluid, componentChanges);
	}

	protected abstract List<Component> getFluidTooltipAgnos(Fluid fluid, DataComponentPatch componentChanges);

	public static boolean isFloatyFluid(FluidEmiStack stack) {
		return delegate.isFloatyFluidAgnos(stack);
	}

	protected abstract boolean isFloatyFluidAgnos(FluidEmiStack stack);

	public static void renderFluid(FluidEmiStack stack, PoseStack matrices, int x, int y, float delta) {
		renderFluid(stack, matrices, x, y, delta, 0, 0, 16, 16);
	}

	public static void renderFluid(FluidEmiStack stack, PoseStack matrices, int x, int y, float delta, int xOff, int yOff, int width, int height) {
		delegate.renderFluidAgnos(stack, matrices, x, y, delta, xOff, yOff, width, height);
	}

	protected abstract void renderFluidAgnos(FluidEmiStack stack, PoseStack matrices, int x, int y, float delta, int xOff, int yOff, int width, int height);

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

	public static BakedModel getBakedTagModel(ResourceLocation id) {
		return delegate.getBakedTagModelAgnos(id);
	}

	protected abstract BakedModel getBakedTagModelAgnos(ResourceLocation id);

	public static boolean isEnchantable(ItemStack stack, Enchantment enchantment) {
		return delegate.isEnchantableAgnos(stack, enchantment);
	}

	protected abstract boolean isEnchantableAgnos(ItemStack stack, Enchantment enchantment);
}
