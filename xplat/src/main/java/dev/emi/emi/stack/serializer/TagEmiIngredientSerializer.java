package dev.emi.emi.stack.serializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;

public class TagEmiIngredientSerializer implements EmiIngredientSerializer<TagEmiIngredient> {
	static final Pattern STACK_REGEX = Pattern.compile("^#([\\w_\\-.:]+):([\\w_\\-.]+):([\\w_\\-./]+)(\\{.*\\})?$");

	@Override
	public String getType() {
		return "tag";
	}

	@Override
	public EmiIngredient deserialize(JsonElement element) {
		if (GsonHelper.isStringValue(element)) {
			String s = element.getAsString();
			Matcher m = STACK_REGEX.matcher(s);
			if (m.matches()) {
				Identifier registry = EmiPort.id(m.group(1));
				Identifier id = EmiPort.id(m.group(2), m.group(3));
				return EmiIngredient.of(TagKey.create(ResourceKey.createRegistryKey(registry), id), 1);
			}
		} else if (element.isJsonObject()) {
			JsonObject json = element.getAsJsonObject();
			Identifier registry = EmiPort.id(json.get("registry").getAsString());
			Identifier id = EmiPort.id(json.get("id").getAsString());
			long amount = GsonHelper.getAsLong(json, "amount", 1);
			float chance = GsonHelper.getAsFloat(json, "chance", 1);
			EmiIngredient stack = EmiIngredient.of(TagKey.create(ResourceKey.createRegistryKey(registry), id), amount);
			if (chance != 1) {
				stack.setChance(chance);
			}
			return stack;
		}
		return EmiStack.EMPTY;
	}

	@Override
	public JsonElement serialize(TagEmiIngredient stack) {
		if (stack.getAmount() == 1 && stack.getChance() == 1) {
			String type = switch(stack.key.registry().identifier().toString()) {
				case "minecraft:item" -> "item";
				case "minecraft:fluid" -> "fluid";
				default -> null;
			};
			return new JsonPrimitive("#" + type + ":" + stack.key.location());
		} else {
			JsonObject json = new JsonObject();
			json.addProperty("type", "tag");
			json.addProperty("registry", stack.key.registry().identifier().toString());
			json.addProperty("id", stack.key.location().toString());
			if (stack.getAmount() != 1) {
				json.addProperty("amount", stack.getAmount());
			}
			if (stack.getChance() != 1) {
				json.addProperty("chance", stack.getChance());
			}
			return json;
		}
	}
}
