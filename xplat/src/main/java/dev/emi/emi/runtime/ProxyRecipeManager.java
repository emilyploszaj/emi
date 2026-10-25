package dev.emi.emi.runtime;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public class ProxyRecipeManager {
	private static final Minecraft client = Minecraft.getInstance();
	private static Map<Recipe<?>, ResourceLocation> recipeIds = Map.of();
	
	public static boolean isAvailable() {
		return getRaw() != null;
	}

	public static RecipeManager getRaw() {
		Level world = client.level;
		if (world == null) {
			return null;
		}
		return world.getRecipeManager();
	}

	public static ResourceLocation getId(Recipe<?> recipe) {
		return recipeIds.get(recipe);
	}

	public static boolean hasId(ResourceLocation id) {
		return recipeIds.containsValue(id);
	}

	public static @Nullable Recipe<?> getRecipe(ResourceLocation id) {
		RecipeHolder<?> entry = getRecipeEntry(id);
		if (entry == null) {
			return null;
		}
		return entry.value();
	}

	public static @Nullable RecipeHolder<?> getRecipeEntry(ResourceLocation id) {
		RecipeManager raw = getRaw();
		if (raw == null || id == null) {
			return null;
		}
		return raw.byKey(id).orElse(null);
	}

	public static <I extends RecipeInput, T extends Recipe<I>> Stream<T> streamMatches(RecipeType<T> type, I inventory) {
		RecipeManager raw = getRaw();
		if (raw == null) {
			return Stream.empty();
		}
		return raw.getRecipesFor(type, inventory, client.level).stream().map(e -> e.value());
	}

	public static <I extends RecipeInput, T extends Recipe<I>> List<T> getMatches(RecipeType<T> type, I inventory) {
		return streamMatches(type, inventory).toList();
	}

	public static <I extends RecipeInput, T extends Recipe<I>> @Nullable T getFirst(RecipeType<T> type, I inventory) {
		RecipeManager raw = getRaw();
		if (raw == null) {
			return null;
		}
		return raw.getRecipeFor(type, inventory, client.level).map(e -> e.value()).orElse(null);
	}

	public static void bakeIds() {
		RecipeManager raw = getRaw();
		if (raw == null) {
			return;
		}
		recipeIds = new Reference2ObjectOpenHashMap<>();
		for (RecipeHolder<?> entry : raw.getRecipes()) {
			recipeIds.put(entry.value(), entry.id());
		}
	}
}
