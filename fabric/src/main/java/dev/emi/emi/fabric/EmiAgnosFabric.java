package dev.emi.emi.fabric;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import dev.emi.emi.EmiFabric;
import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.registry.EmiPluginContainer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
	protected List<EmiPluginContainer> getPluginsAgnos() {
		return FabricLoader.getInstance().getEntrypointContainers("emi", EmiPlugin.class).stream()
			.map(p -> new EmiPluginContainer(p.getEntrypoint(), p.getProvider().getMetadata().getId())).toList();
	}

	@Override
	protected void addBrewingRecipesAgnos(EmiRegistry registry) {
		for (Ingredient ingredient : BrewingRecipeRegistry.POTION_TYPES) {
			for (ItemStack stack : ingredient.getMatchingStacks()) {
				String pid = EmiUtil.subId(stack.getItem());
				for (BrewingRecipeRegistry.Recipe<Potion> recipe : BrewingRecipeRegistry.POTION_RECIPES) {
					try {
						if (recipe.ingredient.getMatchingStacks().length > 0) {
							Identifier id = new Identifier("emi", "brewing/potion/" + pid
								+ "/" + EmiUtil.subId(recipe.ingredient.getMatchingStacks()[0].getItem())
								+ "/" + EmiUtil.subId(EmiPort.getPotionRegistry().getId(recipe.input))
								+ "/" + EmiUtil.subId(EmiPort.getPotionRegistry().getId(recipe.output)));
							registry.addRecipe(new EmiBrewingRecipe(
								EmiStack.of(PotionUtil.setPotion(stack.copy(), recipe.input)), EmiIngredient.of(recipe.ingredient),
								EmiStack.of(PotionUtil.setPotion(stack.copy(), recipe.output)), id));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		for (BrewingRecipeRegistry.Recipe<Item> recipe : BrewingRecipeRegistry.ITEM_RECIPES) {
			try {
				if (recipe.ingredient.getMatchingStacks().length > 0) {
					String gid = EmiUtil.subId(recipe.ingredient.getMatchingStacks()[0].getItem());
					String iid = EmiUtil.subId(recipe.input);
					String oid = EmiUtil.subId(recipe.output);
					EmiPort.getPotionRegistry().streamEntries().forEach(entry -> {
						Potion potion = entry.value();
						if (potion == Potions.EMPTY) {
							return;
						}
						if (BrewingRecipeRegistry.isBrewable(potion)) {
							Identifier id = new Identifier("emi", "brewing/item/"
								+ EmiUtil.subId(entry.getKey().get().getValue()) + "/" + gid + "/" + iid + "/" + oid);
							registry.addRecipe(new EmiBrewingRecipe(
								EmiStack.of(PotionUtil.setPotion(new ItemStack(recipe.input), potion)), EmiIngredient.of(recipe.ingredient),
								EmiStack.of(PotionUtil.setPotion(new ItemStack(recipe.output), potion)), id));
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
	protected void renderFluidAgnos(FluidEmiStack stack, MatrixStack matrices, int x, int y, float delta) {
		EmiFabric.renderFluidStack(stack, matrices, x, y, delta);
	}

	@Override
	protected boolean canBatchAgnos(Item item) {
		return ColorProviderRegistry.ITEM.get(item) == null;
	}
}
