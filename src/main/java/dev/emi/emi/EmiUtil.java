package dev.emi.emi;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import org.lwjgl.glfw.GLFW;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList.Named;

public class EmiUtil {
	public static final Random RANDOM = new Random();
	public static final int CONTROL_MASK = 1;
	public static final int ALT_MASK = 2;
	public static final int SHIFT_MASK = 4;

	public static boolean isControlDown() {
		return Screen.hasControlDown();
	}

	public static boolean isAltDown() {
		return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT)
			|| InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_ALT);
	}

	public static boolean isShiftDown() {
		return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)
			|| InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	public static int maskFromCode(int keyCode) {
		if (MinecraftClient.IS_SYSTEM_MAC) {
			if (keyCode == GLFW.GLFW_KEY_LEFT_SUPER || keyCode == GLFW.GLFW_KEY_RIGHT_SUPER) {
				return EmiUtil.CONTROL_MASK;
			}
		}
		if (keyCode == GLFW.GLFW_KEY_LEFT_CONTROL || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) {
			return EmiUtil.CONTROL_MASK;
		} else if (keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT) {
			return EmiUtil.ALT_MASK;
		} else if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			return EmiUtil.SHIFT_MASK;
		}
		return 0;
	}

	public static int getCurrentModifiers() {
		int ret = 0;
		if (isControlDown()) {
			ret |= CONTROL_MASK;
		}
		if (isAltDown()) {
			ret |= ALT_MASK;
		}
		if (isShiftDown()) {
			ret |= SHIFT_MASK;
		}
		return ret;
	}

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
		if (namespace.equals("c")) {
			return "Common";
		}
		Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(namespace);
		if (container.isPresent()) {
			return container.get().getMetadata().getName();
		}
		return namespace;
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

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static List<EmiRecipe> getValidRecipes(EmiIngredient ingredient, EmiPlayerInventory inventory, boolean requireCraftable) {
		if (ingredient.getEmiStacks().size() == 1) {
			HandledScreen<?> hs = EmiApi.getHandledScreen();
			EmiStack stack = ingredient.getEmiStacks().get(0);
			return EmiRecipes.byOutput.getOrDefault(stack.getKey(), Map.of()).values().stream().flatMap(l -> l.stream()).filter(r -> {
				if (r.supportsRecipeTree() && r.getOutputs().stream().anyMatch(i -> i.isEqual(stack))) {
					EmiRecipeHandler handler = EmiRecipeFiller.getFirstValidHandler(r, hs);
					return handler != null && (!requireCraftable || handler.canCraft(r, inventory, hs));
				}
				return false;
			}).toList();
		}
		return List.of();
	}

	public static EmiRecipe getRecipeResolution(EmiIngredient ingredient, EmiPlayerInventory inventory, boolean requireCraftable) {
		if (ingredient.getEmiStacks().size() == 1) {
			EmiStack stack = ingredient.getEmiStacks().get(0);
			return getPreferredRecipe(EmiRecipes.byOutput.getOrDefault(stack.getKey(), Map.of()).values().stream()
				.flatMap(l -> l.stream()).filter(r -> {
					if (r.supportsRecipeTree() && r.getOutputs().stream().anyMatch(i -> i.isEqual(stack))) {
						return !requireCraftable || inventory.canCraft(r);
					}
					return false;
				}).toList());
		}
		return null;
	}
}
