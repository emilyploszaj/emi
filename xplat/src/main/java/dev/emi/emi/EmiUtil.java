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
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.EmiRecipeFiller;
import dev.emi.emi.registry.EmiRecipes;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList.Named;

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

	public static Stream<RegistryEntry<Item>> values(TagKey<Item> key) {
		Optional<Named<Item>> opt = EmiPort.getItemRegistry().getEntryList(key);
		if (opt.isEmpty()) {
			return Stream.of();
		} else {
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

	public static EmiRecipe getPreferredRecipe(List<EmiRecipe> recipes) {
		if (recipes.isEmpty()) {
			return null;
		}
		for (EmiRecipe recipe : recipes) {
			if (BoM.isRecipeEnabled(recipe)) {
				return recipe;
			}
		}
		return recipes.get(0);
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
			public ItemStack transferSlot(PlayerEntity player, int index) {
				return ItemStack.EMPTY;
			}

			@Override
			public void onContentChanged(Inventory inventory) {
			}
		}, 3, 3);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static List<EmiRecipe> getValidRecipes(EmiIngredient ingredient, EmiPlayerInventory inventory, boolean requireCraftable) {
		if (ingredient.getEmiStacks().size() == 1) {
			HandledScreen<?> hs = EmiApi.getHandledScreen();
			EmiStack stack = ingredient.getEmiStacks().get(0);
			EmiCraftContext context = new EmiCraftContext<>(hs, inventory, EmiCraftContext.Type.CRAFTABLE);
			return EmiRecipes.byOutput.getOrDefault(stack.getKey(), List.of()).stream().filter(r -> {
				if (r.supportsRecipeTree() && r.getOutputs().stream().anyMatch(i -> i.isEqual(stack))) {
					EmiRecipeHandler handler = EmiRecipeFiller.getFirstValidHandler(r, hs);
					return handler != null && (!requireCraftable || handler.canCraft(r, context));
				}
				return false;
			}).toList();
		}
		return List.of();
	}

	public static EmiRecipe getRecipeResolution(EmiIngredient ingredient, EmiPlayerInventory inventory, boolean requireCraftable) {
		if (ingredient.getEmiStacks().size() == 1) {
			EmiStack stack = ingredient.getEmiStacks().get(0);
			return getPreferredRecipe(EmiRecipes.byOutput.getOrDefault(stack.getKey(), List.of()).stream().filter(r -> {
					if (r.supportsRecipeTree() && r.getOutputs().stream().anyMatch(i -> i.isEqual(stack))) {
						return !requireCraftable || inventory.canCraft(r);
					}
					return false;
				}).toList());
		}
		return null;
	}
}
