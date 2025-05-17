package dev.emi.emi.stack.serializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.SearchEmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.registry.EmiIngredientSerializers;
import net.minecraft.util.JsonHelper;

import java.util.ArrayList;
import java.util.List;

public class SearchEmiIngredientSerializer implements EmiIngredientSerializer<SearchEmiIngredient> {
    @Override
    public String getType() {
        return "text";
    }

    @Override
    public EmiIngredient deserialize(JsonElement element) {
        JsonObject json = element.getAsJsonObject();
        String content = JsonHelper.getString(json, "content");
        JsonArray resultsArray = JsonHelper.getArray(json, "results");

        List<EmiIngredient> results = new ArrayList<>();
        for (JsonElement resultElement : resultsArray) {
            results.add(EmiIngredientSerializers.deserialize(resultElement));
        }

        return EmiIngredient.of(content, results);
    }

    @Override
    public JsonElement serialize(SearchEmiIngredient stack) {
        JsonObject json = new JsonObject();
//        json.addProperty("type", getType());
        json.addProperty("content", stack.getContent());

        JsonArray results = new JsonArray();
        for (EmiIngredient inner : stack.getResults()) {
            results.add(EmiIngredientSerializers.serialize(inner));
        }
        json.add("results", results);

        return json;
    }
}
