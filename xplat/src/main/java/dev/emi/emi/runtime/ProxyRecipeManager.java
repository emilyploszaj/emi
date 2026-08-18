package dev.emi.emi.runtime;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ProxyRecipeManager {
	private static final MinecraftClient client = MinecraftClient.getInstance();
	
	public static boolean isAvailable() {
		return getRaw() != null;
	}

	public static RecipeManager getRaw() {
		World world = client.world;
		if (world == null) {
			return null;
		}
		return world.getRecipeManager();
	}

	public static Identifier getId(Recipe<?> recipe) {
		return recipe.getId();
	}

	public static boolean hasId(Identifier id) {
		RecipeManager raw = getRaw();
		return raw.get(id).isPresent();
	}

	public static @Nullable Recipe<?> getRecipe(Identifier id) {
		RecipeManager raw = getRaw();
		if (raw == null || id == null) {
			return null;
		}
		return raw.get(id).orElse(null);
	}

	public static <I extends Inventory, T extends Recipe<I>> Stream<T> streamMatches(RecipeType<T> type, I inventory) {
		RecipeManager raw = getRaw();
		if (raw == null) {
			return Stream.empty();
		}
		return raw.getAllMatches(type, inventory, client.world).stream();
	}

	public static <I extends Inventory, T extends Recipe<I>> List<T> getMatches(RecipeType<T> type, I inventory) {
		return streamMatches(type, inventory).toList();
	}

	public static <I extends Inventory, T extends Recipe<I>> @Nullable T getFirst(RecipeType<T> type, I inventory) {
		RecipeManager raw = getRaw();
		if (raw == null) {
			return null;
		}
		return raw.getFirstMatch(type, inventory, client.world).orElse(null);
	}

	public static void bakeIds() {
		// Nothing to do on older versions
	}
}
