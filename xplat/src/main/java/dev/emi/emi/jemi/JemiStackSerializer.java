package dev.emi.emi.jemi;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.util.GsonHelper;

@SuppressWarnings("rawtypes")
public class JemiStackSerializer implements EmiIngredientSerializer<JemiStack> {
	private final IIngredientManager manager;

	public JemiStackSerializer(IIngredientManager manager) {
		this.manager = manager;
	}

	@Override
	public String getType() {
		return "jemi";
	}
	
	public EmiStack create(String uid, long amount) {
		for (IIngredientType<?> type : manager.getRegisteredIngredientTypes()) {
			if (type == VanillaTypes.ITEM_STACK || type == JemiUtil.getFluidType()) {
				continue;
			}
			Optional<EmiStack> opt = findByUid(type, uid);
			if (opt.isPresent()) {
				return opt.get().setAmount(amount);
			}
		}
		return EmiStack.EMPTY;
	}

	private <T> Optional<EmiStack> findByUid(IIngredientType<T> type, String uid) {
		IIngredientHelper<T> helper = manager.getIngredientHelper(type);
		for (T ingredient : manager.getAllIngredients(type)) {
			if (uid.equals(helper.getUid(ingredient, UidContext.Ingredient).toString())) {
				return Optional.of(JemiUtil.getStack(type, ingredient));
			}
		}
		return Optional.empty();
	}

	@Override
	public EmiIngredient deserialize(JsonElement element) {
		JsonObject json = element.getAsJsonObject();
		String uid = GsonHelper.getAsString(json, "uid");
		long amount = GsonHelper.getAsLong(json, "amount", 1);
		float chance = GsonHelper.getAsFloat(json, "chance", 1);
		EmiStack remainder = EmiStack.EMPTY;
		if (GsonHelper.isValidNode(json, "remainder")) {
			EmiIngredient ing = EmiIngredientSerializer.getDeserialized(json.get("remainder"));
			if (ing instanceof EmiStack stack) {
				remainder = stack;
			}
		}
		EmiStack stack = create(uid, amount);
		if (chance != 1) {
			stack.setChance(chance);
		}
		if (!remainder.isEmpty()) {
			stack.setRemainder(remainder);
		}
		return stack;
	}

	@Override
	public JsonElement serialize(JemiStack stack) {
		JsonObject json = new JsonObject();
		json.addProperty("type", getType());
		json.addProperty("uid", stack.getJeiUid());
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
