package dev.emi.emi.jemi;

import java.util.Optional;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.serializer.EmiStackSerializer;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

@SuppressWarnings("rawtypes")
public class JemiStackSerializer implements EmiStackSerializer<JemiStack> {
	private final IIngredientManager manager;

	public JemiStackSerializer(IIngredientManager manager) {
		this.manager = manager;
	}

	@Override
	public String getType() {
		return "jemi";
	}

	@Override
	public EmiStack create(Identifier id, NbtCompound nbt, long amount) {
		for (IIngredientType<?> type : manager.getRegisteredIngredientTypes()) {
			if (type == VanillaTypes.ITEM_STACK || type == JemiUtil.getFluidType()) {
				continue;
			}
			Optional<?> opt = manager.getIngredientByUid(type, id.toString());
			if (opt.isPresent()) {
				return JemiUtil.getStack(type, opt.get()).setAmount(amount);
			}
		}
		return EmiStack.EMPTY;
	}
}
