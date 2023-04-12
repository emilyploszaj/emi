package dev.emi.emi.forge;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.VanillaPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.registry.EmiPluginContainer;
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
import net.minecraftforge.fluids.FluidStack;
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
	protected List<EmiPluginContainer> getPluginsAgnos() {
		return List.of(new EmiPluginContainer(new VanillaPlugin(), "emi"));
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
								+ "/" + EmiUtil.subId(EmiPort.getPotionRegistry().getId(recipe.f_43532_.get()))
								+ "/" + EmiUtil.subId(EmiPort.getPotionRegistry().getId(recipe.f_43534_.get())));
							registry.addRecipe(new EmiBrewingRecipe(
								EmiStack.of(PotionUtil.setPotion(stack.copy(), recipe.f_43532_.get())), EmiIngredient.of(recipe.ingredient),
								EmiStack.of(PotionUtil.setPotion(stack.copy(), recipe.f_43534_.get())), id));
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
					String iid = EmiUtil.subId(recipe.f_43532_.get());
					String oid = EmiUtil.subId(recipe.f_43534_.get());
					EmiPort.getPotionRegistry().streamEntries().forEach(entry -> {
						Potion potion = entry.value();
						if (potion == Potions.EMPTY) {
							return;
						}
						if (BrewingRecipeRegistry.isBrewable(potion)) {
							Identifier id = new Identifier("emi", "brewing/item/"
								+ EmiUtil.subId(entry.getKey().get().getValue()) + "/" + gid + "/" + iid + "/" + oid);
							registry.addRecipe(new EmiBrewingRecipe(
								EmiStack.of(PotionUtil.setPotion(new ItemStack(recipe.f_43532_.get()), potion)), EmiIngredient.of(recipe.ingredient),
								EmiStack.of(PotionUtil.setPotion(new ItemStack(recipe.f_43534_.get()), potion)), id));
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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

	@Override
	protected Text getFluidNameAgnos(Fluid fluid, NbtCompound nbt) {
		return new FluidStack(fluid, 1000).getDisplayName();
	}

	@Override
	protected List<Text> getFluidTooltipAgnos(Fluid fluid, NbtCompound nbt) {
		return List.of();
	}

	@Override
	protected void renderFluidAgnos(FluidEmiStack stack, MatrixStack matrices, int x, int y, float delta) {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException("Unimplemented method 'renderFluidAgnos'");
	}

	@Override
	protected boolean canBatchAgnos(Item item) {
		// TODO Auto-generated method stub
		return true;
	}
}
