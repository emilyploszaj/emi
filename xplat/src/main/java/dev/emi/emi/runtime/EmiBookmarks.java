package dev.emi.emi.runtime;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.SearchEmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;

import java.util.List;

public class EmiBookmarks {
    public static List<SearchEmiIngredient> bookmarks = Lists.newArrayList();

    public static JsonArray save() {
        JsonArray arr = new JsonArray();
        for (SearchEmiIngredient bookmark : bookmarks) {
            JsonElement serialized = EmiIngredientSerializer.getSerialized(bookmark);
            arr.add(serialized);
        }
        return arr;
    }

    public static void load(JsonArray arr) {
        bookmarks.clear();
        for (JsonElement element : arr) {
            EmiIngredient bookmark = EmiIngredientSerializer.getDeserialized(element);

            if (bookmark instanceof SearchEmiIngredient) {
                bookmarks.add((SearchEmiIngredient) bookmark);
            }
        }
    }

    public static void addBookmark(String content, List<? extends EmiIngredient> items) {
        SearchEmiIngredient bookmark = new SearchEmiIngredient(content, items);
        if (!bookmarks.contains(bookmark)) {
            bookmarks.add(bookmark);
        }
        EmiPersistentData.save();
    }

    public static void removeBookmark(SearchEmiIngredient bookmark) {
        bookmarks.remove(bookmark);
        EmiPersistentData.save();
    }
}

