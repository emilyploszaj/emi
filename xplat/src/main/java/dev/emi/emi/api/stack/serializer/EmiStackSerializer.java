package dev.emi.emi.api.stack.serializer;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public interface EmiStackSerializer<T extends EmiStack> extends EmiIngredientSerializer<T> {
	static final Pattern STACK_REGEX = Pattern.compile("^([\\w_\\-./]+):([\\w_\\-.]+):([\\w_\\-./]+)(\\{.*\\})?$");
	
	EmiStack create(Identifier id, DataComponentPatch componentChanges, long amount);

	private static <T> DynamicOps<T> withRegistryAccess(DynamicOps<T> ops) {
		Minecraft instance = Minecraft.getInstance();
		if (instance == null || instance.level == null) {
			//Note: instance can be null in datagen, just fall back to a variant that doesn't have registry access
			// as in the majority of cases this will work fine
			return ops;
		}
		return instance.level.registryAccess().createSerializationContext(ops);
	}

	@Override
	default EmiIngredient deserialize(JsonElement element) {
		Identifier id = null;
		String nbt = null;
		JsonObject changesJson = null;
		long amount = 1;
		float chance = 1;
		EmiStack remainder = EmiStack.EMPTY;
		if (GsonHelper.isStringValue(element)) {
			String s = element.getAsString();
			Matcher m = STACK_REGEX.matcher(s);
			if (m.matches()) {
				id = EmiPort.id(m.group(2), m.group(3));
				nbt = m.group(4);
			}
		} else if (element.isJsonObject()) {
			JsonObject json = element.getAsJsonObject();
			id = EmiPort.id(GsonHelper.getAsString(json, "id"));
			nbt = GsonHelper.getAsString(json, "nbt", null);
			changesJson = GsonHelper.getAsJsonObject(json, "componentChanges", null);
			amount = GsonHelper.getAsLong(json, "amount", 1);
			chance = GsonHelper.getAsFloat(json, "chance", 1);
			if (GsonHelper.isValidNode(json, "remainder")) {
				EmiIngredient ing = EmiIngredientSerializer.getDeserialized(json.get("remainder"));
				if (ing instanceof EmiStack stack) {
					remainder = stack;
				}
			}
		}
		if (id != null) {
			try {
				DataComponentPatch changes = DataComponentPatch.EMPTY;
				if (changesJson != null) {
					changes = DataComponentPatch.CODEC.decode(withRegistryAccess(JsonOps.INSTANCE), changesJson).getOrThrow().getFirst();
				} else if (nbt != null) {
					changes = DataComponentPatch.CODEC.decode(withRegistryAccess(NbtOps.INSTANCE), TagParser.parseCompoundFully(nbt)).getOrThrow().getFirst();
				}
				EmiStack stack = create(id, changes, amount);
				if (chance != 1) {
					stack.setChance(chance);
				}
				if (!remainder.isEmpty()) {
					stack.setRemainder(remainder);
				}
				return stack;
			} catch (Exception e) {
				EmiLog.error("Error parsing NBT in deserialized stack", e);
				return EmiStack.EMPTY;
			}
		}
		return EmiStack.EMPTY;
	}

	@Override
	default JsonElement serialize(T stack) {
		String nbt = null;
		DataComponentPatch componentChanges = stack.getComponentChanges();
		if (componentChanges != DataComponentPatch.EMPTY) {
			nbt = DataComponentPatch.CODEC.encodeStart(withRegistryAccess(NbtOps.INSTANCE), componentChanges).getOrThrow().toString();
		}
		if (stack.getAmount() == 1 && stack.getChance() == 1 && stack.getRemainder().isEmpty()) {
			String s = getType() + ":" + stack.getId();
			if (nbt != null) {
				s += nbt;
			}
			return new JsonPrimitive(s);
		} else {
			JsonObject json = new JsonObject();
			json.addProperty("type", getType());
			json.addProperty("id", stack.getId().toString());
			if (nbt != null) {
				json.addProperty("nbt", nbt);
			}
			if (stack.getAmount() != 1) {
				json.addProperty("amount", stack.getAmount());
			}
			if (stack.getChance() != 1) {
				json.addProperty("chance", stack.getChance());
			}
			if (!stack.getRemainder().isEmpty()) {
				EmiStack remainder = stack.getRemainder();
				if (!remainder.getRemainder().isEmpty()) {
					remainder = remainder.copy().setRemainder(EmiStack.EMPTY);
				}
				if (remainder.getRemainder().isEmpty()) {
					JsonElement remainderElement = EmiIngredientSerializer.getSerialized(remainder);
					if (remainderElement != null) {
						json.add("remainder", remainderElement);
					}
				}
			}
			return json;
		}
	}
}
