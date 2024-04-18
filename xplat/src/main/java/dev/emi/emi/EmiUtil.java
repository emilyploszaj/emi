package dev.emi.emi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.data.EmiRecipeCategoryProperties;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.EmiRecipeFiller;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList.Named;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class EmiUtil {
	public static final Random RANDOM = new Random();

	public static String subId(Identifier id) {
		return id.getNamespace() + "/" + id.getPath();
	}

	public static String subId(Block block) {
		return subId(EmiPort.getBlockRegistry().getId(block));
	}

	public static String subId(Item item) {
		return subId(EmiPort.getItemRegistry().getId(item));
	}

	public static String subId(Fluid fluid) {
		return subId(EmiPort.getFluidRegistry().getId(fluid));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <T> Stream<RegistryEntry<T>> values(TagKey<T> key) {
		Registry<T> registry = Registries.REGISTRIES.get((RegistryKey) key.registry());
		Optional<Named<T>> opt = registry.getEntryList(key);
		if (opt.isEmpty()) {
			return Stream.of();
		} else {
			if (registry == EmiPort.getFluidRegistry()) {
				return opt.get().stream().filter(o -> {
					Fluid f = (Fluid) o.value();
					return f.isStill(f.getDefaultState());
				});
			}
			return opt.get().stream();
		}
	}

	public static boolean showAdvancedTooltips() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client.options.advancedItemTooltips;
	}

	public static String translateId(String prefix, Identifier id) {
		return prefix + id.getNamespace() + "." + id.getPath().replace('/', '.');
	}

	public static String getModName(String namespace) {
		return EmiAgnos.getModName(namespace);
	}

	public static List<String> getStackTrace(Throwable t) {
		StringWriter writer = new StringWriter();
		t.printStackTrace(new PrintWriter(writer, true));
		return Arrays.asList(writer.getBuffer().toString().split("\n"));
	}

	public static CraftingInventory getCraftingInventory() {
		return new CraftingInventory(new ScreenHandler(null, -1) {

			@Override
			public boolean canUse(PlayerEntity player) {
				return false;
			}

			@Override
			public ItemStack quickMove(PlayerEntity player, int index) {
				return ItemStack.EMPTY;
			}

			@Override
			public void onContentChanged(Inventory inventory) {
			}
		}, 3, 3);
	}

	public static int getOutputCount(EmiRecipe recipe, EmiIngredient stack) {
		int count = 0;
		for (EmiStack o : recipe.getOutputs()) {
			if (stack.getEmiStacks().contains(o)) {
				count += o.getAmount();
			}
		}
		return count;
	}

	public static EmiRecipe getPreferredRecipe(EmiIngredient ingredient, EmiPlayerInventory inventory, boolean requireCraftable) {
		if (ingredient.getEmiStacks().size() == 1 && !ingredient.isEmpty()) {
			HandledScreen<?> hs = EmiApi.getHandledScreen();
			EmiStack stack = ingredient.getEmiStacks().get(0);
			return getPreferredRecipe(EmiApi.getRecipeManager().getRecipesByOutput(stack).stream().filter(r -> {
				@SuppressWarnings("rawtypes")
				EmiRecipeHandler handler = EmiRecipeFiller.getFirstValidHandler(r, hs);
				return handler != null && handler.supportsRecipe(r);
			}).toList(), inventory, requireCraftable);
		}
		return null;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static EmiRecipe getPreferredRecipe(List<EmiRecipe> recipes, EmiPlayerInventory inventory, boolean requireCraftable) {
		EmiRecipe preferred = null;
		int preferredWeight = -1;
		HandledScreen<?> hs = EmiApi.getHandledScreen();
		EmiCraftContext context = new EmiCraftContext<>(hs, inventory, EmiCraftContext.Type.CRAFTABLE);
		for (EmiRecipe recipe : recipes) {
			if (!recipe.supportsRecipeTree()) {
				continue;
			}
			int weight = 0;
			EmiRecipeHandler handler = EmiRecipeFiller.getFirstValidHandler(recipe, hs);
			if (handler != null && handler.canCraft(recipe, context)) {
				weight += 16;
			} else if (requireCraftable) {
				continue;
			} else if (inventory.canCraft(recipe)) {
				weight += 8;
			}
			if (BoM.isRecipeEnabled(recipe)) {
				weight += 4;
			}
			if (recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING) {
				weight += 2;
			}
			if (weight > preferredWeight) {
				preferredWeight = weight;
				preferred = recipe;
			} else if (weight == preferredWeight) {
				if (EmiRecipeCategoryProperties.getOrder(recipe.getCategory()) < EmiRecipeCategoryProperties.getOrder(preferred.getCategory())) {
					preferredWeight = weight;
					preferred = recipe;
				}
			}
		}
		return preferred;
	}

	public static EmiRecipe getRecipeResolution(EmiIngredient ingredient, EmiPlayerInventory inventory) {
		if (ingredient.getEmiStacks().size() == 1 && !ingredient.isEmpty()) {
			EmiStack stack = ingredient.getEmiStacks().get(0);
			return getPreferredRecipe(EmiApi.getRecipeManager().getRecipesByOutput(stack).stream().filter(r -> {
					return r.getOutputs().stream().anyMatch(i -> i.isEqual(stack));
				}).toList(), inventory, false);
		}
		return null;
	}

	public static ItemStack setPotion(ItemStack stack, RegistryEntry<Potion> potion) {
		stack.apply(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT, potion, PotionContentsComponent::with);
		return stack;
	}
}
