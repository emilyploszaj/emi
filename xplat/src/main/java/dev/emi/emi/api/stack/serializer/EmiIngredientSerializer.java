package dev.emi.emi.api.stack.serializer;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.registry.EmiIngredientSerializers;

public interface EmiIngredientSerializer<T extends EmiIngredient> {

	String getType();
	
	EmiIngredient deserialize(JsonElement element);

	JsonElement serialize(T stack);

	public static @Nullable JsonElement getSerialized(EmiIngredient ingredient) {
		return EmiIngredientSerializers.serialize(ingredient);
	}

	public static EmiIngredient getDeserialized(JsonElement element) {
		return EmiIngredientSerializers.deserialize(element);
	}
}
