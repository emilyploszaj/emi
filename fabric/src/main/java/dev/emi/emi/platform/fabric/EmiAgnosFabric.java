package dev.emi.emi.platform.fabric;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.jemi.JemiUtil;
import dev.emi.emi.mixin.accessor.BrewingRecipeRegistryRecipeAccessor;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.registry.EmiPluginContainer;
import dev.emi.emi.screen.FakeScreen;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.text.WordUtils;

public class EmiAgnosFabric extends EmiAgnos {
	static {
		EmiAgnos.delegate = new EmiAgnosFabric();
	}

	@Override
	protected boolean isForgeAgnos() {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected String getModNameAgnos(String namespace) {
		if (namespace.equals("c")) {
			return "Common";
		}
		Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(namespace);
		if (container.isPresent()) {
			return container.get().getMetadata().getName();
		}
		container = FabricLoader.getInstance().getModContainer(namespace.replace('_', '-'));
		if (container.isPresent()) {
			return container.get().getMetadata().getName();
		}
		return WordUtils.capitalizeFully(namespace.replace('_', ' '));
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
						Ingredient recipeIngredient = ((BrewingRecipeRegistryRecipeAccessor) recipe).getIngredient();
						if (recipeIngredient.getMatchingStacks().length > 0) {
							Identifier id = EmiPort.id("emi", "/brewing/" + pid
								+ "/" + EmiUtil.subId(recipeIngredient.getMatchingStacks()[0].getItem())
								+ "/" + EmiUtil.subId(EmiPort.getPotionRegistry().getId((Potion) ((BrewingRecipeRegistryRecipeAccessor) recipe).getInput()))
								+ "/" + EmiUtil.subId(EmiPort.getPotionRegistry().getId((Potion) ((BrewingRecipeRegistryRecipeAccessor) recipe).getOutput())));
							registry.addRecipe(new EmiBrewingRecipe(
								EmiStack.of(EmiPort.setPotion(stack.copy(), (Potion) ((BrewingRecipeRegistryRecipeAccessor) recipe).getInput())), EmiIngredient.of(recipeIngredient),
								EmiStack.of(EmiPort.setPotion(stack.copy(), (Potion) ((BrewingRecipeRegistryRecipeAccessor) recipe).getOutput())), id));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		for (BrewingRecipeRegistry.Recipe<Item> recipe : BrewingRecipeRegistry.ITEM_RECIPES) {
			try {
				Ingredient recipeIngredient = ((BrewingRecipeRegistryRecipeAccessor) recipe).getIngredient();
				if (recipeIngredient.getMatchingStacks().length > 0) {
					String gid = EmiUtil.subId(recipeIngredient.getMatchingStacks()[0].getItem());
					String iid = EmiUtil.subId((Item) ((BrewingRecipeRegistryRecipeAccessor) recipe).getInput());
					String oid = EmiUtil.subId((Item) ((BrewingRecipeRegistryRecipeAccessor) recipe).getOutput());
					Consumer<RegistryEntry<Potion>> potionRecipeGen = entry -> {
						Potion potion = entry.value();
						if (potion == Potions.EMPTY) {
							return;
						}
						if (BrewingRecipeRegistry.isBrewable(potion)) {
							Identifier id = new Identifier("emi", "brewing/item/"
								+ EmiUtil.subId(entry.getKey().get().getValue()) + "/" + gid + "/" + iid + "/" + oid);
							registry.addRecipe(new EmiBrewingRecipe(
								EmiStack.of(EmiPort.setPotion(new ItemStack((Item) ((BrewingRecipeRegistryRecipeAccessor) recipe).getInput()), potion)), EmiIngredient.of(recipeIngredient),
								EmiStack.of(EmiPort.setPotion(new ItemStack((Item) ((BrewingRecipeRegistryRecipeAccessor) recipe).getOutput()), potion)), id));
						}
					};
					if ((((BrewingRecipeRegistryRecipeAccessor) recipe).getInput() instanceof PotionItem)) {
						EmiPort.getPotionRegistry().streamEntries().forEach(potionRecipeGen);
					} else {
						potionRecipeGen.accept(EmiPort.getPotionRegistry().getEntry(Potions.AWKWARD));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected List<TooltipComponent> getItemTooltipAgnos(ItemStack stack) {
		return FakeScreen.INSTANCE.getTooltipComponentListFromItem(stack);
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
	protected boolean isFloatyFluidAgnos(FluidEmiStack stack) {
		FluidVariant fluid = FluidVariant.of(stack.getKeyOfType(Fluid.class), stack.getNbt());
		return FluidVariantAttributes.isLighterThanAir(fluid);
	}

	@Override
	protected void renderFluidAgnos(FluidEmiStack stack, MatrixStack matrices, int x, int y, float delta, int xOff, int yOff, int width, int height) {
		FluidVariant fluid = FluidVariant.of(stack.getKeyOfType(Fluid.class), stack.getNbt());
		Sprite[] sprites = FluidVariantRendering.getSprites(fluid);
		if (sprites == null || sprites.length < 1 || sprites[0] == null) {
			return;
		}
		Sprite sprite = sprites[0];
		int color = FluidVariantRendering.getColor(fluid);
		
		EmiRenderHelper.drawTintedSprite(matrices, sprite, color, x, y, xOff, yOff, width, height);
	}

	@Override
	protected EmiStack createFluidStackAgnos(Object object) {
		return JemiUtil.getFluidFromJei(object);
	}

	@Override
	protected boolean canBatchAgnos(ItemStack stack) {
		return ColorProviderRegistry.ITEM.get(stack.getItem()) == null;
	}

	@Override
	protected Map<Item, Integer> getFuelMapAgnos() {
		Object2IntMap<Item> fuelMap = new Object2IntOpenHashMap<>();
		for (Item item : EmiPort.getItemRegistry()) {
			if (FuelRegistry.INSTANCE.get(item) == null) {
				continue;
			}
			int time = FuelRegistry.INSTANCE.get(item);
			if (time > 0) {
				fuelMap.put(item, time);
			}
		}
		return fuelMap;
	}
}
