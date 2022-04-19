package dev.emi.emi;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.Nullable;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

public interface EmiStackSerializer<T extends EmiIngredient> {
	public static final Map<Class<?>, EmiStackSerializer<?>> BY_CLASS = Maps.newHashMap();
	public static final Map<Identifier, EmiStackSerializer<?>> BY_ID = Maps.newHashMap();

	static final Object DID_YOU_KNOW_JAVAC_PREVENTS_INTERFACES_FROM_HAVING_STATIC_INITIALIZERS_DESPITE_CLINIT_CLEARLY_EXISTING
		= Util.make(() -> {
			register(new Identifier("emi", "item"), ItemEmiStack.class, new EmiStackSerializer<ItemEmiStack>() {
				public JsonObject toJson(ItemEmiStack stack) {
					JsonObject object = new JsonObject();
					ItemStack is = stack.getItemStack();
					object.addProperty("item", Registry.ITEM.getId(is.getItem()).toString());
					object.addProperty("amount", stack.getAmount());
					if (stack.hasNbt()) {
						object.addProperty("nbt", stack.getNbt().toString());
					}
					return object;
				}

				public EmiIngredient toStack(JsonObject object) {
					ItemStack is = new ItemStack(Registry.ITEM.get(new Identifier(object.get("item").getAsString())));
					is.setCount(JsonHelper.getInt(object, "amount", 1));
					if (JsonHelper.hasString(object, "nbt")) {
						try {
							is.setNbt(StringNbtReader.parse(JsonHelper.getString(object, "nbt")));
						} catch (Exception e) {
						}
					}
					return EmiStack.of(is);
				}

				public Identifier getId() {
					return new Identifier("emi", "item");
				}
			});
			return null;
		});

	public static <T extends EmiIngredient> void register(Identifier id, Class<T> clazz, EmiStackSerializer<T> serializer) {
		BY_CLASS.put(clazz, serializer);
		BY_ID.put(id, serializer);
	}

	@SuppressWarnings("unchecked")
	public static @Nullable JsonObject serialize(EmiIngredient stack) {
		if (BY_CLASS.containsKey(stack.getClass())) {
			EmiStackSerializer<?> serializer = BY_CLASS.get(stack.getClass());
			JsonObject json = ((EmiStackSerializer<EmiIngredient>) serializer).toJson(stack);
			json.addProperty("type", serializer.getId().toString());
			return json;
		}
		return null;
	}

	public static EmiIngredient deserialize(Identifier id, JsonObject object) {
		if (BY_ID.containsKey(id)) {
			return BY_ID.get(id).toStack(object);
		}
		return EmiStack.EMPTY;
	}

	public static EmiIngredient deserialize(JsonObject object) {
		String type = JsonHelper.getString(object, "type", "");
		if (Identifier.isValid(type)) {
			Identifier id = new Identifier(type);
			return deserialize(id, object);
		}
		return EmiStack.EMPTY;
	}
	
	Identifier getId();

	JsonObject toJson(T stack);

	EmiIngredient toStack(JsonObject object);
}
