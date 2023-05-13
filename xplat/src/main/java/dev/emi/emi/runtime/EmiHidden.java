package dev.emi.emi.runtime;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.registry.EmiStackList;

public class EmiHidden {
	public static Set<EmiIngredient> hiddenStacks = new LinkedHashSet<>();

	public static JsonArray save() {
		JsonArray arr = new JsonArray();
		for (EmiIngredient stack : hiddenStacks) {
			JsonElement el = EmiIngredientSerializer.getSerialized(stack);
			if (el != null && !el.isJsonNull()) {
				arr.add(el);
			}
		}
		return arr;
	}

	public static void load(JsonArray arr) {
		hiddenStacks.clear();
		for (JsonElement el : arr) {
			EmiIngredient stack = EmiIngredientSerializer.getDeserialized(el);
			if (!stack.isEmpty()) {
				hiddenStacks.add(stack);
			}
		}
		EmiStackList.bakeFiltered();
	}

	public static boolean isHidden(EmiIngredient stack) {
		return hiddenStacks.contains(stack);
	}

	public static void setVisibility(EmiIngredient stack, boolean hide, boolean similar) {
		if (similar && stack.getEmiStacks().size() == 1) {
			EmiStack es = stack.getEmiStacks().get(0);
			for (EmiStack i : EmiStackList.stacks) {
				if (es.getId().equals(i.getId())) {
					if (hide) {
						hiddenStacks.add(i);
					} else {
						hiddenStacks.remove(i);
					}
				}
			}
		} else {
			if (hide) {
				hiddenStacks.add(stack);
			} else {
				hiddenStacks.remove(stack);
			}
		}
		EmiStackList.bakeFiltered();
	}
}
