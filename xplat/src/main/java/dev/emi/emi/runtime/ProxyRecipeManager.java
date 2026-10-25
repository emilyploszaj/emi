package dev.emi.emi.runtime;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

/**
 * Recipes are no longer sent to the client by vanilla, platforms provide them through their recipe sync
 */
public class ProxyRecipeManager {
	private static final Minecraft client = Minecraft.getInstance();
	private static @Nullable RecipeMap recipes;
	private static Map<Recipe<?>, Identifier> recipeIds = Map.of();

	public static void setRecipes(@Nullable RecipeMap recipes) {
		ProxyRecipeManager.recipes = recipes;
		if (recipes == null) {
			recipeIds = Map.of();
		}
	}

	public static boolean isAvailable() {
		return getRaw() != null;
	}

	public static @Nullable RecipeMap getRaw() {
		if (client.level == null) {
			return null;
		}
		return recipes;
	}

	public static Identifier getId(Recipe<?> recipe) {
		return recipeIds.get(recipe);
	}

	public static boolean hasId(Identifier id) {
		return recipeIds.containsValue(id);
	}

	public static @Nullable Recipe<?> getRecipe(Identifier id) {
		RecipeHolder<?> entry = getRecipeEntry(id);
		if (entry == null) {
			return null;
		}
		return entry.value();
	}

	public static @Nullable RecipeHolder<?> getRecipeEntry(Identifier id) {
		RecipeMap raw = getRaw();
		if (raw == null || id == null) {
			return null;
		}
		return raw.byKey(ResourceKey.create(Registries.RECIPE, id));
	}

	public static <I extends RecipeInput, T extends Recipe<I>> Stream<T> streamMatches(RecipeType<T> type, I inventory) {
		RecipeMap raw = getRaw();
		if (raw == null) {
			return Stream.empty();
		}
		return raw.getRecipesFor(type, inventory, client.level).map(e -> e.value());
	}

	public static <I extends RecipeInput, T extends Recipe<I>> List<T> getMatches(RecipeType<T> type, I inventory) {
		return streamMatches(type, inventory).toList();
	}

	public static <I extends RecipeInput, T extends Recipe<I>> @Nullable T getFirst(RecipeType<T> type, I inventory) {
		return streamMatches(type, inventory).findFirst().orElse(null);
	}

	public static void bakeIds() {
		RecipeMap raw = getRaw();
		if (raw == null) {
			return;
		}
		recipeIds = new Reference2ObjectOpenHashMap<>();
		for (RecipeHolder<?> entry : raw.values()) {
			recipeIds.put(entry.value(), entry.id().identifier());
		}
	}
}
