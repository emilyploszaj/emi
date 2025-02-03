package dev.emi.emi.api;

import java.util.function.Predicate;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiRegistryAdapter;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;

public interface EmiInitRegistry {

	/**
	 * Adds a serializer for a given type of ingredient.
	 * This will allow it to be favorited, constructed through resource packs, among other things.
	 */
	<T extends EmiIngredient> void addIngredientSerializer(Class<T> clazz, EmiIngredientSerializer<T> serializer);

	/**
	 * Entirely disables the given stack in EMI.
	 * This method is not intended to be called by typical mods, and exists for modpack configuration.
	 * Tags ingredients will hide all disabled stacks.
	 * If all but 1 element of a tag is disabled, tag ingredients will display as the only remaining stack.
	 * Recipes that require a disabled stack with no alternatives will not be displayed.
	 */
	void disableStack(EmiStack stack);

	/**
	 * Entirely disables stacks that match the given predicate in EMI.
	 * This method is not intended to be called by typical mods, and exists for modpack configuration.
	 * Tags ingredients will hide all disabled stacks.
	 * If all but 1 element of a tag is disabled, tag ingredients will display as the only remaining stack.
	 * Recipes that require a disabled stack with no alternatives will not be displayed.
	 */
	void disableStacks(Predicate<EmiStack> predicate);

	/**
	* Adds an adapter for a given registry to allow EMI to construct stacks and create tags from objects of the registry.
	*/
	void addRegistryAdapter(EmiRegistryAdapter<?> adapter);
}
