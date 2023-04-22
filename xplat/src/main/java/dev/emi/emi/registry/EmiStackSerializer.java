package dev.emi.emi.registry;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.runtime.EmiLog;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.entry.RegistryEntryList.Named;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;

public interface EmiStackSerializer<T extends EmiIngredient> {
	public static final Pattern STACK_REGEX = Pattern.compile("^([\\w_\\-./]+):([\\w_\\-.]+):([\\w_\\-./]+)(\\{.*\\})?$");
	public static final Map<Class<?>, EmiStackSerializer<?>> BY_CLASS = Maps.newHashMap();
	public static final Map<Identifier, EmiStackSerializer<?>> BY_ID = Maps.newHashMap();

	static final Object DID_YOU_KNOW_JAVAC_PREVENTS_INTERFACES_FROM_HAVING_STATIC_INITIALIZERS_DESPITE_CLINIT_CLEARLY_EXISTING
		= Util.make(() -> {
			register(new Identifier("emi", "item"), ItemEmiStack.class, new EmiStackSerializer<ItemEmiStack>() {
				public JsonObject toJson(ItemEmiStack stack) {
					JsonObject object = new JsonObject();
					ItemStack is = stack.getItemStack();
					object.addProperty("item", EmiPort.getItemRegistry().getId(is.getItem()).toString());
					object.addProperty("amount", stack.getAmount());
					if (stack.hasNbt()) {
						object.addProperty("nbt", stack.getNbt().toString());
					}
					return object;
				}

				public EmiIngredient toStack(JsonObject object) {
					ItemStack is = new ItemStack(EmiPort.getItemRegistry().get(new Identifier(object.get("item").getAsString())));
					is.setCount(JsonHelper.getInt(object, "amount", 1));
					if (JsonHelper.hasString(object, "nbt")) {
						is.setNbt(parseNbt(object));
					}
					return EmiStack.of(is);
				}

				public Identifier getId() {
					return new Identifier("emi", "item");
				}
			});
			register(new Identifier("emi", "fluid"), FluidEmiStack.class, new EmiStackSerializer<FluidEmiStack>() {
				public JsonObject toJson(FluidEmiStack stack) {
					JsonObject object = new JsonObject();
					Fluid fluid = stack.getKeyOfType(Fluid.class);
					object.addProperty("fluid", EmiPort.getFluidRegistry().getId(fluid).toString());
					object.addProperty("amount", stack.getAmount());
					if (stack.hasNbt()) {
						object.addProperty("nbt", stack.getNbt().toString());
					}
					return object;
				}

				public EmiIngredient toStack(JsonObject object) {
					Fluid fluid = EmiPort.getFluidRegistry().get(new Identifier(object.get("fluid").getAsString()));
					int amount = JsonHelper.getInt(object, "amount", 1);
					if (JsonHelper.hasString(object, "nbt")) {
						return EmiStack.of(fluid, parseNbt(object), amount);
					} else {
						return EmiStack.of(fluid, amount);
					}
				}

				public Identifier getId() {
					return new Identifier("emi", "fluid");
				}
			});
			register(new Identifier("emi", "item_tag"), TagEmiIngredient.class, new EmiStackSerializer<TagEmiIngredient>() {
				public JsonObject toJson(TagEmiIngredient stack) {
					JsonObject object = new JsonObject();
					object.addProperty("tag", stack.key.id().toString());
					object.addProperty("amount", stack.getAmount());
					return object;
				}

				public EmiIngredient toStack(JsonObject object) {
					TagKey<Item> key = TagKey.of(EmiPort.getItemRegistry().getKey(), new Identifier(object.get("tag").getAsString()));
					int amount = JsonHelper.getInt(object, "amount", 1);
					Optional<Named<Item>> optional = EmiPort.getItemRegistry().getEntryList(key);
					if (!optional.isPresent() || optional.get().size() < 1) {
						return EmiStack.EMPTY;
					}
					return new TagEmiIngredient(key, amount);
				}

				public Identifier getId() {
					return new Identifier("emi", "item_tag");
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
			try {
				return BY_ID.get(id).toStack(object);
			} catch (Exception e) {
			}
		}
		return EmiStack.EMPTY;
	}

	public static EmiIngredient deserialize(JsonElement el) {
		if (el.isJsonObject()) {
			return deserialize(el.getAsJsonObject());
		} else if (JsonHelper.isString(el)) {
			String s = el.getAsString();
			Matcher m = STACK_REGEX.matcher(s);
			if (m.matches()) {
				NbtCompound nbt = null;
				String type = m.group(1);
				Identifier stackId = new Identifier(m.group(2), m.group(3));
				try {
					String g4 = m.group(4);
					if (g4 != null) {
						nbt = StringNbtReader.parse(g4);
					}
				} catch (Exception e) {
					EmiLog.error("Error parsing NBT in deserialized stack");
					e.printStackTrace();
					return EmiStack.EMPTY;
				}
				if (type.equals("item")) {
					ItemStack stack = new ItemStack(EmiPort.getItemRegistry().get(stackId));
					if (nbt != null) {
						stack.setNbt(nbt);
					}
					return EmiStack.of(stack);
				} else if (type.equals("fluid")) {
					return EmiStack.of(EmiPort.getFluidRegistry().get(stackId), nbt);
				}
			}
		}
		return EmiStack.EMPTY;
	}

	public static EmiIngredient deserialize(JsonObject object) {
		if (object != null) {
			String type = JsonHelper.getString(object, "type", "");
			if (Identifier.isValid(type)) {
				Identifier id = new Identifier(type);
				return deserialize(id, object);
			}
		}
		return EmiStack.EMPTY;
	}

	private static NbtCompound parseNbt(JsonObject json) {
		try {
			return StringNbtReader.parse(JsonHelper.getString(json, "nbt"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	Identifier getId();

	JsonObject toJson(T stack);

	EmiIngredient toStack(JsonObject object);
}
