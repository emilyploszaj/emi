package dev.emi.emi.platform.neoforge;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import dev.emi.emi.mixin.accessor.BrewingRecipeRegistryAccessor;
import net.neoforged.neoforge.client.ClientHooks;
import org.apache.commons.lang3.text.WordUtils;
import org.objectweb.asm.Type;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.ModFileScanData;

public class EmiAgnosNeoForge extends EmiAgnos {
	static {
		EmiAgnos.delegate = new EmiAgnosNeoForge();
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
	protected List<String> getModsWithPluginsAgnos() {
		List<String> mods = Lists.newArrayList();
		Type entrypointType = Type.getType(EmiEntrypoint.class);
		for (ModFileScanData data : ModList.get().getAllScanData()) {
			for (ModFileScanData.AnnotationData annot : data.getAnnotations()) {
				try {
					if (entrypointType.equals(annot.annotationType())) {
						mods.add(data.getIModInfoData().get(0).getMods().get(0).getModId());
					}
				} catch (Throwable t) {
					EmiLog.error("Exception constructing entrypoint:", t);
				}
			}
		}
		return mods;
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
					EmiLog.error("Exception constructing entrypoint:", t);
				}
			}
		}
		return containers;
	}

	@Override
	protected void addBrewingRecipesAgnos(EmiRegistry registry) {
		PotionBrewing brewingRegistry = Minecraft.getInstance().level != null ? Minecraft.getInstance().level.potionBrewing() : PotionBrewing.EMPTY;
		BrewingRecipeRegistryAccessor brewingRegistryAccess = (BrewingRecipeRegistryAccessor)brewingRegistry;
		for (Ingredient ingredient : brewingRegistryAccess.getPotionTypes()) {
			for (ItemStack stack : ingredient.getItems()) {
				String pid = EmiUtil.subId(stack.getItem());
				for (PotionBrewing.Mix<Potion> recipe : brewingRegistryAccess.getPotionRecipes()) {
					try {
						if (recipe.ingredient().getItems().length > 0) {
							ResourceLocation id = EmiPort.id("emi", "/brewing/" + pid
								+ "/" + EmiUtil.subId(recipe.ingredient().getItems()[0].getItem())
								+ "/" + EmiUtil.subId(EmiPort.getPotionRegistry().getKey(recipe.from().value()))
								+ "/" + EmiUtil.subId(EmiPort.getPotionRegistry().getKey(recipe.to().value())));
							registry.addRecipe(new EmiBrewingRecipe(
								EmiStack.of(EmiPort.setPotion(stack.copy(), recipe.from().value())), EmiIngredient.of(recipe.ingredient()),
								EmiStack.of(EmiPort.setPotion(stack.copy(), recipe.to().value())), id));
						}
					} catch (Exception e) {
						EmiLog.error("Error registering brewing recipe", e);
					}
				}
			}
		}

		for (PotionBrewing.Mix<Item> recipe : brewingRegistryAccess.getItemRecipes()) {
			try {
				if (recipe.ingredient().getItems().length > 0) {
					String gid = EmiUtil.subId(recipe.ingredient().getItems()[0].getItem());
					String iid = EmiUtil.subId(recipe.from().value());
					String oid = EmiUtil.subId(recipe.to().value());
					Consumer<Holder<Potion>> potionRecipeGen = entry -> {
						Potion potion = entry.value();
						if (brewingRegistry.isBrewablePotion(entry)) {
							ResourceLocation id = EmiPort.id("emi", "/brewing/item/"
								+ EmiUtil.subId(entry.unwrapKey().get().location()) + "/" + gid + "/" + iid + "/" + oid);
							registry.addRecipe(new EmiBrewingRecipe(
								EmiStack.of(EmiPort.setPotion(new ItemStack(recipe.from().value()), potion)), EmiIngredient.of(recipe.ingredient()),
								EmiStack.of(EmiPort.setPotion(new ItemStack(recipe.to().value()), potion)), id));
						}
					};
					if ((recipe.from().value() instanceof PotionItem)) {
						EmiPort.getPotionRegistry().holders().forEach(potionRecipeGen);
					} else {
						potionRecipeGen.accept(Potions.AWKWARD);
					}

				}
			} catch (Exception e) {
				EmiLog.error("Error registering brewing recipe", e);
			}
		}
		for (IBrewingRecipe ibr : brewingRegistry.getRecipes()) {
			try {
				if (ibr instanceof BrewingRecipe recipe) {
					for (ItemStack is : recipe.getInput().getItems()) {
						EmiStack input = EmiStack.of(is);
						EmiIngredient ingredient = EmiIngredient.of(recipe.getIngredient());
						EmiStack output = EmiStack.of(recipe.getOutput(is, recipe.getIngredient().getItems()[0]));
						ResourceLocation id = EmiPort.id("emi", "/brewing/neoforge/"
							+ EmiUtil.subId(input.getId()) + "/"
							+ EmiUtil.subId(ingredient.getEmiStacks().get(0).getId()) + "/"
							+ EmiUtil.subId(output.getId()));
						registry.addRecipe(new EmiBrewingRecipe(input, ingredient, output, id));
					}
				}
			} catch (Exception e) {
				EmiLog.error("Error registering brewing recipe", e);
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
	protected List<ClientTooltipComponent> getItemTooltipAgnos(ItemStack stack) {
		Minecraft client = Minecraft.getInstance();
		return ClientHooks.gatherTooltipComponents(stack, Screen.getTooltipFromItem(client, stack), stack.getTooltipImage(), 0, Integer.MAX_VALUE, Integer.MAX_VALUE, client.font);
	}

	@Override
	protected Component getFluidNameAgnos(Fluid fluid, DataComponentPatch componentChanges) {
		return new FluidStack(fluid.builtInRegistryHolder(), 1000, componentChanges).getHoverName();
	}

	@Override
	protected List<Component> getFluidTooltipAgnos(Fluid fluid, DataComponentPatch componentChanges) {
		List<Component> tooltip = Lists.newArrayList();
		tooltip.add(getFluidName(fluid, componentChanges));
		Minecraft client = Minecraft.getInstance();
		if (client.options.advancedItemTooltips) {
			tooltip.add(EmiPort.literal(EmiPort.getFluidRegistry().getKey(fluid).toString()).withStyle(ChatFormatting.DARK_GRAY));
		}
		return tooltip;
	}

	@Override
	protected boolean isFloatyFluidAgnos(FluidEmiStack stack) {
		FluidStack fs = new FluidStack(stack.getKeyOfType(Fluid.class).builtInRegistryHolder(), 1000, stack.getComponentChanges());
		return fs.getFluid().getFluidType().isLighterThanAir();
	}

	@Override
	protected void renderFluidAgnos(FluidEmiStack stack, PoseStack matrices, int x, int y, float delta, int xOff, int yOff, int width, int height) {
		FluidStack fs = new FluidStack(stack.getKeyOfType(Fluid.class).builtInRegistryHolder(), 1000, stack.getComponentChanges());
		IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fs.getFluid());
		ResourceLocation texture = ext.getStillTexture(fs);
		if (texture == null) {
			return;
		}
		int color = ext.getTintColor(fs);
		Minecraft client = Minecraft.getInstance();
		TextureAtlasSprite sprite = client.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
		EmiRenderHelper.drawTintedSprite(matrices, sprite, color, x, y, xOff, yOff, width, height);
	}

	@Override
	protected EmiStack createFluidStackAgnos(Object object) {
		if (object instanceof FluidStack f) {
			return EmiStack.of(f.getFluid(), f.getComponentsPatch(), f.getAmount());
		}
		return EmiStack.EMPTY;
	}

	@Override
	protected boolean canBatchAgnos(ItemStack stack) {
		Minecraft client = Minecraft.getInstance();
		ItemRenderer ir = client.getItemRenderer();
		BakedModel model = ir.getModel(stack, client.level, null, 0);
		return model != null && model.getClass() == SimpleBakedModel.class;
	}

	@Override
	protected Map<Item, Integer> getFuelMapAgnos() {
		Object2IntMap<Item> fuelMap = new Object2IntOpenHashMap<>();
		for (Item item : EmiPort.getItemRegistry()) {
			int time = item.getDefaultInstance().getBurnTime(null);
			if (time > 0) {
				fuelMap.put(item, time);
			}
		}
		return fuelMap;
	}

	@Override
	protected BakedModel getBakedTagModelAgnos(ResourceLocation id) {
		return Minecraft.getInstance().getModelManager().getModel(new ModelResourceLocation(id, ModelResourceLocation.STANDALONE_VARIANT));
	}

	@Override
	protected boolean isEnchantableAgnos(ItemStack stack, Enchantment enchantment) {
		ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
		enchantedBook.enchant(Holder.direct(enchantment), enchantment.getMaxLevel());
		return stack.isBookEnchantable(enchantedBook);
	}
}
