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
        if (!element.isJsonObject()) {
            return EmiStack.EMPTY;
        }
        JsonObject json = element.getAsJsonObject();
        long amount = JsonHelper.getLong(json, "amount", 1);
        float chance = JsonHelper.getFloat(json, "chance", 1);
        JsonArray ingredientsArray = JsonHelper.getArray(json, "ingredients");
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
        JsonObject json = new JsonObject();
        json.addProperty("type", getType());
        if (stack.getAmount() != 1) {
            json.addProperty("amount", stack.getAmount());
        }
        if (stack.getChance() != 1) {
            json.addProperty("chance", stack.getChance());
        }
        JsonArray ingredients = new JsonArray();
        for (EmiStack innerStack : stack.getEmiStacks()) {
            ingredients.add(EmiIngredientSerializers.serialize(innerStack));
        }
        json.add("ingredients", ingredients);
        return json;
    }
}
