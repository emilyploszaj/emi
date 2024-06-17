package dev.emi.emi.platform.forge;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.minecraft.item.PotionItem;
import net.minecraft.registry.entry.RegistryEntry;
import org.objectweb.asm.Type;

import com.google.common.collect.Lists;

import dev.emi.emi.EmiPort;
import dev.emi.emi.EmiRenderHelper;
import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.registry.EmiPluginContainer;
import dev.emi.emi.runtime.EmiLog;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.texture.Sprite;
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
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.commons.lang3.text.WordUtils;

public class EmiAgnosForge extends EmiAgnos {
	static {
		EmiAgnos.delegate = new EmiAgnosForge();
	}

	@Override
	protected boolean isForgeAgnos() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected String getModNameAgnos(String namespace) {
		if (namespace.equals("c")) {
			return "Common";
		}
		Optional<? extends ModContainer> container = ModList.get().getModContainerById(namespace);
		if (container.isPresent()) {
			return container.get().getModInfo().getDisplayName();
		}
		container = ModList.get().getModContainerById(namespace.replace('_', '-'));
		if (container.isPresent()) {
			return container.get().getModInfo().getDisplayName();
		}
		return WordUtils.capitalizeFully(namespace.replace('_', ' '));
	}

	@Override
	protected Path getConfigDirectoryAgnos() {
		return FMLPaths.CONFIGDIR.get();
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
		List<EmiPluginContainer> containers = Lists.newArrayList();
		Type entrypointType = Type.getType(EmiEntrypoint.class);
		for (ModFileScanData data : ModList.get().getAllScanData()) {
			for (ModFileScanData.AnnotationData annot : data.getAnnotations()) {
				try {
					if (entrypointType.equals(annot.annotationType())) {
						Class<?> clazz = Class.forName(annot.memberName());
						if (EmiPlugin.class.isAssignableFrom(clazz)) {
							Class<? extends EmiPlugin> pluginClass = clazz.asSubclass(EmiPlugin.class);
							EmiPlugin plugin = pluginClass.getConstructor().newInstance();
							String id = data.getIModInfoData().get(0).getMods().get(0).getModId();
							containers.add(new EmiPluginContainer(plugin, id));
						} else {
							EmiLog.error("EmiEntrypoint " + annot.memberName() + " does not implement EmiPlugin");
						}
					}
				} catch (Throwable t) {
					EmiLog.error("Exception constructing entrypoint:");
					t.printStackTrace();
				}
			}
		}
		return containers;
	}

