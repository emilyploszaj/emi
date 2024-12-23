package dev.emi.emi.stack.serializer;

import net.minecraft.util.JsonHelper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ListEmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.registry.EmiIngredientSerializers;

import java.util.ArrayList;
import java.util.List;

public class ListEmiIngredientSerializer implements EmiIngredientSerializer<ListEmiIngredient> {
    @Override
    public String getType() {
        return "list";
    }

    @Override
    public EmiIngredient deserialize(JsonElement element) {
        JsonArray ingredientsArray;
        long amount;
        float chance;
        if (element.isJsonObject()) {
            JsonObject json = element.getAsJsonObject();
            amount = JsonHelper.getLong(json, "amount", 1);
            chance = JsonHelper.getFloat(json, "chance", 1);
            ingredientsArray = JsonHelper.getArray(json, "ingredients");
        } else if (element.isJsonArray()) {
            ingredientsArray = element.getAsJsonArray();
            amount = 1;
            chance = 1;
        } else {
            return EmiStack.EMPTY;
        }
        List<EmiIngredient> ingredients = new ArrayList<>();
        for (JsonElement ingredientElement : ingredientsArray) {
            ingredients.add(EmiIngredientSerializers.deserialize(ingredientElement));
        }
        EmiIngredient ingredient = EmiIngredient.of(ingredients, amount);
        if (chance != 1) {
            ingredient.setChance(chance);
        }
        return ingredient;
    }

    @Override
    public JsonElement serialize(ListEmiIngredient stack) {
        if (stack.getAmount() == 1 && stack.getChance() == 1) {
            JsonArray array = new JsonArray();
            for (EmiIngredient inner : stack.getIngredients()) {
                array.add(EmiIngredientSerializers.serialize(inner));
            }
            return array;
        } else {
            JsonObject json = new JsonObject();
            json.addProperty("type", getType());
            if (stack.getAmount() != 1) {
                json.addProperty("amount", stack.getAmount());
            }
            if (stack.getChance() != 1) {
                json.addProperty("chance", stack.getChance());
            }
            JsonArray ingredients = new JsonArray();
            for (EmiIngredient inner : stack.getIngredients()) {
                ingredients.add(EmiIngredientSerializers.serialize(inner));
            }
            json.add("ingredients", ingredients);
            return json;
        }
    }
}
