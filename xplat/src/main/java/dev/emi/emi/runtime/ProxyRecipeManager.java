package dev.emi.emi.runtime;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ProxyRecipeManager {
	private static final MinecraftClient client = MinecraftClient.getInstance();
	private static Map<Recipe<?>, Identifier> recipeIds = Map.of();
	
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
		return recipeIds.get(recipe);
	}

	public static boolean hasId(Identifier id) {
		return recipeIds.containsValue(id);
	}

	public static @Nullable Recipe<?> getRecipe(Identifier id) {
		RecipeEntry<?> entry = getRecipeEntry(id);
		if (entry == null) {
			return null;
		}
		return entry.value();
	}

	public static @Nullable RecipeEntry<?> getRecipeEntry(Identifier id) {
		RecipeManager raw = getRaw();
		if (raw == null || id == null) {
			return null;
		}
		return raw.get(id).orElse(null);
	}

	public static <I extends RecipeInput, T extends Recipe<I>> Stream<T> streamMatches(RecipeType<T> type, I inventory) {
		RecipeManager raw = getRaw();
		if (raw == null) {
			return Stream.empty();
		}
		return raw.getAllMatches(type, inventory, client.world).stream().map(e -> e.value());
	}

	public static <I extends RecipeInput, T extends Recipe<I>> List<T> getMatches(RecipeType<T> type, I inventory) {
		return streamMatches(type, inventory).toList();
	}

	public static <I extends RecipeInput, T extends Recipe<I>> @Nullable T getFirst(RecipeType<T> type, I inventory) {
		RecipeManager raw = getRaw();
		if (raw == null) {
			return null;
		}
		return raw.getFirstMatch(type, inventory, client.world).map(e -> e.value()).orElse(null);
	}

	public static void bakeIds() {
		RecipeManager raw = getRaw();
		if (raw == null) {
			return;
		}
		recipeIds = new Reference2ObjectOpenHashMap<>();
		for (RecipeEntry<?> entry : raw.values()) {
			recipeIds.put(entry.value(), entry.id());
		}
	}
}
