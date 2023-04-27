package dev.emi.emi.registry;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.util.JsonHelper;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EmiIngredientSerializers {
	public static final Map<Class<?>, EmiIngredientSerializer<?>> BY_CLASS = Maps.newHashMap();
	public static final Map<String, EmiIngredientSerializer<?>> BY_TYPE = Maps.newHashMap();

	public static void clear() {
		BY_CLASS.clear();
		BY_TYPE.clear();
	}

	public static @Nullable JsonElement serialize(EmiIngredient ingredient) {
		try {
			return ((EmiIngredientSerializer) BY_CLASS.get(ingredient.getClass())).serialize(ingredient);
		} catch (Exception e) {
			EmiLog.error("Exception serializing stack " + ingredient);
			e.printStackTrace();
			return null;
		}
	}

	public static EmiIngredient deserialize(JsonElement element) {
		try {
			String type;
			if (element.isJsonObject()) {
				JsonObject json = element.getAsJsonObject();
				type = json.get("type").getAsString();
				if (type.equals("emi:item")) {
					json.addProperty("type", "item");
					if (!json.has("id")) {
						json.addProperty("id", JsonHelper.getString(json, "item", ""));
					}
				} else if (type.equals("emi:fluid")) {
					json.addProperty("type", "fluid");
					if (!json.has("id")) {
						json.addProperty("id", JsonHelper.getString(json, "fluid", ""));
					}
				} else if (type.equals("emi:item_tag")) {
					json.addProperty("type", "tag");
					json.addProperty("registry", "minecraft:item");
					if (!json.has("id")) {
						json.addProperty("id", JsonHelper.getString(json, "tag", ""));
					}
				}
				type = json.get("type").getAsString();
			} else {
				String[] split = element.getAsString().split(":");
				type = split[0];
				if (!BY_TYPE.containsKey(type) && type.startsWith("#")) {
					type = "tag";
				}
			}
			return ((EmiIngredientSerializer) BY_TYPE.get(type)).deserialize(element);
		} catch (Exception e) {
			EmiLog.error("Exception deserializing stack " + element);
			e.printStackTrace();
			return EmiStack.EMPTY;
		}
	}
}