	@Override
	protected void addBrewingRecipesAgnos(EmiRegistry registry) {
		for (Ingredient ingredient : BrewingRecipeRegistry.POTION_TYPES) {
			for (ItemStack stack : ingredient.getMatchingStacks()) {
				String pid = EmiUtil.subId(stack.getItem());
				for (BrewingRecipeRegistry.Recipe<Potion> recipe : BrewingRecipeRegistry.POTION_RECIPES) {
					try {
						if (recipe.ingredient.getMatchingStacks().length > 0) {
							Identifier id = EmiPort.id("emi", "/brewing/" + pid
								+ "/" + EmiUtil.subId(recipe.ingredient.getMatchingStacks()[0].getItem())
								+ "/" + EmiUtil.subId(EmiPort.getPotionRegistry().getId(recipe.input.get()))
								+ "/" + EmiUtil.subId(EmiPort.getPotionRegistry().getId(recipe.output.get())));
							registry.addRecipe(new EmiBrewingRecipe(
								EmiStack.of(EmiPort.setPotion(stack.copy(), recipe.input.get())), EmiIngredient.of(recipe.ingredient),
								EmiStack.of(EmiPort.setPotion(stack.copy(), recipe.output.get())), id));
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
					String iid = EmiUtil.subId(recipe.input.get());
					String oid = EmiUtil.subId(recipe.output.get());
					Consumer<RegistryEntry<Potion>> potionRecipeGen = entry -> {
						Potion potion = entry.value();
						if (potion == Potions.EMPTY) {
							return;
						}
						if (BrewingRecipeRegistry.isBrewable(potion)) {
							Identifier id = EmiPort.id("emi", "brewing/item/"
								+ EmiUtil.subId(entry.getKey().get().getValue()) + "/" + gid + "/" + iid + "/" + oid);
							registry.addRecipe(new EmiBrewingRecipe(
								EmiStack.of(EmiPort.setPotion(new ItemStack(recipe.input.get()), potion)), EmiIngredient.of(recipe.ingredient),
								EmiStack.of(EmiPort.setPotion(new ItemStack(recipe.output.get()), potion)), id));
						}
					};
					if ((recipe.input.get() instanceof PotionItem)) {
						EmiPort.getPotionRegistry().streamEntries().forEach(potionRecipeGen);
					} else {
						potionRecipeGen.accept(EmiPort.getPotionRegistry().getEntry(Potions.AWKWARD));
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (IBrewingRecipe ibr : net.minecraftforge.common.brewing.BrewingRecipeRegistry.getRecipes()) {
			try {
				if (ibr instanceof BrewingRecipe recipe) {
					for (ItemStack is : recipe.getInput().getMatchingStacks()) {
						EmiStack input = EmiStack.of(is);
						EmiIngredient ingredient = EmiIngredient.of(recipe.getIngredient());
						EmiStack output = EmiStack.of(recipe.getOutput(is, recipe.getIngredient().getMatchingStacks()[0]));
						Identifier id = EmiPort.id("emi", "brewing/forge/"
							+ EmiUtil.subId(input.getId()) + "/"
							+ EmiUtil.subId(ingredient.getEmiStacks().get(0).getId()) + "/"
							+ EmiUtil.subId(output.getId()));
						registry.addRecipe(new EmiBrewingRecipe(input, ingredient, output, id));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<String> getAllModAuthorsAgnos() {
		return ModList.get().getMods().stream().flatMap(m -> {
			Optional<Object> opt = m.getConfig().getConfigElement("authors");
			if (opt.isPresent()) {
				Object obj = opt.get();
				if (obj instanceof String authors) {
					return Lists.newArrayList(authors.split("\\,")).stream().map(s -> s.trim());
				} else if (obj instanceof List<?> list) {
					if (list.size() > 0 && list.get(0) instanceof String) {
						List<String> authors = (List<String>) list;
						return authors.stream();
					}
				}
			}
			return Stream.empty();
		}).distinct().toList();
	}

	@Override
	protected List<TooltipComponent> getItemTooltipAgnos(ItemStack stack) {
		MinecraftClient client = MinecraftClient.getInstance();
		return ForgeHooksClient.gatherTooltipComponents(stack, Screen.getTooltipFromItem(client, stack), stack.getTooltipData(), 0, Integer.MAX_VALUE, Integer.MAX_VALUE, client.textRenderer);
	}

	@Override
	protected Text getFluidNameAgnos(Fluid fluid, NbtCompound nbt) {
		return new FluidStack(fluid, 1000, nbt).getDisplayName();
	}

	@Override
	protected List<Text> getFluidTooltipAgnos(Fluid fluid, NbtCompound nbt) {
		return List.of(getFluidName(fluid, nbt));
	}

	@Override
	protected boolean isFloatyFluidAgnos(FluidEmiStack stack) {
		FluidStack fs = new FluidStack(stack.getKeyOfType(Fluid.class), 1000, stack.getNbt());
		return fs.getFluid().getFluidType().isLighterThanAir();
	}

	@Override
	protected void renderFluidAgnos(FluidEmiStack stack, MatrixStack matrices, int x, int y, float delta, int xOff, int yOff, int width, int height) {
		FluidStack fs = new FluidStack(stack.getKeyOfType(Fluid.class), 1000, stack.getNbt());
		IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fs.getFluid());
		Identifier texture = ext.getStillTexture(fs);
		int color = ext.getTintColor(fs);
		MinecraftClient client = MinecraftClient.getInstance();
		Sprite sprite = client.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(texture);
		EmiRenderHelper.drawTintedSprite(matrices, sprite, color, x, y, xOff, yOff, width, height);
	}

	@Override
	protected EmiStack createFluidStackAgnos(Object object) {
		if (object instanceof FluidStack f) {
			return EmiStack.of(f.getFluid(), f.getTag(), f.getAmount());
		}
		return EmiStack.EMPTY;
	}

	@Override
	protected boolean canBatchAgnos(ItemStack stack) {
		MinecraftClient client = MinecraftClient.getInstance();
		ItemRenderer ir = client.getItemRenderer();
		BakedModel model = ir.getModel(stack, client.world, null, 0);
		return model != null && model.getClass() == BasicBakedModel.class;
	}

	@Override
	protected Map<Item, Integer> getFuelMapAgnos() {
		Object2IntMap<Item> fuelMap = new Object2IntOpenHashMap<>();
		for (Item item : EmiPort.getItemRegistry()) {
			int time = ForgeHooks.getBurnTime(item.getDefaultStack(), null);
			if (time > 0) {
				fuelMap.put(item, time);
			}
		}
		return fuelMap;
	}
}
